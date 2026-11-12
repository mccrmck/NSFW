NS_EnvGen : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_envGen" ++ numChans).asSymbol,
            {
                var sig   = In.ar(\bus.kr, numChans);
                var duty  = \duty.kr(1);
                var freq  = \freq.kr(2);
                var phase = Phasor.ar(DC.ar(0), freq * SampleDur.ir) * duty;

                var window = NS_UnitShape.exponentialWin(
                    phase.clip(0, 1), \skew.kr(0.5), \curve.kr(0.5)
                );

                sig = sig * window;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    // add in the modulation controls to freq as well
                    NS_ControlFloat(\freq, ControlSpec(0.05, 30, \exp), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\freq, c.value) }),

                    NS_ControlFloat(\duty, ControlSpec(1, 10, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\duty, c.value) }),

                    NS_ControlFloat(\skew, ControlSpec(0.05,0.95), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\skew, c.value) }),

                    NS_ControlFloat(\curve, ControlSpec(0.05,0.95), 0.5) // kiki < triangle < bouba
                    .addAction(\synth, { |c| synths[0].set(\curve, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    }),
                );

                loaded = true;
            }
        );
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['freq']),
            NS_ControlFader(controlDict['duty']),
            NS_ControlFader(controlDict['skew']),
            NS_ControlFader(controlDict['curve']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false),
                OpenStageButton().width_("20%")
            ]).columns_(2)
        ]).randCol.label_("EnvGen")
    }
}
