NS_Squish : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_squish" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var amp = Amplitude.ar(sig, \atk.kr(0.01), \rls.kr(0.1));
                amp = amp.max(-100.dbamp).ampdb;
                amp = (amp - \thresh.kr(-12)).max(0) * (\ratio.kr(4).reciprocal - 1);
                amp = amp.lag(\knee.kr(0)).dbamp;

                sig = sig * amp * \muGain.kr(0).dbamp;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\thresh, \db, -12)
                    .addAction(\synth, { |c| synths[0].set(\thresh, c.value) }),

                    NS_ControlFloat(\ratio, ControlSpec(1, 20), 4)
                    .addAction(\synth, { |c| synths[0].set(\ratio, c.value) }),

                    NS_ControlFloat(\atk, ControlSpec(0.001, 0.1, \lin), 0.001)
                    .addAction(\synth, { |c| synths[0].set(\atk, c.value) }),

                    NS_ControlFloat(\rls, ControlSpec(0.001, 0.3, \lin), 0.001)
                    .addAction(\synth, { |c| synths[0].set(\rls, c.value) }),

                    NS_ControlFloat(\knee, ControlSpec(0, 0.5, \lin), 0.1)
                    .addAction(\synth, { |c| synths[0].set(\knee, c.value) }),

                    NS_ControlFloat(\mUp, ControlSpec(0, 20, \db), 0)
                    .addAction(\synth, { |c| synths[0].set(\muGain, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1, \lin), 1)
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
            NS_ControlFader(controlDict['thresh'], 0.1),
            HLayout( 
                NS_ControlKnob(controlDict['ratio'], 0.1).minHeight_(75),
                NS_ControlKnob(controlDict['atk'], 0.001).minHeight_(75),
                NS_ControlKnob(controlDict['rls'], 0.001).minHeight_(75),
                NS_ControlKnob(controlDict['knee'], 0.01).minHeight_(75),
            ),
            NS_ControlFader(controlDict['mUp'], 0.1),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStagePanel({ OpenStageKnob() } ! 4, columns: 4),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2),
        ], randCol: true).oscString("Squish")
    }
}
