NS_MultiIn : NS_SynthModule {
    classvar <isSource = true;
    var dragSinks;

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

                sig = sig.sum; // * (1 - \allMute.kr(0));
                                
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
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

            controls[index * 2 + 1] = NS_Control("mute" ++ index, \db)
            .addAction(\synth, { |c| synths[0].set(("mute" ++ index).asSymbol, c.value) });
            assignButtons[index * 2 + 1] = NS_AssignButton(this, index * 2 + 1, \fader).maxWidth_(30);
        });

        controls[8] = NS_Control(\amp, \db, 0)
        .addAction(\synth, { |c| synths[0].set(\amp, c.value.dbamp) });
        assignButtons[8] = NS_AssignButton(this, 8, \fader).maxWidth_(30);

        controls[9] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| /*strip.inSynthGate_(c.value);*/ synths[0].set(\mute, c.value) });
        assignButtons[9] = NS_AssignButton(this, 9, \button).maxWidth_(30);


        // should these have wee red X boxes to remove the bus?
        dragSinks = 4.collect({ |index|
            DragSink()
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
        });

        win.layout_(
            VLayout(
                HLayout( 
                    dragSinks[0],
                    NS_ControlFader(controls[0]).showLabel_(false).round_(1), assignButtons[0],
                    NS_ControlButton(controls[1], ["M","▶"]).maxWidth_(30), assignButtons[1]
                ),
                HLayout( 
                    dragSinks[1],
                    NS_ControlFader(controls[2]).showLabel_(false).round_(1), assignButtons[2],
                    NS_ControlButton(controls[3], ["M","▶"]).maxWidth_(30), assignButtons[3]
                ),
                HLayout( 
                    dragSinks[2],
                    NS_ControlFader(controls[4]).showLabel_(false).round_(1), assignButtons[4],
                    NS_ControlButton(controls[5], ["M","▶"]).maxWidth_(30), assignButtons[5]
                ),
                HLayout( 
                    dragSinks[3],
                    NS_ControlFader(controls[6]).showLabel_(false).round_(1), assignButtons[6],
                    NS_ControlButton(controls[7], ["M","▶"]).maxWidth_(30), assignButtons[7]
                ),
                HLayout( 
                    NS_ControlFader(controls[8]).round_(1), assignButtons[8],
                    NS_ControlButton(controls[9], ["▶","bypass"]).maxWidth_(64), assignButtons[9]
                )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    saveExtra { |saveArray|
        var busArray = 4.collect({ |i| dragSinks[i].object });
        saveArray.add( busArray );
        ^saveArray
    }

    loadExtra { |loadArray|
        loadArray.do({ |bus, i|
            if(bus.notNil,{
                var sink = dragSinks[i];
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
