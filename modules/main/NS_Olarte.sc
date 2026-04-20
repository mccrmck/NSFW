NS_Olarte : NS_SynthModule {

    // the original synthdef came to me from Alejandro Olarte,
    // via Harald Jordal Johannessen, I've since made some tweaks
    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_olarte" ++ numChans).asSymbol,
            {
                var freq = \freq.kr(4);
                var sr = SampleRate.ir * \sRate.kr(1);
                var bits = \bits.ar(32);  
                var powBits = 2 ** bits;
                var t = Phasor.ar(DC.ar(0), freq * (powBits / sr), 0, powBits - 1 );
                var array = [
                    t * (( (t>>64) | (t>>8) ) & (63 & (t>>4)) ),
                    t * (( (t>>9)  | (t>>13)) & (25 & (t>>6)) ),
                    t * (( (t>>5)  | (t>>8) ) & 63),
                    t * (((t>>11)  & (t>>8) ) & (123 & (t>>3)) ),
                    t * (t>>8 * ((t>>15) | (t>>8)) & (20 | (t>>19) * 5>>t | (t>>3))),
                    t * (t>>( (t>>9) | (t>>8) ) & (63 & (t>>4)) ),
                    (t>>7 | t | t>>6) * 10 + 4 * (t & t>>13 | t>>6 )
                ];

                var sig = SelectX.ar(\which.kr(0, 0.1), array);

                sig = sig % powBits;
                sig = sig * (0.5 ** (bits - 1)) - 1;
                sig = LeakDC.ar(sig) * -12.dbamp;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\sRate, ControlSpec(0.01, 1, \exp), 1)
                    .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) }),

                    NS_ControlFloat(\bits, ControlSpec(8, 32, \exp), 32)
                    .addAction(\synth,{ |c| synths[0].set(\bits, c.value) }),

                    NS_ControlFloat(\freq, ControlSpec(0.01, 250, \exp), 4)
                    .addAction(\synth,{ |c| synths[0].set(\freq, c.value) }),

                    NS_ControlInt(\which, 0, 6, 0)
                    .addAction(\synth,{ |c| synths[0].set(\which, c.value) }),

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
            NS_ControlFader(controlDict['sRate']),
            NS_ControlFader(controlDict['bits']),
            NS_ControlFader(controlDict['freq']),
            NS_ControlSwitch(controlDict['which'], (0..6), 7),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(height: "45%"),
            OpenStageFader(),
            OpenStageSwitch(7, 7),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("Olarte")
    }
}
