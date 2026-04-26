NS_DynKlank : NS_SynthModule {
    var busses, notes;

    buildSynthModule {

        busses = (
            octave:   Bus.control(nsServer.server, 12).setn(1 ! 12),
            bandAmp:  Bus.control(nsServer.server, 12).setn(0 ! 12),
            bandMute: Bus.control(nsServer.server, 12).setn(0 ! 12),
            ringTime: Bus.control(nsServer.server, 12).setn(0.25 ! 12)
        );

        notes = ["C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"];

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_dynKlank" ++ numChans).asSymbol,
            {
                var sig      = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;  // this is a shame, no?
                var freq     = (60, 61..71).midicps * 2;
                var octave   = In.kr(\octave.kr, 12);
                var bandAmp  = In.kr(\bandAmp.kr, 12);
                var bandMute = In.kr(\bandMute.kr, 12);
                var ringTime = In.kr(\ringTime.kr, 12);

                sig = sig  * -18.dbamp * \trim.kr(1);
                sig = DynKlank.ar(`[
                    freq * octave.lag(1),
                    bandAmp.lag(0.1) * bandMute.varlag(4),
                    ringTime.lag(1)
                ], sig);

                sig = sig.tanh * \gain.kr(1);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            busses.asPairs ++ [\bus, modBus],
            { |synth| 
                synths.add(synth);

                notes.do { |note, index|
                    controlDict.addAll(
                        NS_ControlFloat(note + "dB", \amp, 0)
                        .addAction(\synth, { |c| 
                            busses['bandAmp'].subBus(index).set(c.value)
                        }),

                        NS_ControlFloat(note + "dcy", ControlSpec(0.1, 1.5, \lin), 0.25)
                        .addAction(\synth, { |c| busses['ringTime'].subBus(index).set(c.value) }),

                        NS_ControlInt(note + "oct", 0, 4, 2)
                        .addAction(\synth, { |c| 
                            var octave = [0.25, 0.5, 1, 2, 4].at(c.value); 
                            busses['octave'].subBus(index).set(octave)
                        }),

                        NS_ControlInt(note + "on", 0, 1, 0)
                        .addAction(\synth, { |c| 
                            busses['bandMute'].subBus(index).set(c.value)
                        }),
                    )
                };

                controlDict.addAll(
                    NS_ControlFloat(\trim, \boostcut, 0)
                    .addAction(\synth, { |c| synths[0].set(\trim, c.value.dbamp) }),

                    NS_ControlFloat(\gain, ControlSpec(-12, 12, \db), 0)
                    .addAction(\synth, { |c| synths[0].set(\gain, c.value.dbamp) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1, \lin), 1)
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
            *(notes.collect { |n|
                HLayout(
                    NS_ControlFader(controlDict[(n + "dB").asSymbol], 0.01),
                    NS_ControlFader(controlDict[(n + "dcy").asSymbol], 0.01),
                    NS_ControlSwitch(
                        controlDict[(n + "oct").asSymbol],
                        ["16vb","8vb","nat","8va","16va"],
                        5
                    ),
                    NS_ControlButton(controlDict[(n + "on").asSymbol], [
                        [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')],
                        [NS_Style('clear'), NS_Style('textDark'), NS_Style('red')]
                    ]).maxWidth_(45)
                )
            } ++ [
                HLayout( 
                    NS_ControlFader(controlDict['trim']),
                    NS_ControlFader(controlDict['gain']),
                    NS_ControlFader(controlDict['mix']),
                    NS_ControlButton.bypass(controlDict['bypass']).maxWidth_(45)
                )
            ])
        )
    }

    freeExtra { busses.do(_.free) }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStagePanel( {OpenStageButton()} ! 12, columns: 6),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width:"20%")
            ], columns: 2)
        ], randCol: true).oscString("DynKlank")
    }
}
