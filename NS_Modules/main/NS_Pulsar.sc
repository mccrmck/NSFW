NS_Pulsar : NS_SynthModule {
    // based on dietcv's research, post a link here!

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;
        var grainChans = 4;

        this.initModuleArrays(7);
       
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

                sig = sig * SinOsc.ar(420);
                sig = LeakDC.ar(sig.sum * ampComp) * -12.dbamp;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth|
                synths.add(synth);

                controls[0] = NS_Control(\freq, ControlSpec(1, 8000, \exp), 80)
                .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });

                controls[1] = NS_Control(\overlap, ControlSpec(0.125, grainChans, \exp), 1)
                .addAction(\synth,{ |c| synths[0].set(\overlap, c.value) });

                controls[2] = NS_Control(\skew, ControlSpec(0.01, 0.99, 'lin'), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\skew, c.value) });

                controls[3] = NS_Control(\width, ControlSpec(0.01, 0.99, 'lin'), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\width, c.value) });

                controls[4] = NS_Control(\duty, ControlSpec(0.01, 0.99, 'lin'), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\duty, c.value) });

                controls[5] = NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[6] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value);
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("Pulsar", Rect(0,0,180,120));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 1),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlFader(controls[5]),
                NS_ControlButton(controls[6], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(height: "40%"),
            OpenStageFader(false),
            OpenStageFader(false),
            OpenStageFader(false),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("Pulsar")
    }
}
