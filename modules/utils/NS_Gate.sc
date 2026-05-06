NS_Gate : NS_SynthModule {

    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_gate" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var thresh = \thresh.kr(36.neg);
                var sliceDur = SampleRate.ir * 0.01;
                var gate = FluidAmpGate.ar(
                    sig, 10, 10, thresh, thresh - 5, 
                    sliceDur, sliceDur, sliceDur, sliceDur
                );

                gate = LagUD.ar(gate, \atk.kr(0.01), \rls.kr(0.01));
                sig = sig * gate;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\thresh, \db, -36)
                    .addAction(\synth, { |c| synths[0].set(\thresh, c.value) }),

                    NS_ControlFloat(\atk, ControlSpec(0, 0.1), 0.01)
                    .addAction(\synth, { |c| synths[0].set(\atk, c.value) }),

                    NS_ControlFloat(\rls, ControlSpec(0, 0.1), 0.1)
                    .addAction(\synth, { |c| synths[0].set(\rls, c.value) }),

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
            NS_ControlFader(controlDict['atk'], 0.001),
            NS_ControlFader(controlDict['rls'], 0.001),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {
        ^OpenStagePanel().widgetArray_(
            { OpenStageFader() } ! 3 ++ [ OpenStageButton() ], 
        ).randCol.label_("Gate")
    }
}
