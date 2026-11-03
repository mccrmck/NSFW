NS_AmpMod : NS_SynthModule {

    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_ampMod" ++ numChans).asSymbol,
            {
                var sig    = In.ar(\bus.kr, numChans);
                var freq   = \freq.kr(4);
                var offset  = \offset.kr(0);
                var window = SinOsc.ar(freq / 2).abs;

                window = (window * (1 - offset)) + offset;
                sig = sig * window;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\freq, ControlSpec(16, 8000, \exp), 40)
                    .addAction(\synth,{ |c| synths[0].set(\freq, c.value) }),

                    NS_ControlFloat(\offset, ControlSpec(0, 0.5, \lin), 0)
                    .addAction(\synth,{ |c| synths[0].set(\offset, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
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
            NS_ControlFader(controlDict['freq'], 1),
            NS_ControlFader(controlDict['offset']),
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
        ]).randCol.label_("AmpMod")
    }
}
