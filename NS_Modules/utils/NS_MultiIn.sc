NS_MultiIn : NS_SynthModule {
    classvar <isSource = true;
    var dragSinks;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_multiIn,{
                var sig = 4.collect({ |i|
                    var name = "inBus" ++ i;
                    In.ar(NamedControl.ar(name.asSymbol),numChans)
                    * NamedControl.kr(("amp" ++ i).asSymbol,0)
                    * (1 - NamedControl.kr(("mute" ++ i).asSymbol,0))
                });

                sig = sig.sum * (1 - \muteAll.kr(0));
                                
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add
        }
    }

    init {
        this.initModuleArrays(10);
        this.makeWindow("MultiIn", Rect(0,0,330,150));

        synths.add( Synth(\ns_multiIn,[\bus,bus],modGroup) );

        4.do({ |index|
            controls[index * 2] = NS_Control("dB" ++ index, \db)
            .addAction(\synth, { |c| synths[0].set(("amp" ++ index).asSymbol, c.value.dbamp) });
            assignButtons[index * 2] = NS_AssignButton(this, index * 2, \fader).maxWidth_(30);

            controls[index * 2 + 1] = NS_Control("mute" ++ index, ControlSpec(0,1,\lin,1),0)
            .addAction(\synth, { |c| synths[0].set(("mute" ++ index).asSymbol, c.value) });
            assignButtons[index * 2 + 1] = NS_AssignButton(this, index * 2 + 1, \button).maxWidth_(30);
        });

        controls[8] = NS_Control(\amp, \db)
        .addAction(\synth, { |c| synths[0].set(\amp, c.value.dbamp) });
        assignButtons[8] = NS_AssignButton(this, 8, \fader).maxWidth_(30);

        controls[9] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| /*strip.inSynthGate_(c.value);*/ synths[0].set(\muteAll, c.value) });
        assignButtons[9] = NS_AssignButton(this, 9, \button).maxWidth_(30);


        // should these have wee red X boxes to remove the bus?
        dragSinks = 4.collect({ |index|
            DragSink()
            .align_(\center).background_(Color.white).string_("in")
            .receiveDragHandler_({ |drag|
                var dragObject = View.currentDrag;

                if(dragObject.isInteger and: {dragObject < NSFW.servers[modGroup.server.name].options.inChannels},{

                    drag.object_(dragObject);
                    drag.align_(\left).string_("in:" + dragObject.asString);
                    synths[0].set( 
                        ("inBus" ++ index).asSymbol, 
                        NSFW.servers[modGroup.server.name].inputBusses[dragObject]
                    )
                },{
                    "drag Object not valid".warn
                })
            })
        });


        win.layout_(
            VLayout(
                *(
                    4.collect({ |i|
                        HLayout(
                            dragSinks[i],
                            NS_ControlFader(controls[i * 2])
                            .showLabel_(false).round_(1), 
                            assignButtons[i * 2],
                            NS_ControlButton(controls[i * 2 + 1], [
                                ["M", NS_Style.muteRed, NS_Style.bGroundDark],
                                [NS_Style.play,NS_Style.playGreen, NS_Style.bGroundDark]
                            ]).maxWidth_(30),
                            assignButtons[i * 2 + 1]
                        )
                    }) 
                    ++
                    [
                        HLayout( 
                            NS_ControlFader(controls[8]).round_(1),
                            assignButtons[8],
                            NS_ControlButton(controls[9], [
                                ["M", NS_Style.muteRed, NS_Style.bGroundDark],
                                [NS_Style.play,NS_Style.playGreen, NS_Style.bGroundDark]
                            ]).maxWidth_(30),
                            assignButtons[9]
                        )
                    ]
                )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    saveExtra { |saveArray|
        var busArray = dragSinks.collect(_.object);
        saveArray.add( busArray );
        ^saveArray
    }

    loadExtra { |loadArray|
        loadArray.do({ |bus, i|
            if(bus.notNil,{
                var sink = dragSinks[i];
                sink.object_(bus);
                sink.align_(\left).string_("in:" + bus.asString);
                synths[0].set(("inBus" ++ i).asSymbol, NSFW.servers[strip.group.server.name].inputBusses[bus] )
            })
        })
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Panel({ OSC_Fader(false, false) } ! 5, columns: 5),
            OSC_Panel({ OSC_Button() } ! 5, columns: 5, height: "20%")
        ], randCol:true).oscString("MultiIn")
    }
}
