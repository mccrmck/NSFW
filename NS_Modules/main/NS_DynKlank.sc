NS_DynKlank : NS_SynthModule {
    classvar <isSource = false;
    var busses, notes;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(52);

        busses = (
            octave:   Bus.control(server, 12).setn(1 ! 12),
            bandAmp:  Bus.control(server, 12).setn(0 ! 12),
            bandMute: Bus.control(server, 12).setn(0 ! 12),
            ringTime: Bus.control(server, 12).setn(0.25 ! 12)
        );

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_dynKlank" ++ numChans).asSymbol,
            {
                var sig      = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;  // this is a shame, no?
                var freq     = (60,61..71).midicps * 2;
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
            busses.asPairs ++ [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                notes = ["C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"];

                notes.do({ |note, index|

                    controls[index * 4] = NS_Control(note + "dB",\amp,0)
                    .addAction(\synth,{ |c| busses['bandAmp'].subBus(index).set( c.value ) });

                    controls[index * 4 + 1] = NS_Control(note + "dcy", ControlSpec(0.1,1.5,\lin), 0.25)
                    .addAction(\synth,{ |c| busses['ringTime'].subBus(index).set( c.value ) });

                    controls[index * 4 + 2] = NS_Control(note + "oct",ControlSpec(0,4,\lin,1),2)
                    .addAction(\synth,{ |c| busses['octave'].subBus(index).set([0.25,0.5,1,2,4].at( c.value )) });

                    controls[index * 4 + 3] = NS_Control(note + "on",ControlSpec(0,1,\lin,1),0)
                    .addAction(\synth,{ |c| busses['bandMute'].subBus(index).set( c.value ) });
                });

                controls[48] = NS_Control(\trim,\boostcut,0)
                .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });

                controls[49] = NS_Control(\gain,ControlSpec(-12,12,\db),0)
                .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) });

                controls[50] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[51] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
                .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

                { this.makeModuleWindow }.defer;
                loaded = true;
            } 
        );
    }

    makeModuleWindow {

        this.makeWindow("DynKlank", Rect(0,0,690,360));

        win.layout_(
            VLayout(
                *(notes.collect({ |n, i|
                    var index = (i * 4).asInteger;
                    HLayout(
                        NS_ControlFader(controls[index], 0.01),
                        NS_ControlFader(controls[index + 1], 0.01),
                        NS_ControlSwitch(controls[index + 2], ["16vb","8vb","nat","8va","16va"], 5),
                        NS_ControlButton(controls[index + 3], [
                            [NS_Style.play, NS_Style.green, NS_Style.bGroundDark],
                            ["X", NS_Style.textDark, NS_Style.red]
                        ]).maxWidth_(45)
                    )
                }) ++ [
                    HLayout( 
                        NS_ControlFader(controls[48]),
                        NS_ControlFader(controls[49]),
                        NS_ControlFader(controls[50]),
                        NS_ControlButton(controls[51], ["▶","bypass"]).maxWidth_(45)
                    )
                ])
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra {
        busses.do(_.free)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Panel( {OSC_Button()} ! 12, columns: 6),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width:"20%")], columns: 2)
        ], randCol: true).oscString("DynKlank")
    }
}
