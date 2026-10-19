NS_Chorus : NS_SynthModule {
    classvar numVoices = 3;

    buildSynthModule {

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_chorus" ++ numChans).asSymbol,
            {
                var in     = In.ar(\bus.kr, numChans);
                // 35 ms buffer
                var buf    = LocalBuf((SampleRate.ir * 0.035).asInteger).clear;
                var tap    = DelTapWr.ar(buf, in);
                var rate   = { rrand(0.87,1.13) } ! numVoices * \rate.kr(1);
                var offset = numVoices.collect { |v| v * (360/numVoices) }.degrad;
                var sweep  = 0.007 * \depth.kr(1, 0.5);
                var phase  = SinOsc.ar(rate, offset).unipolar * sweep;
                // sweep between 18 - 25ms in buffer
                var sig = DelTapRd.ar(buf, tap, 0.018 + phase, 2); 

                // for now, this outputs mono...would be nice with a Splay on the output!
                sig = sig.sum * numVoices.reciprocal.sqrt;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\rate, ControlSpec(0.01, 3, \exp), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

                    NS_ControlFloat(\depth, ControlSpec(0.01, 2, \exp), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\depth, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        this.gateBool_(c.value); 
                        synths[0].set(\thru, c.value)
                    })
                );

                loaded = true;
            }
        );
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['depth']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false),
                OpenStageButton().width_("20%")
            ]).columns_(2)   
        ]).randCol.label_("Chorus")
    }
}
