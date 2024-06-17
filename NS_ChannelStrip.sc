NS_ChannelStrip : NS_SynthModule {
    classvar numSlots = 5;
    var <stripBus;
    var stripGroup, <inGroup, slots, <slotGroups, <faderGroup;
    var <inSynth, <inModule, <fader;
    var <inSynthGate = 0;
    var <moduleSinks, <view;
    var <paused = false;

    *initClass {
        StartUp.add{
            SynthDef(\ns_stripFader,{
                var sig = In.ar(\bus.kr, 2);
                var mute = 1 - \mute.kr(0); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(0));

                ReplaceOut.ar(\bus.kr, sig)
            }).add;

            SynthDef(\ns_stripIn,{
                var sig = In.ar(\inBus.kr,2);
                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                sig = sig * \thru.kr(0);
                ReplaceOut.ar(\outBus.kr,sig);
            }).add;

            SynthDef(\ns_stripSend,{
                var sig = In.ar(\inBus.kr,2);
                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                Out.ar(\outBus.kr,sig);
            }).add
        }
    }

    *new { |group, outBus| 
        ^super.new(group, outBus)
    }

    init {
        this.initModuleArrays(2);
        synths = Array.newClear(4);

        stripBus   = Bus.audio(modGroup.server,2);

        stripGroup = Group(modGroup,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        slots      = Group(stripGroup,\addToTail);
        slotGroups = numSlots.collect({ |i| Group(slots,\addToTail) });
        faderGroup = Group(stripGroup,\addToTail);

        inSynth = Synth(\ns_stripIn,[\inBus,stripBus,\outBus,stripBus],inGroup);
        fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);

        this.makeView;
    }

    makeView {
        var inSink = DragBoth().string_("in")
        .align_(\center)
        .receiveDragHandler_({ |drag|
            var dragObject = View.currentDrag[0];
            var className  = ("NS_" ++ dragObject).asSymbol.asClass;

            if(className.respondsTo('isSource'),{
                if(className.isSource == true,{
                    if(inModule.notNil,{ inModule.free });
                    drag.object_(View.currentDrag);
                    drag.string = "in:" + dragObject.asString;
                    inModule = className.new( inGroup, stripBus ).linkStrip(this)
                })
            },{
                if(dragObject.interpret.isInteger,{
                    if(inModule.notNil,{ inModule.free }); 
                    drag.object_(View.currentDrag);
                    drag.string = "in:" + dragObject.asString;
                    inSynth.set(\inBus,View.currentDrag[1])
                })
            })
        });
        //var sends = Array.newClear(4);
        var sendButtons = 4.collect({ |outChannel|
            Button()
            .states_([[outChannel,Color.white,Color.black],[outChannel, Color.cyan, Color.black]])
            .action_({ |but|
                var outSend = synths[outChannel];
                if(but.value == 0,{
                    if(outSend.notNil,{ outSend.set(\gate,0) });
                    synths.put(outChannel, nil);
                },{
                    var mixerBus = NSFW.servers[modGroup.server.name].outMixerBusses; // binds this object to NS_Server...is that okay?
                    synths.put(outChannel, Synth(\ns_stripSend,[\inBus,stripBus,\outBus,mixerBus[outChannel]],faderGroup,\addToTail) )
                })
            })
        });

        moduleSinks = slotGroups.collect({ |slotGroup| 
            NS_ModuleSink().moduleAssign_(slotGroup, stripBus, this)
        });

        controls.add(
            NS_Fader(nil, nil,\amp,{ |f| fader.set(\amp, f.value) }).maxHeight_(190),
        );
        assignButtons[0] = NS_AssignButton().setAction(this,0,\fader);

        controls.add(
            Button()
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                if(but.value == 0,{
                    fader.set(\mute,0)
                },{ 
                    fader.set(\mute,1)
                })
            })
        );
        assignButtons[1] = NS_AssignButton().setAction(this,1,\button);

        view = View().layout_(
            VLayout(
                HLayout(
                    inSink,
                    Button().maxHeight_(45).maxWidth_(15)
                    .states_([["S", Color.black, Color.yellow]])
                    .action_({ |but|
                        if(inModule.notNil,{ inModule.toggleVisible })
                    }),
                    Button().maxWidth_(15).states_([["X", Color.black, Color.red]])
                    .action_({ |but|
                        inSynth.set(\inBus,stripBus);
                        inModule.free;
                        inModule = nil;
                        inSink.string_("in")
                    })
                ).margins_([0,4]),
                VLayout( *moduleSinks ),
                Button()
                .states_([["S", Color.black, Color.yellow]])
                .action_({ |but|
                    moduleSinks.do({ |sink| 
                        var mod = sink.module;
                        if(mod.notNil,{ mod.toggleVisible });
                    });
                    if(inModule.notNil,{ inModule.toggleVisible })
                }),
                controls[0],
                assignButtons[0],
                HLayout( controls[1], assignButtons[1] ),
                HLayout( *sendButtons )
            )
        );

        sendButtons[0].valueAction_(1);
        view.layout.spacing_(0).margins_(2);
    }

    asView { ^view }
    
    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }

    free {}

    amp  { this.fader.get(\amp,{ |a| a.postln }) }
    amp_ { |amp| this.fader.set(\amp, amp) }

    toggleMute {
        this.fader.get(\mute,{ |muted|
            this.fader.set(\mute,1 - muted)
        })
    }

    inSynthGate_ { |val|
        var increment = val.linlin(0,1,-1,1);
        inSynthGate = inSynthGate + increment;

        inSynth.set(\thru,inSynthGate)
    }

    outBus_ { |newBus|
        bus = newBus;
        fader.set(\outBus,newBus)
    }

    pause {
        inSynth.set(\pauseGate, 0);
        if(inModule.notNil,{ inModule.pause });
        this.moduleArray.do({ |mod| 
            if(mod.notNil,{ mod.pause })
        });
        fader.set(\pauseGate, 0);
        synths.do({ |snd|
            if(snd.notNil,{ snd.set(\pauseGate, 0) })
        });
        stripGroup.run(false);
        paused = true;
    }

    unpause {
        inSynth.set(\pauseGate, 1);
        inSynth.run(true);
        if(inModule.notNil,{ inModule.unpause });
        this.moduleArray.do({ |mod| 
            if(mod.notNil,{ mod.unpause })
        });
        fader.set(\pauseGate, 1);
        fader.run(true);
        synths.do({ |snd|
            if(snd.notNil,{ snd.set(\pauseGate, 1); snd.run(true) })
        });
        stripGroup.run(true);
        paused = false;
    }
}

NS_OutChannelStrip : NS_ChannelStrip {
    classvar numSlots = 4;
    var <label, <send;

    makeView {

        moduleSinks = numSlots.collect({ |slotIndex| 
            NS_ModuleSink().moduleAssign_(slotGroups[slotIndex],stripBus, this)
        });

        label = StaticText().align_(\center).stringColor_(Color.white);

        controls.add(
            Button()
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                if(but.value == 0,{
                    this.fader.set(\mute,0)
                },{ 
                    this.fader.set(\mute,1)
                })
            })
        );
        assignButtons[0] = NS_AssignButton().setAction(this,0,\button);

        controls.add(
            NS_Fader(nil, nil,\db,{ |f| fader.set(\amp, f.value.dbamp) }).maxWidth_(45),
        );
        assignButtons[1] = NS_AssignButton().maxWidth_(45).setAction(this,1,\fader);

        view = View().layout_(
            VLayout(
                HLayout(
                    VLayout(
                        label,
                        VLayout( *moduleSinks ),
                        HLayout(
                            PopUpMenu()
                            .items_(["0-1","2-3","4-5","6-7"])
                            .value_(0)
                            .action_({ |menu|
                                this.outBus_( menu.value * 2 )
                            }),
                            Button()
                            .states_([["S", Color.black, Color.yellow]])
                            .action_({ |but|
                                moduleSinks.do({ |sink| 
                                    var mod = sink.module;
                                    if(mod.notNil,{ mod.toggleVisible });
                                })
                            })
                        ),
                        HLayout( controls[0], assignButtons[0] ),
                    ),
                    VLayout( controls[1], assignButtons[1] )
                )
            )
        );

        //inModule = Synth(\ns_stripIn,[\inBus,stripBus,\outBus,stripBus],inGroup);
        inSynth.set(\thru,1);
        send = Synth(\ns_stripSend,[\inBus,stripBus,\outBus,bus],faderGroup,\addToTail);
        slotGroups.removeAt(slotGroups.size - 1).free;   // this feels sloppy, but inheritance is not serving me well..
        view.layout.spacing_(0).margins_([2,0]);
    }

    setLabel { |text| label.string_( text.asString ) }
}
