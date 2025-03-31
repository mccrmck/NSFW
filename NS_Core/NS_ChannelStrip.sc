//NS_ChannelStrip : NS_ControlModule {
//    classvar numSlots = 5;
//    var <>group, <>outBus, <>pageIndex, <>stripIndex; // do these need setters?
//    var <synths, <stripBus;
//    var stripGroup, <inGroup, slots, <slotGroups, <faderGroup;
//    var <inSink, <inSynth, <fader;
//    var <inSynthGate = 0;
//    var <moduleSinks, <view;
//    var <>paused = false;
//
//    *initClass {
//        ServerBoot.add{
//            SynthDef(\ns_stripIn,{
//                var numChans = NSFW.numChans;
//                var sig = In.ar(\inBus.kr,numChans);
//                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
//                sig = sig * \thru.kr(0);
//                ReplaceOut.ar(\outBus.kr,sig);
//            }).add;
//
//            SynthDef(\ns_stripFader,{
//                var numChans = NSFW.numChans;
//                var sig = In.ar(\bus.kr, numChans);
//                var mute = 1 - \mute.kr(0,0.01); 
//                sig = ReplaceBadValues.ar(sig);
//                sig = sig * mute;
//                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));
//
//                ReplaceOut.ar(\bus.kr, sig)
//            }).add;
//
//            SynthDef(\ns_stripSend,{
//                var numChans = NSFW.numChans;
//                var sig = In.ar(\inBus.kr,numChans);
//                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
//                Out.ar(\outBus.kr,sig);
//            }).add
//        }
//    }
//
//    *new { |inGroup, sendBus, pgIndex, strIndex| 
//        ^super.new.group_(inGroup).outBus_(sendBus).pageIndex_(pgIndex).stripIndex_(strIndex).init
//    }
//
//    init {
//        this.initControlArrays(6);
//        synths     = Array.newClear(4);
//
//        stripBus   = Bus.audio(group.server, NSFW.numChans);
//
//        stripGroup = Group(group,\addToTail);
//        inGroup    = Group(stripGroup,\addToTail);
//        slots      = Group(stripGroup,\addToTail);
//        slotGroups = numSlots.collect({ |i| Group(slots,\addToTail) });
//        faderGroup = Group(stripGroup,\addToTail);
//
//        inSynth = Synth(\ns_stripIn,[\inBus,stripBus,\outBus,stripBus],inGroup);
//        fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);
//
//        this.makeView;
//    }
//
//    makeView {
//        inSink = NS_InModuleSink(this);
//        moduleSinks = slotGroups.collect({ |slotGroup, slotIndex| 
//            NS_ModuleSink(this, slotIndex)
//        });
//
//        controls[0] = NS_Control(\amp,\db)
//        .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) });
//        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(45);
//
//        controls[1] = NS_Control(\mute,ControlSpec(0,1,'lin',1))
//        .addAction(\synth,{ |c| fader.set(\mute, c.value) });
//        assignButtons[1] = NS_AssignButton(this,1,\button);
//
//        4.do({ |outChannel|
//            controls[2 + outChannel] = NS_Control(("send" ++ outChannel).asSymbol,ControlSpec(0,1,'lin',1),[1,0,0,0].at(outChannel))
//            .addAction(\send,{ |c|
//                var outSend = synths[outChannel];
//                if(c.value == 0,{
//                    if(outSend.notNil,{ outSend.set(\gate,0) });
//                    synths.put(outChannel, nil);
//                },{
//                    var mixerBus = NSFW.servers[group.server.name].outMixerBusses;
//                    synths.put(outChannel, Synth(\ns_stripSend,[\inBus,stripBus,\outBus,mixerBus[outChannel]],faderGroup,\addToTail) )
//                })
//            });
//        });
//
//        view = UserView().layout_(
//            VLayout(
//                VLayout( *([inSink] ++ moduleSinks) ),
//                HLayout( NS_ControlFader(controls[0]).round_(1).showLabel_(false), assignButtons[0] ),
//                HLayout( 
//                    Button()
//                    .states_([["S", Color.black, Color.yellow]])
//                    .action_({ this.toggleAllVisible }),
//                    NS_ControlButton(controls[1], [
//                        ["M",NS_Style.muteRed,NS_Style.textDark],
//                        [NS_Style.play, NS_Style.playGreen, NS_Style.bGroundDark]
//                    ]),
//                    assignButtons[1]
//                ),
//                HLayout(
//                    *4.collect({ |i| 
//                        NS_ControlButton(controls[i+2], [
//                            [i, Color.white,Color.black],
//                            [i, Color.cyan, Color.black]
//                        ])
//                    })
//                )
//            )
//        );
//
//        view.layout.spacing_(NS_Style.stripSpacing).margins_(NS_Style.stripMargins);
//    }
//
//    asView { ^view }
//
//    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }
//
//    moduleStrings { ^this.moduleArray.collect({ |mod| mod.class.asString.split($_)[1] }) }
//
//    free {
//        inSink.free;
//        synths.do(_.free);
//        synths = Array.newClear(4);
//        this.setInSynthGate(0);
//        moduleSinks.do({ |sink| sink.free });
//        this.amp_(0)
//    }
//
//    amp  { ^controls[0].normValue }
//    amp_ { |amp| controls[0].normValue_(amp) }
//
//    toggleMute {
//        controls[1].value_( 1 - controls[1].value )
//    }
//
//    toggleAllVisible {
//        this.moduleArray.do({ |mod| if( mod.notNil,{ mod.toggleVisible }) });
//        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.toggleVisible })
//    }
//
//
//    // these two methods need to be reassessed...
//    setInSynthGate { |val| inSynthGate = val }
//
//    inSynthGate_ { |val|
//        inSynthGate = inSynthGate + val.linlin(0,1,-1,1);
//
//        inSynthGate.postln;
//
//        inSynthGate = inSynthGate.max(0);
//        inSynth.set( \thru, inSynthGate.sign )
//    }
//
//
//
//    saveExtra { |saveArray|
//        var stripArray = List.newClear(0);
//        var inSinkArray = if(inSink.module.notNil,{ inSink.save }); 
//        var sinkArray = moduleSinks.collect({ |sink|
//            if(sink.module.notNil,{ sink.save })
//        });
//        stripArray.add( inSinkArray );
//        stripArray.add( sinkArray );
//        stripArray.add( inSynthGate );
//
//        saveArray.add(stripArray);
//
//        ^saveArray
//    }
//
//    loadExtra { |loadArray|
//        var cond = CondVar();
//        var count = 0;
//
//        {
//            if(loadArray[0].notNil,{ inSink.load( loadArray[0] ) });
//
//            loadArray[1].do({ |sinkArray, index|
//                if(sinkArray.notNil,{
//                    moduleSinks[index].load(sinkArray, slotGroups[index])
//                });
//                count = count + 1
//            });
//            cond.wait( count == loadArray[1].size );
//
//            this.setInSynthGate( loadArray[2] );
//            inSynth.set( \thru, inSynthGate.sign );
//            // 0.5.wait;
//            // this.toggleAllVisible
//        }.fork(AppClock)
//    }
//
//    pause {
//        inSynth.set(\pauseGate, 0);
//        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.pause });
//        this.moduleArray.do({ |mod|
//            if(mod.notNil,{ mod.pause })
//        });
//        fader.set(\pauseGate, 0);
//        synths.do({ |snd|
//            if(snd.notNil,{ snd.set(\pauseGate, 0) })
//        });
//        stripGroup.run(false);
//        this.paused = true;
//    }
//
//    unpause {
//        inSynth.set(\pauseGate, 1);
//        inSynth.run(true);
//        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.unpause });
//        this.moduleArray.do({ |mod| 
//            if(mod.notNil,{ mod.unpause })
//        });
//        fader.set(\pauseGate, 1);
//        fader.run(true);
//        synths.do({ |snd|
//            if(snd.notNil,{ snd.set(\pauseGate, 1); snd.run(true) })
//        });
//        stripGroup.run(true);
//        this.paused = false;
//    }
//
//    highlight { |bool|
//        view.drawFunc_({ |v|
//            var w = v.bounds.width;
//            var h = v.bounds.height;
//            var r = NS_Style.radius;
//            var fill = if(bool,{ NS_Style.highlight },{ NS_Style.transparent });
//
//            Pen.fillColor_(fill);
//            Pen.addRoundedRect(Rect(0, 0, w, h), r, r);
//            Pen.fill;
//        });
//        view.refresh
//    }
//}
//
//NS_OutChannelStrip : NS_ControlModule {
//    classvar numSlots = 4;
//    var <>group, <>outBus, <>pageIndex = -1, <>stripIndex; // do these need setters?
//    var <synths, <stripBus;
//    var stripGroup, <inGroup, slots, <slotGroups, <faderGroup;
//    var <inSynth, <fader;
//    var <inSynthGate = 0;
//    var <moduleSinks, <view;
//    var <label, <send;
//
//    *new { |inGroup, strIndex| 
//        ^super.new.group_(inGroup).stripIndex_(strIndex).init
//    }
//
//    init {
//        this.initControlArrays(2);
//        synths     = List.newClear();
//
//        stripBus   = Bus.audio(group.server,NSFW.numChans);
//
//        stripGroup = Group(group,\addToTail);
//        inGroup    = Group(stripGroup,\addToTail);
//        slots      = Group(stripGroup,\addToTail);
//        slotGroups = numSlots.collect({ |i| Group(slots,\addToTail) });
//        faderGroup = Group(stripGroup,\addToTail);
//
//        inSynth = Synth(\ns_stripIn,[\inBus,stripBus,\thru,1,\outBus,stripBus],inGroup);
//        fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);
//        synths.add( Synth(\ns_stripSend,[\inBus,stripBus,\outBus,0],faderGroup,\addToTail) );
//
//        this.makeView;
//    }
//
//    makeView {
//
//        moduleSinks = slotGroups.collect({ |slotGroup, slotIndex| 
//            NS_ModuleSink(this, slotIndex)
//        });
//
//        label = StaticText().align_(\center).stringColor_(Color.white).string_("out: %".format(stripIndex));
//
//        controls[0] = NS_Control(\mute,ControlSpec(0,1,'lin',1))
//        .addAction(\synth,{ |c| fader.set(\mute, c.value) });
//        assignButtons[0] = NS_AssignButton(this,0,\button).maxWidth_(30);
//
//        controls[1] = NS_Control(\amp,\db)
//        .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) });
//        assignButtons[1] = NS_AssignButton(this,1,\fader).maxWidth_(30);
//
//        view = View().layout_(
//            VLayout(
//                label,
//                HLayout(
//                    VLayout(
//                        *(moduleSinks ++ [
//                            HLayout(
//                                PopUpMenu()
//                                .items_( (0..((NSFW.numOutBusses - 1) - NSFW.numChans)) )
//                                .value_(0)
//                                .action_({ |menu| synths[0].set(\outBus, menu.value) }),
//                                Button()
//                                .maxWidth_(45)
//                                .states_([["S", Color.black, Color.yellow]])
//                                .action_({ |but|
//                                    this.toggleAllVisible
//                                }),
//                                NS_ControlButton(controls[0], [
//                                    ["M",NS_Style.muteRed,NS_Style.textDark],
//                                    [NS_Style.play, NS_Style.playGreen, NS_Style.bGroundDark]
//                                ]),
//                                assignButtons[0]
//                            )]
//                        )
//                    ),
//                    VLayout( NS_ControlFader(controls[1],'vertical').maxWidth_(30).round_(1).showLabel_(false).maxWidth_(30), assignButtons[1] ),
//                )
//            ),
//        );
//
//        view.layout.spacing_(NS_Style.stripSpacing).margins_(NS_Style.stripMargins);
//    }
//
//    asView { ^view }
//
//    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }
//
//
//    inSynthGate_ { |val| /* if this is not here, the language crashes... */ }
//
//    free {
//        moduleSinks.do({ |sink| sink.free });
//        this.amp_(0)
//    }
//
//    amp  { ^controls[1].normValue }
//    amp_ { |amp| controls[1].normValue_(amp) }
//
//    toggleMute {
//        controls[0].value_( 1 - controls[0].value )
//    }
//
//    toggleAllVisible {
//        this.moduleArray.do({ |mod| if( mod.notNil,{ mod.toggleVisible }) });
//    }
//
//    saveExtra { |saveArray|
//        var sinkArray = moduleSinks.collect({ |sink|
//            if(sink.module.notNil,{ sink.save })
//        });
//
//        saveArray.add(sinkArray);
//
//        ^saveArray
//    }
//
//    loadExtra { |loadArray|
//        loadArray.do({ |sinkArray, index|
//            if(sinkArray.notNil,{
//                moduleSinks[index].load(sinkArray, slotGroups[index])
//            })
//        })
//    }
//}
