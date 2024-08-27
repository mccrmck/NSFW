NS_ChannelStrip : NS_SynthModule {
    classvar numSlots = 5;
    var <>pageIndex, <>stripIndex;
    var <stripBus;
    var stripGroup, <inGroup, slots, <slotGroups, <faderGroup;
    var <inSink, <inSynth, <fader;
    var <inSynthGate = 0;
    var <moduleSinks, <view;
    var <paused = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_stripFader,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0,0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));

                ReplaceOut.ar(\bus.kr, sig)
            }).add;

            SynthDef(\ns_stripIn,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                sig = sig * \thru.kr(0);
                ReplaceOut.ar(\outBus.kr,sig);
            }).add;

            SynthDef(\ns_stripSend,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
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

        stripBus   = Bus.audio(modGroup.server,NSFW.numOutChans);

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
            NS_ModuleSink(this, slotIndex)
        });

        controls.add(
            NS_Fader(nil,\amp,{ |f| fader.set(\amp, f.value) },'horz').maxHeight_(45)
        );
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                fader.set(\mute,but.value)
            })
        );
        assignButtons[1] = NS_AssignButton(this,1,\button);

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
                VLayout( *([inSink] ++ moduleSinks) ),
                HLayout( controls[0], assignButtons[0] ),
                HLayout( 
                    Button()
                    .states_([["S", Color.black, Color.yellow]])
                    .action_({ |but|
                        moduleSinks.do({ |sink| 
                            if( sink.module.notNil,{ sink.module.toggleVisible });
                        });
                        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.toggleVisible })
                    }),
                    controls[1],
                    assignButtons[1]
                ),
                HLayout( controls[2], controls[3], controls[4], controls[5] )
            )
        );

        controls[2].valueAction_(1);
        view.layout.spacing_(0).margins_(0);
    }

    asView { ^view }

    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }

    moduleStrings { ^this.moduleArray.collect({ |mod| mod.class.asString.split($_)[1] }) }

    free {
        inSink.free;
        synths.do(_.free);
        synths = Array.newClear(4);
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
        inSynthGate = inSynthGate + val.linlin(0,1,-1,1);
        inSynthGate.postln;
        
        // these two lines need to be reassessed...
        inSynthGate = inSynthGate.max(0);
        inSynth.set( \thru, inSynthGate.sign )
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
        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.pause });
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
        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.unpause });
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

        stripBus   = Bus.audio(modGroup.server,NSFW.numOutChans);

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
            NS_ModuleSink(this, slotIndex)
        });

        label = StaticText().align_(\center).stringColor_(Color.white);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                this.fader.set(\mute, but.value)
            })
        );
        assignButtons[0] = NS_AssignButton(this,0,\button).maxWidth_(45);

        controls.add(
            NS_Fader(nil,\db,{ |f| fader.set(\amp, f.value.dbamp) }).maxWidth_(45),
        );
        assignButtons[1] = NS_AssignButton(this,1,\fader).maxWidth_(45);

        view = View().layout_(
            VLayout(
                label,
                HLayout(
                    VLayout(
                        *(moduleSinks ++ [
                            HLayout(
                                PopUpMenu()
                                .items_( (0..(modGroup.server.options.numOutputBusChannels - NSFW.numOutChans)) )
                                .value_(0)
                                .action_({ |menu|
                                    synths[0].set(\outBus, menu.value)
                                }),
                                Button()
                                .maxWidth_(45)
                                .states_([["S", Color.black, Color.yellow]])
                                .action_({ |but|
                                    moduleSinks.do({ |sink| 
                                        var mod = sink.module;
                                        if(mod.notNil,{ mod.toggleVisible });
                                    })
                                }),
                                controls[0],
                                assignButtons[0]
                            )]
                        )
                    ),
                    VLayout( controls[1], assignButtons[1] ),
                )
            ),
        );

        view.layout.spacing_(0).margins_(0);
    }

    setLabel { |text| label.string_( text.asString ) }

    asView { ^view }

    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }


    inSynthGate_ { |val| /* if this is not here, the language crashes... */ }

    free {
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

NS_InChannelStrip : NS_SynthModule {   // this is not yet compatible when numServers > 1
    classvar numSlots = 3;
    var <stripBus;
    var localResponder;
    var stripGroup, <inGroup, slots, <slotGroups, <sendGroup;
    var <inSynth, <fader;
    var modules;
    var <inSynthGate = 0;
    var <inSink, <moduleSinks, <rms, <view;
    var <send;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_inputMono,{
                var numChans = NSFW.numOutChans;
                var sig = SoundIn.ar(\inBus.kr());

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\inAmp.kr(0));
                SendPeakRMS.ar(sig,10,3,'/inSynth',0);

                Out.ar(\outBus.kr, sig ! numChans )
            }).add;

            SynthDef(\ns_inputStereo,{
                var numChans = NSFW.numOutChans;
                var inBus = \inBus.kr();
                var sig = SoundIn.ar([inBus,inBus + 1]);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\inAmp.kr(0));
                SendPeakRMS.ar(sig.sum * -3.dbamp,10,3,'/inSynth',0);

                Out.ar(\outBus.kr, sig ! numChans )
            }).add;
        }
    }

    // this new, init, makeView function is fucking wack, please fix!
    *new { |group, inbusIndex| 
        ^super.new( group, inbusIndex )
    }

    init {
        this.initModuleArrays(2);
        synths = Array.newClear(4);
        modules = Array.newClear(3);

        stripGroup = Group(modGroup,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        slots      = Group(stripGroup,\addToTail);
        slotGroups = numSlots.collect({ |i| Group(slots,\addToTail) });
        sendGroup  = Group(stripGroup,\addToTail);

        inSynth = Synth(\ns_inputMono,[\inBus,bus,\outBus,NS_ServerHub.servers[modGroup.server.name].inputBusses[bus]],inGroup);

        localResponder.free;
        localResponder = OSCFunc({ |msg|

            if( msg[2] == 0,{
                { 
                    rms.value = msg[4].ampdb.linlin(-80, 0, 0, 1);
                    rms.peakLevel = msg[3].ampdb.linlin(-80, 0, 0, 1,\min)
                }.defer
            })
        }, '/inSynth', argTemplate: [inSynth.nodeID]);

        this.makeView
    }

    makeView {

        inSink = TextField()
        .maxWidth_(135)
        .align_(\center)
        .object_( bus.asString )
        .string_( bus.asString )
        .beginDragAction_({ bus.asInteger })
        .mouseDownAction_({ |v| v.beginDrag });

        moduleSinks = 3.collect({ |slotIndex| 
            HLayout(
                DragBoth()
                .maxWidth_(150)
                .align_(\left).background_(Color.white)
                .receiveDragHandler_({ |drag|
                    var moduleString = View.currentDrag;
                    var className = ("NS_" ++ moduleString).asSymbol.asClass;
                    if( className.respondsTo('isSource'),{ 
                        if(modules[slotIndex].notNil,{ modules[slotIndex].free });
                        drag.object_(moduleString);
                        drag.string_(moduleString);
                        modules[slotIndex] = className.new(slotGroups[slotIndex], NS_ServerHub.servers[modGroup.server.name].inputBusses[bus], this);
                    })
                }),
                Button().maxHeight_(25).maxWidth_(15)
                .states_([["S", Color.black, Color.yellow]])
                .action_({ |but|
                    if(modules[slotIndex].notNil,{ modules[slotIndex].toggleVisible })
                }),
                Button().maxHeight_(25).maxWidth_(15)
                .states_([["X", Color.black, Color.red]])
                .action_({ |but|
                    modules[slotIndex].free;
                    modules[slotIndex] = nil;
                   // modSink.object_( nil );
                   // modSink.string_("");
                }),
            )
        });

        controls.add(
            Button()
            .maxWidth_(60)
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                
            })
        );
        assignButtons[0] = NS_AssignButton(this,0,\button).maxWidth_(60);

        controls.add(
            NS_Fader(nil,\db,{ |f| inSynth.set(\inAmp, f.value.dbamp ) },'horz').maxWidth_(135),
        );
        assignButtons[1] = NS_AssignButton(this,1,\fader).maxWidth_(45);

        4.do({ |outMixerChannel|
            controls.add(
                Button()
                .maxWidth_(45)
                .states_([[outMixerChannel,Color.white,Color.black],[outMixerChannel, Color.cyan, Color.black]])
                .action_({ |but|
                    var outSend = synths[outMixerChannel];
                    if(but.value == 0,{
                        if(outSend.notNil,{ outSend.set(\gate,0) });
                        synths.put(outMixerChannel, nil);
                    },{
                        var inputBus = NS_ServerHub.servers[modGroup.server.name].inputBusses[bus];
                        var mixerBus = NS_ServerHub.servers[modGroup.server.name].outMixerBusses;
                        synths.put(outMixerChannel, Synth(\ns_stripSend,[\inBus,inputBus,\outBus,mixerBus[outMixerChannel]],sendGroup,\addToTail) )
                    })
                })            
            )
        });

        rms = LevelIndicator()
        .minWidth_(15).maxWidth_(20).style_(\led).numTicks_(11).numMajorTicks_(3)
        .stepWidth_(2).drawsPeak_(true).warning_(0.9).critical_(1.0);

        view = View()
        .background_( Color.white.alpha_(0.15) )
        .layout_(
            HLayout(
                VLayout(
                    HLayout(
                        inSink,
                        Button()
                        .maxWidth_(45)
                        .states_([["mono",Color.black, Color.white],["stereo", Color.white, Color.black]])
                        .action_({ |but| })
                    ),
                    moduleSinks[0],
                    moduleSinks[1],
                    moduleSinks[2],
                    HLayout( controls[1], assignButtons[1] ),
                    HLayout(
                        Button()
                        .maxWidth_(60)
                        .states_([["S", Color.black, Color.yellow]])
                        .action_({ |but|
                            moduleSinks.do({ |sink| 
                                var mod = sink.module;
                                if(mod.notNil,{ mod.toggleVisible });
                            })
                        }),
                        controls[0],
                        assignButtons[0]
                    ),
                    HLayout( *controls[2..5] )
                ),
                rms
            )
        );

        view.layout.spacing_(0).margins_(0);
    }

    asView { ^view }

    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }

    setSynths { }


    inSynthGate_ { |val| /* if this is not here, the language crashes... */ }

    free {
        
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
