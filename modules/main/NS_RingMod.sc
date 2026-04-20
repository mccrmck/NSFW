NS_RingMod : NS_SynthModule {

    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_ringMod" ++ numChans).asSymbol,
            {
                var sig  = In.ar(\bus.kr, numChans);
                var freq = \freq.kr(40).lag(0.05);
                var mod  = SinOsc.ar(\modFreq.kr(40), mul: \modMul.kr(1));
                sig = sig * SinOsc.ar(freq + mod);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\freq, ControlSpec(1, 3500, \exp), 40)
                    .addAction(\synth,{ |c| synths[0].set(\freq, c.value) }),

                    NS_ControlFloat(\mFreq, ControlSpec(1, 3500, \exp), 4)
                    .addAction(\synth,{ |c| synths[0].set(\modFreq, c.value) }),

                    NS_ControlFloat(\mMul, ControlSpec(1, 3500, \amp))
                    .addAction(\synth,{ |c| synths[0].set(\modMul, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    }),
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['freq'], 1),
            NS_ControlFader(controlDict['mFreq'], 1),
            NS_ControlFader(controlDict['mMul'], 1),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(width: "70%"),
            OpenStageFader(true, false),
            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height: "20%")
            ])
        ], columns: 3, randCol: true).oscString("RingMod")
    }
}
