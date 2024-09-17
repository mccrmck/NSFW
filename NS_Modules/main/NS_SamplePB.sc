NS_SamplePB : NS_SynthModule{
    classvar <isSource = true;
    var rateBus, ampBus;
    var bufArray, bufferPath;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_samplePBmono,{
                var numChans = NSFW.numOutChans;
                var bufnum   = \bufnum.kr;
                var sig = PlayBuf.ar(1,bufnum,BufRateScale.kr(bufnum) * \rate.kr(1),doneAction:2);

                // should I add an envelop with BufDur? This is lazy...

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                sig = NS_Pan(sig,numChans,Rand(-0.8,0.8),numChans/4);

                // should I add a mix control here? 
                Out.ar(\bus.kr,sig);
                //NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )

            }).add
        }
    }

    init {
        this.initModuleArrays(20);
        this.makeWindow("SamplePB", Rect(0,0,300,250));

        rateBus  = Bus.control(modGroup.server,1).set(1);
        ampBus   = Bus.control(modGroup.server,1).set(1);
        bufArray = Array.newClear(16);

        16.do({ |buttonIndex|

            controls.add(
                Button()
                .maxWidth_(45)
                .states_([[buttonIndex,Color.black,Color.white],[buttonIndex,Color.white,Color.black]])
                .action_({ |but|

                    if(but.value == 1,{
                        Synth(\ns_samplePBmono,[
                            \bufnum, bufArray[buttonIndex],
                            \rate,rateBus.getSynchronous,
                            \amp,ampBus.asMap,
                            \bus,bus
                        ],strip.inSynth,\addBefore)
                    })
                })
            );
            assignButtons[buttonIndex] = NS_AssignButton(this, buttonIndex, \button).maxWidth_(45)

        });

        controls.add(
            NS_Fader("rate",ControlSpec(0.5,2,\exp),{ |f| rateBus.set(f.value) },initVal:1).maxWidth_(45)
        );
        assignButtons[16] = NS_AssignButton(this, 16, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("amp",\db,{ |f| ampBus.set(f.value.dbamp) },initVal:1).maxWidth_(45)
        );
        assignButtons[17] = NS_AssignButton(this, 17, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
            })
        );
        assignButtons[18] = NS_AssignButton(this, 18, \button).maxWidth_(45);

        controls.add(
            DragSink()
            .background_(Color.white)
            .align_(\center)
            .string_("drag sample folder here")
            .canReceiveDragHandler_({ View.currentDrag.isKindOf(String) })
            .receiveDragHandler_({ |sink|
                bufferPath = View.currentDrag;
                sink.object_(PathName(bufferPath).folderName);
                bufArray.do(_.free);
                {
                    PathName(bufferPath).entries.wrapExtend(16).do({ |entry, index|

                        bufArray[index] = Buffer.readChannel(modGroup.server,entry.fullPath,channels: [0]);
                    });
                    modGroup.server.sync;
                }.fork(AppClock)
            })
        );

        win.layout_(
            HLayout(
                VLayout(
                    GridLayout.rows(
                        controls[0..3],
                        assignButtons[0..3],
                        controls[4..7],
                        assignButtons[4..7],
                        controls[8..11],
                        assignButtons[8..11],
                        controls[12..15],
                        assignButtons[12..15],
                    ),
                    VLayout( controls[19] ),
                ),
                VLayout( controls[16], assignButtons[16] ),
                VLayout( controls[17], assignButtons[17], controls[18], assignButtons[18] )
            )
        );
        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        bufArray.do(_.free)
    }

    saveExtra { |saveArray|
        var moduleArray = List.newClear(0);
        moduleArray.add( bufferPath );
        saveArray.add( moduleArray );
        ^saveArray
    }

    loadExtra { |loadArray|
        if(loadArray[0].notNil,{
            bufferPath = loadArray[0];
            controls[19].object_(PathName(bufferPath).folderName);

            {
                PathName(bufferPath).entries.wrapExtend(16).do({ |entry, index|

                    bufArray[index] = Buffer.readChannel(modGroup.server,entry.fullPath,channels: [0]);
                });
                modGroup.server.sync;
            }.fork(AppClock)
        })
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 4 ),
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 4 ),
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 4 ),
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 4 ),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true)
        ],randCol:true).oscString("SamplePB")
    }
}
