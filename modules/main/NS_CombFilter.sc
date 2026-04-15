NS_CombFilter : NS_SynthModule {

    buildSynthModule {

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_combFilter" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = CombC.ar(
                    sig, 0.2, \delay.kr(250).reciprocal.lag, \decay.kr(0.5)
                );
                sig = sig + PinkNoise.ar(0.0001);
                sig = LeakDC.ar(sig.tanh);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_Control(\freq, ControlSpec(20, 1200, \exp), 250)
                    .addAction(\synth,{ |c| synths[0].set(\delay, c.value) }),

                    NS_Control(\decay, ControlSpec(0.1, 3, \exp), 0.5)
                    .addAction(\synth,{ |c| synths[0].set(\decay, c.value) }),

                    NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    })
                )
            }
        );

        loaded = true;
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['freq'], 1),
            NS_ControlFader(controlDict['decay']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height: "20%")
            ], width: "15%")
        ], columns: 2, randCol: true).oscString("CombFilter")
    }
}
