NS_Flanger : NS_SynthModule {

    buildSynthModule {
        
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_flanger" ++ numChans).asSymbol,
            {
                var in = In.ar(\bus.kr, numChans);
                var buf = { LocalBuf((SampleRate.ir * 0.009).asInteger).clear } ! numChans;
                var tap = DelTapWr.ar(buf, in + LocalIn.ar(numChans));
                var phase = SinOsc.ar(\rate.kr(0.25)).unipolar * \depth.kr(1);

                // sweep between 0.5 - 5.5ms in buffer
                var sig = DelTapRd.ar(buf, tap, 0.0005 + (phase * 0.005), 2);

                LocalOut.ar(
                    // heuristic attenuation
                    (sig * \feedB.kr(0.5) * numChans.reciprocal.sqrt)
                    + PinkNoise.ar(0.0001)
                );

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\rate, ControlSpec(0.01, 2, \exp), 0.25)
                    .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

                    NS_ControlFloat(\depth, ControlSpec(0.01, 1, \exp), 0.25)
                    .addAction(\synth, { |c| synths[0].set(\depth, c.value) }),

                    NS_ControlFloat(\feedB, ControlSpec(0, 0.95, \lin), 0.25)
                    .addAction(\synth, { |c| synths[0].set(\feedB, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        this.gateBool_(c.value); 
                        synths[0].set(\thru, c.value)
                    })
                );

                loaded = true
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['depth']),
            NS_ControlFader(controlDict['feedB']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false),
                OpenStageButton().height("20%")
            ]).columns_(2)   
        ]).randCol.label_("Flanger")
    }
}
