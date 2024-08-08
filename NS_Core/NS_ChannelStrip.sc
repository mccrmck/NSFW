NS_ChannelStrip : NS_SynthModule {
    classvar numSlots = 4;
    var <>pageIndex, <>stripIndex;
    var <stripBus;
    var stripGroup, <inGroup, slots, <slotGroups, <faderGroup;
    var <inSink, <inSynth, <fader;
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

    *new { |group, outBus, pgIndex, strIndex| 

        ^super.new(group, outBus).pageIndex_(pgIndex).stripIndex_(strIndex)
    }

    init {
        this.initModuleArrays(6);
        synths     = Array.newClear(4);

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
        inSink = NS_InModuleSink(this);
        moduleSinks = slotGroups.collect({ |slotGroup, slotIndex| 
            NS_ModuleSink(this).onReceiveDrag(slotGroup, slotIndex)
        });

        controls.add(
            NS_Fader(nil,\amp,{ |f| fader.set(\amp, f.value) }).maxHeight_(190),
        );
        assignButtons[0] = NS_AssignButton().setAction(this,0,\fader);

        controls.add(
            Button()
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                fader.set(\mute,but.value)
            })
        );
        assignButtons[1] = NS_AssignButton().setAction(this,1,\button);

        4.do({ |outChannel|
            controls.add(
                Button()
                .states_([[outChannel,Color.white,Color.black],[outChannel, Color.cyan, Color.black]])
                .action_({ |but|
                    var outSend = synths[outChannel];
                    if(but.value == 0,{
                        if(outSend.notNil,{ outSend.set(\gate,0) });
                        synths.put(outChannel, nil);
                    },{
                        var mixerBus = NS_ServerHub.servers[modGroup.server.name].outMixerBusses;
                        synths.put(outChannel, Synth(\ns_stripSend,[\inBus,stripBus,\outBus,mixerBus[outChannel]],faderGroup,\addToTail) )
                    })
                })
            )
        });

        view = View().layout_(
            VLayout(
                inSink,
                VLayout( *moduleSinks ),
                Button()
                .states_([["S", Color.black, Color.yellow]])
                .action_({ |but|
                    moduleSinks.do({ |sink| 
                        if( sink.module.notNil,{ sink.module.toggleVisible });
                    });
                    if(inSink.module.notNil,{ inSink.module.toggleVisible })
                }),
                controls[0],
                assignButtons[0],
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], controls[3], controls[4], controls[5] )
            )
        );

        controls[2].valueAction_(1);
        view.layout.spacing_(0).margins_(2);
    }

    asView { ^view }

    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }

    moduleStrings { ^this.moduleArray.collect({ |mod| mod.class.asString.split($_)[1] }) }

    free {
        inSink.free;
        moduleSinks.do({ |sink| sink.free });
        this.amp_(0)
    }

    amp  { this.fader.get(\amp,{ |a| a.postln }) }
    amp_ { |amp| this.fader.set(\amp, amp) }

    toggleMute {
        this.fader.get(\mute,{ |muted|
            this.fader.set(\mute,1 - muted)
        })
    }

    inSynthGate_ { |val|
        if(val == 1,{
            inSynthGate = inSynthGate + 1;
        },{
            inSynthGate = inSynthGate - 1;
        });

        inSynthGate = inSynthGate.max(0);

        inSynth.set( \thru, inSynthGate.clip(0,1) )
    }

    saveExtra { |saveArray|
        var stripArray = List.newClear(0);
        var inSinkArray = if(inSink.module.notNil,{ inSink.save }); 
        var sinkArray = moduleSinks.collect({ |sink|
            if(sink.module.notNil,{
                sink.save
            })
        });
        stripArray.add( inSinkArray );
        stripArray.add( sinkArray );

        saveArray.add(stripArray);

        ^saveArray
    }

    loadExtra { |loadArray|
        if(loadArray[0].notNil,{ inSink.load( loadArray[0] ) });

        loadArray[1].do({ |sinkArray, index|
            if(sinkArray.notNil,{
                moduleSinks[index].load(sinkArray, slotGroups[index])
            })
        })
    }

    pause {
        inSynth.set(\pauseGate, 0);
        if(inSink.module.notNil,{ inSink.module.pause });
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
        if(inSink.module.notNil,{ inSink.module.unpause });
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

NS_OutChannelStrip : NS_SynthModule {
    classvar numSlots = 4;
    var <stripBus;
    var stripGroup, <inGroup, slots, <slotGroups, <faderGroup;
    var <inSynth, <fader;
    var <inSynthGate = 0;
    var <moduleSinks, <view;
    var <label, <send;

    *new { |group, outBus| 
        ^super.new(group, outBus)
    }

    init {
        this.initModuleArrays(2);

        stripBus   = Bus.audio(modGroup.server,2);

        stripGroup = Group(modGroup,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        slots      = Group(stripGroup,\addToTail);
        slotGroups = numSlots.collect({ |i| Group(slots,\addToTail) });
        faderGroup = Group(stripGroup,\addToTail);

        inSynth = Synth(\ns_stripIn,[\inBus,stripBus,\thru,1,\outBus,stripBus],inGroup);
        synths.add( Synth(\ns_stripSend,[\inBus,stripBus,\outBus,bus],faderGroup,\addToTail) );
        fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);

        this.makeView;
    }

    makeView {

        moduleSinks = slotGroups.collect({ |slotGroup, slotIndex| 
            NS_ModuleSink(this).onReceiveDrag( slotGroup, slotIndex )
        });

        label = StaticText().align_(\center).stringColor_(Color.white);

        controls.add(
            Button()
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                this.fader.set(\mute, but.value)
            })
        );
        assignButtons[0] = NS_AssignButton().setAction(this,0,\button);

        controls.add(
            NS_Fader(nil,\db,{ |f| fader.set(\amp, f.value.dbamp) }).maxWidth_(45),
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
                                synths[0].set(\outBus, menu.value * 2)
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

        view.layout.spacing_(0).margins_([2,0]);
    }

    setLabel { |text| label.string_( text.asString ) }

    asView { ^view }

    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }


    inSynthGate_ { |val| /* if this is not here, the language crashes... */ }

    free {
        //moduleSinks.do({ |sink| sink.free }); // noPageIndex for clearing
        this.amp_(0)
    }

    amp  { this.fader.get(\amp,{ |a| a.postln }) }
    amp_ { |amp| this.fader.set(\amp, amp) }

    toggleMute {
        this.fader.get(\mute,{ |muted|
            this.fader.set(\mute,1 - muted)
        })
    }

    saveExtra { |saveArray|
        var sinkArray = moduleSinks.collect({ |sink|
            if(sink.module.notNil,{
                sink.save
            })
        });

        saveArray.add(sinkArray);

        ^saveArray
    }

    loadExtra { |loadArray|
        loadArray.do({ |sinkArray, index|
            if(sinkArray.notNil,{
                moduleSinks[index].load(sinkArray, slotGroups[index])
            })
        })
    }
}
