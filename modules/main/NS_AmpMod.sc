NS_AmpMod : NS_SynthModule {

    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_ampMod" ++ numChans).asSymbol,
            {
                var sig    = In.ar(\bus.kr, numChans);
                var freq   = \freq.kr(4);
                var rDuty  = \rDuty.kr(2);
                var phase  = Phasor.ar(DC.ar(0), freq * SampleDur.ir) * rDuty;
                var window = NS_UnitShape.gaussianWin(
                    phase.clip(0, 1), \skew.kr(0.5), \index.kr(1)
                );

                sig = sig * window;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\freq, ControlSpec(1, 5000, \exp), 4)
                    .addAction(\synth,{ |c| synths[0].set(\freq, c.value) }),

                    NS_ControlFloat(\rDuty, ControlSpec(1, 10), 2)
                    .addAction(\synth,{ |c| synths[0].set(\rDuty, c.value) }),

                    NS_ControlFloat(\skew, ControlSpec(0, 0.99), 0.5)
                    .addAction(\synth,{ |c| synths[0].set(\skew, c.value) }),

                    NS_ControlFloat(\index, ControlSpec(1, 8), 1)
                    .addAction(\synth,{ |c| synths[0].set(\index, c.value) }),

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
            NS_ControlFader(controlDict['rDuty']),
            NS_ControlFader(controlDict['skew']),
            NS_ControlFader(controlDict['index']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {
        ^OpenStagePanel().widgetArray_([
            OpenStageXY(),
            OpenStageXY(),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false).vertical,
                OpenStageButton().height_("20%")
            ]).width_("15%")
        ]).columns_(3).randCol.label_("AmpMod")
    }
}
