NS_Gain : NS_SynthModule {

    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_gain" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = sig * \gain.kr(1);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\trim, \boostcut, 0)
                    .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) }),

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
            NS_ControlFader(controlDict['trim'], 0.01), 
            NS_ControlButton.bypass(controlDict['bypass'])
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader().snap_(false).vertical,
            OpenStageButton().height_("20%"),
        ]).randCol.label_("Gain")
    }
}
