NS_Pulsar : NS_SynthModule {
    // based on dietcv's research, post a link here!

    buildSynthModule {
        var grainChans = 4;

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_pulsar" ++ numChans).asSymbol,
            {
                var freq        = \freq.kr(80);
                //var formantFreq = \formant.kr(160);
                //var overlap     = freq / formantFreq;
                var overlap     = \overlap.kr(1);
                var maxOverlap  = min(overlap, grainChans);
                var phase       = Phasor.ar(DC.ar(0), freq * SampleDur.ir);
                var trig        = NS_GrainFuncs.rampToTrig(phase);
                var slope       = NS_GrainFuncs.rampToSlope(phase);
                var mChanTrig   = NS_GrainFuncs.mChanTrigger(grainChans, trig);
                var subS        = NS_GrainFuncs.subSampleOffset(phase, slope, mChanTrig);

                var mChanAccum = NS_GrainFuncs.mChanAccumSubSample(mChanTrig, subS);
                var slopes     = Latch.ar(slope, mChanTrig) / max(0.001, maxOverlap);
                var winPhase   = slopes * mChanAccum;
                var ampComp    = max(1, overlap).reciprocal.sqrt;

                var sig = NS_UnitShape.tukeyWin(
                    winPhase.clip(0, 1), \skew.kr(0.5), \width.kr(0.5), \duty.kr(0.5)
                );

                // aha, I'm so funny...fix this
                sig = sig * SinOsc.ar(420);
                sig = LeakDC.ar(sig.sum * ampComp) * -12.dbamp;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\freq, ControlSpec(1, 8000, \exp), 80)
                    .addAction(\synth, { |c| synths[0].set(\freq, c.value) }),

                    NS_ControlFloat(\overlap, ControlSpec(0.125, grainChans, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\overlap, c.value) }),

                    NS_ControlFloat(\skew, ControlSpec(0.01, 0.99), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\skew, c.value) }),

                    NS_ControlFloat(\width, ControlSpec(0.01, 0.99), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\width, c.value) }),

                    NS_ControlFloat(\duty, ControlSpec(0.01, 0.99), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\duty, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    }),
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['freq'], 1),
            NS_ControlFader(controlDict['overlap']),
            NS_ControlFader(controlDict['skew']),
            NS_ControlFader(controlDict['width']),
            NS_ControlFader(controlDict['duty']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageXY().height_("40%"),
            OpenStageFader().snap_(false),
            OpenStageFader().snap_(false),
            OpenStageFader().snap_(false),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false),
                OpenStageButton().width_("20%")
            ]).columns_(2)
        ]).randCol.label_("Pulsar")
    }
}
