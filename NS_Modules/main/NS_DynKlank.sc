NS_DynKlank : NS_SynthModule {
    classvar <isSource = false;
    var notes;
    var octaveBus, bandAmpBus, bandMuteBus, ringTimeBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_dynKlank,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;
                var freq = (60,61..71).midicps * 2;
                var octave = In.kr(\octave.kr,12);
                var bandAmp = In.kr(\bandAmp.kr,12);
                var bandMute = In.kr(\bandMute.kr,12);
                var ringTime = In.kr(\ringTime.kr,12);

                sig = sig  * -18.dbamp * \trim.kr(1);
                sig = DynKlank.ar(`[ freq * octave.lag(1), bandAmp.lag(0.1) * bandMute.varlag(4), ringTime.lag(1) ],sig);

                sig = sig.tanh * \gain.kr(1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add     
        }
    }

    init {
        this.initModuleArrays(52);
        this.makeWindow("DynKlank", Rect(0,0,870,400));

        notes = ["C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"];

        octaveBus   = Bus.control(modGroup.server,12).setn(1!12);
        bandAmpBus  = Bus.control(modGroup.server,12).setn(0!12);
        bandMuteBus = Bus.control(modGroup.server,12).setn(0!12);
        ringTimeBus = Bus.control(modGroup.server,12).setn(0.25!12);

        synths.add( Synth(\ns_dynKlank,[\octave,octaveBus,\bandAmp,bandAmpBus,\bandMute, bandMuteBus,\ringTime,ringTimeBus,\bus,bus],modGroup) );

        notes.do({ |note, index|

            controls.add(
                NS_Fader(note + "dB",\amp,{ |f| bandAmpBus.subBus(index).set( f.value ) },initVal: 0).maxWidth_(45);
            );
            assignButtons[index * 4] = NS_AssignButton(this, index * 4, \fader).maxWidth_(45);

            controls.add(
                NS_Fader(note + "dcy",ControlSpec(0.1,1.5,\lin),{ |f| ringTimeBus.subBus(index).set( f.value ) },initVal:0.25).maxWidth_(45);
            );
            assignButtons[index * 4 + 1] = NS_AssignButton(this, index * 4 + 1, \fader).maxWidth_(45);

            controls.add(
                NS_Switch(["16vb","8vb","nat","8va","16va"],{ |switch| octaveBus.subBus(index).set([0.25,0.5,1,2,4].at(switch.value)) }).minHeight_(90).maxWidth_(45)
            );
            assignButtons[index * 4 + 2] = NS_AssignButton(this, index * 4 + 2, \switch).maxWidth_(45);

            controls.add(
                Button()
                .maxWidth_(45)
                .states_([["▶",Color.green,Color.black],["X",Color.black,Color.red]])
                .action_({ |but|
                    bandMuteBus.subBus(index).set(but.value)
                })       
            );
            assignButtons[index * 4 + 3] = NS_AssignButton(this, index * 4 + 3, \button).maxWidth_(45);
        });

        controls.add(
            NS_Fader("trim",\boostcut,{ |f| synths[0].set(\trim, f.value.dbamp) },'horz',initVal:0)  
        );
        assignButtons[48] = NS_AssignButton(this, 48, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("gain",ControlSpec(-12,12,\db),{ |f| synths[0].set(\gain, f.value.dbamp) },'horz',initVal:0)  
        );
        assignButtons[49] = NS_AssignButton(this, 49, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)  
        );
        assignButtons[50] = NS_AssignButton(this, 50, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })       
        );
        assignButtons[51] = NS_AssignButton(this, 51, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                GridLayout.rows(
                    *notes.collect({ |n, i|
                        var index = (i * 4).asInteger;
                        GridLayout.columns(
                            [ [controls[index], rows: 3],     assignButtons[index] ],
                            [ [controls[index + 1], rows: 3], assignButtons[index + 1] ],
                            [ controls[index + 2], assignButtons[index + 2], controls[index + 3], assignButtons[index + 3] ],
                        )
                    }).clump(6),
                ),
                HLayout( 
                    controls[48], assignButtons[48],
                    controls[49], assignButtons[49],
                    controls[50], assignButtons[50],
                    controls[51], assignButtons[51] 
                )
            )
        );

        notes.do({|n,i| controls[i * 4 + 2].value_(2) });
        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        octaveBus.free;
        bandAmpBus.free;
        bandMuteBus.free;
        ringTimeBus.free;
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Panel(widgetArray: { OSC_Button() } ! 6),
            OSC_Panel(widgetArray: { OSC_Button() } ! 6),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal:true),
                OSC_Button(width:"20%")
            ])
        ],randCol: true).oscString("DynKlank")
    }
}
