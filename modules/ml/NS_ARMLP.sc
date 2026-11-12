NS_ARMLP : NS_SynthModule {

    buildSynthModule {

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_armlp" ++ numChans).asSymbol,
            {
                var in = In.ar(\bus.kr, numChans);
                var sig = in.sum * numChans.reciprocal.sqrt + LocalIn.ar(1);
                sig = ARMLP.ar(
                    sig, 
                    learnRate1: \lr1.kr,
                    learnRate2: \lr1.kr + \lr2.kr(0.001),
                    act1: \act1.kr(0.5),
                    act2: \act2.kr(0.5),
                    reset: \reset.tr(0)
                );

                LocalOut.ar(sig * \feedB.kr(0.8));
                sig = LeakDC.ar(Sanitize.ar(sig));

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\learn1, ControlSpec(0.001, 0.1, \exp), 0.005)
                    .addAction(\synth, { |c| synths[0].set(\lr1, c.value) }),

                    NS_ControlFloat(\learn2, ControlSpec(0.001, 0.1, \exp), 0.01)
                    .addAction(\synth, { |c| synths[0].set(\lr2, c.value) }),
                    
                    NS_ControlFloat(\activation1, ControlSpec(0, 1.5, \lin), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\act1, c.value) }),

                    NS_ControlFloat(\activation2, ControlSpec(0, 1.5, \lin), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\act2, c.value) }),

                    NS_ControlFloat(\feedB, ControlSpec(0.5, 1.5), 0.8)
                    .addAction(\synth, { |c| synths[0].set(\feedB, c.value) }),

                    NS_ControlInt(\reset, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\reset, c.value) }),

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
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['learn1'], 0.001),
            NS_ControlFader(controlDict['learn2'], 0.001),
            NS_ControlFader(controlDict['activation1']),
            NS_ControlFader(controlDict['activation2']),
            NS_ControlFader(controlDict['feedB']),
            NS_ControlButton(controlDict['reset'], "reset" ! 2),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass'])
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader().height_("15%"),
            OpenStagePanel().widgetArray_([
                OpenStageXY(),
                OpenStageXY(),
            ]).columns_(2),
            OpenStageFader().snap_(false).height_("15%"),
            OpenStagePanel().widgetArray_([
                OpenStageButton().mode_("push"),
                OpenStageButton()
            ]).columns_(2).height_("15%"),
        ]).randCol.label_("ARMLP")
    }
}
