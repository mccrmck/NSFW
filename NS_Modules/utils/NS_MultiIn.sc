NS_MultiIn : NS_SynthModule {
    classvar <isSource = true;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_multiIn,{
                var numChans = NSFW.numChans;
                var sig = 4.collect({ |i|
                    var name = "inBus" ++ i;
                    In.ar(NamedControl.ar(name.asSymbol),numChans)
                    * NamedControl.kr(("amp" ++ i).asSymbol,0)
                    * (1 - NamedControl.kr(("mute" ++ i).asSymbol,0))
                });

                sig = sig.sum;
                                
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(14);
        this.makeWindow("MultiIn", Rect(0,0,240,270));

        synths.add( Synth(\ns_multiIn,[\bus,bus],modGroup) );

        controls.add(
            NS_Fader("amp",\db,{ |f| synths[0].set(\amp, f.value.dbamp)},initVal: 0).maxWidth_(45),
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

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
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(45);

        4.do({ |index|
            
            controls.add(
                NS_Fader("amp",\db,{ |f| synths[0].set(("amp" ++ index).asSymbol, f.value.dbamp)}).maxWidth_(45)
            );
            assignButtons[index * 3 + 2] = NS_AssignButton(this, index * 3 + 2, \fader).maxWidth_(45);

            controls.add(
                Button()
                .maxWidth_(45)
                .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
                .action_({ |but|
                    synths[0].set(("mute" ++ index).asSymbol,but.value)
                })
            );
            assignButtons[index * 3 + 3] = NS_AssignButton(this, index * 3 + 3, \button).maxWidth_(45);

            controls.add(
                DragSink()
                .maxWidth_(45)
                .align_(\center).background_(Color.white).string_("in")
                .receiveDragHandler_({ |drag|
                    var dragObject = View.currentDrag;

                    if(dragObject.isInteger and: {dragObject < NSFW.numInBusses},{
                        
                        drag.object_(dragObject);
                        drag.align_(\left).string_("in:" + dragObject.asString);
                        synths[0].set( 
                            ("inBus" ++ index).asSymbol, 
                            NS_ServerHub.servers[strip.modGroup.server.name].inputBusses[dragObject]
                        )
                    },{
                        "drag Object not valid".warn
                    })
                })
            )
        });

        win.layout_(
            HLayout(
                HLayout(
                    *4.collect({ |i|
                        VLayout( 
                            controls[i * 3 + 4],
                            controls[i * 3 + 2], 
                            assignButtons[i * 3 + 2],
                            controls[i * 3 + 3],
                            assignButtons[i * 3 + 3] 
                        )
                    })
                ),
                VLayout( controls[0], assignButtons[0], controls[1], assignButtons[1]),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    saveExtra { |saveArray|
        var busArray = 4.collect({ |i| controls[i * 3 + 4].object });
        saveArray.add( busArray );
        ^saveArray
    }

    loadExtra { |loadArray|
        loadArray.do({ |bus, i|
            if(bus.notNil,{
                var sink = controls[i * 3 + 4];
                sink.object_(bus);
                sink.align_(\left).string_("in:" + bus.asString);
                synths[0].set(("inBus" ++ i).asSymbol, NS_ServerHub.servers[strip.modGroup.server.name].inputBusses[bus] )
            })
        })
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false, widgetArray: [
            OSC_Panel(widgetArray: { OSC_Fader() } ! 5),
            OSC_Panel(height: "20%", widgetArray: { OSC_Button() } ! 5),
        ],randCol:true).oscString("MultiIn")
    }
}
