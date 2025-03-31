NS_ChannelStrip1 : NS_ControlModule {
    var <group, <numSlots;
    var stripGroup, <inGroup, allSlots, <slotGroups, <faderGroup;
    var <slots;
    var <stripBus;
    var <inSynth, fader;
    var sends;

    var <view;

    var <>paused = false;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_stripIn,{
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                sig = sig * \thru.kr(0);
                ReplaceOut.ar(\outBus.kr,sig);
            }).add;

            SynthDef(\ns_stripFader,{
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0,0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));

                ReplaceOut.ar(\bus.kr, sig)
            }).add;

            SynthDef(\ns_stripSend,{
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                Out.ar(\outBus.kr,sig);
            }).add
        }
    }

    *new { |inGroup, numModules = 6|
        ^super.new.init(inGroup, numModules)
    }

    init { |inGroup, numModules|
        group    = inGroup;
        numSlots = numModules;
        this.initControlArrays(3);

        stripBus   = Bus.audio(group.server, NSFW.numChans(group.server));

        slots      = numSlots.collect({ |i| NS_ModuleSlot(this, i) });

        stripGroup = Group(group,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        allSlots   = Group(stripGroup,\addToTail);
        slotGroups = numSlots.collect({ |i| Group(allSlots,\addToTail) });
        faderGroup = Group(stripGroup,\addToTail);

        fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);
        sends = List.newClear();
        
        controls[0] = NS_Control(\amp,\db)
        .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) });
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(30);

        controls[1] = NS_Control(\visible,ControlSpec(0,0,'lin',1))
        .addAction(\synth, { |c| this.toggleAllVisible }, false);
        assignButtons[1] = NS_AssignButton(this,1,\button).maxWidth_(30);


        controls[2] = NS_Control(\mute,ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false);
        assignButtons[2] = NS_AssignButton(this,2,\button).maxWidth_(30);

        view = NS_ChannelStripView(this)

    }

    inSynth_ { |synthKey|
        inSynth = Synth(synthKey.asSymbol,[
            \inBus,stripBus, \outBus, stripBus
        ],inGroup)
    }

    addSend { |outBus|


    }



    moduleArray { ^slots.collect({ |slt| slt.module }) }

    inSynthGate_ {} // this needs an overhaul

    // I don't like calling controls by their indexes...
    amp  { ^controls[0].normValue }
    amp_ { |amp| controls[0].normValue_(amp) }

    toggleAllVisible {
        this.moduleArray.do({ |mod| if( mod.notNil,{ mod.toggleVisible }) });
    }

    free {
       // this.setInSynthGate(0);
        slots.do({ |slt| slt.free });
        this.amp_(0)
    }

    pause {
       // inSynth.set(\pauseGate, 0);
       // if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.pause });
        this.moduleArray.do({ |mod|
            if(mod.notNil,{ mod.pause })
        });
        fader.set(\pauseGate, 0);
        stripGroup.run(false);
        this.paused = true;
    }

    unpause {
     //   inSynth.set(\pauseGate, 1);
     //   inSynth.run(true);
      //  if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.unpause });
        this.moduleArray.do({ |mod| 
            if(mod.notNil,{ mod.unpause })
        });
        fader.set(\pauseGate, 1);
        fader.run(true);
                stripGroup.run(true);
        this.paused = false;
    }

    //saveExtra { |saveArray|
    //    var stripArray = List.newClear(0);
    //    var inSinkArray = if(inSink.module.notNil,{ inSink.save }); 
    //    var sinkArray = moduleSinks.collect({ |sink|
    //        if(sink.module.notNil,{ sink.save })
    //    });
    //    stripArray.add( inSinkArray );
    //    stripArray.add( sinkArray );
    //    stripArray.add( inSynthGate );

    //    saveArray.add(stripArray);

    //    ^saveArray
    //}

    //loadExtra { |loadArray|
    //    var cond = CondVar();
    //    var count = 0;

    //    {
    //        if(loadArray[0].notNil,{ inSink.load( loadArray[0] ) });

    //        loadArray[1].do({ |sinkArray, index|
    //            if(sinkArray.notNil,{
    //                moduleSinks[index].load(sinkArray, slotGroups[index])
    //            });
    //            count = count + 1
    //        });
    //        cond.wait( count == loadArray[1].size );

    //        this.setInSynthGate( loadArray[2] );
    //        inSynth.set( \thru, inSynthGate.sign );
    //        // 0.5.wait;
    //        // this.toggleAllVisible
    //    }.fork(AppClock)
    //}
}

NS_ChannelStripView {
    var <view;
    var highlight = false;

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;
        var assignButtons = strip.assignButtons;
        var modSinks = strip.slots.collect({ |slot| slot.view });
        var routing = NS_MatrixRoutingView(strip);

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style.radius;
            var fill = if(highlight,{ NS_Style.highlight },{ NS_Style.transparent });

            Pen.fillColor_(fill);
            Pen.strokeColor_(NS_Style.bGroundDark);
            Pen.width_(2);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(1), r, r);
            Pen.fillStroke;
        })
        .layout_(
            VLayout(
                UserView()
                .minHeight_("string".bounds(Font(*NS_Style.defaultFont)).height + 2)
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var rect = Rect(0, 0, w, h);
                    var r = NS_Style.radius;//w.min(h) / 2;

                    Pen.stringCenteredIn(
                        "0:0",
                        rect,
                        Font(*NS_Style.defaultFont),
                        NS_Style.textLight
                    )
                })
                .mouseDownAction_({ |view, x, y, modifiers, buttonNumber, clickCount|
                    if(buttonNumber == 1,{
                        Menu(
                            CustomViewAction(routing.view)
                        ).front
                    })
                }) ,
                VLayout( *modSinks ),
                HLayout(
                    NS_ControlFader(controls[0]).round_(1).showLabel_(false),
                    assignButtons[0]
                ),
                HLayout( 
                    NS_ControlButton(controls[1],[
                        ["S", Color.black, Color.yellow]
                    ]),
                    assignButtons[1],
                    NS_ControlButton(controls[2],[
                        ["M", NS_Style.muteRed, NS_Style.textDark],
                        [NS_Style.play, NS_Style.playGreen, NS_Style.bGroundDark]
                    ]),
                    assignButtons[2]
                ),
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
    }

    toggleAllVisible { }

    highlight { |bool|
        highlight = bool;
        view.refresh
    }

    asView { ^view }
}

NS_MatrixRoutingView {
    var <view;

    *new { |strip|
        ^super.new.init
    }

    init { |strip|
        view = View()
        .background_(NS_Style.bGroundDark)
        .layout_(
            VLayout(
                StaticText()
                .string_("strips")
                .align_(\center)
                .stringColor_(NS_Style.textLight),
                GridLayout.rows(
                    *(24.collect({ |i|
                        Button()
                        .maxWidth_(30)
                        .font_(Font(*NS_Style.smallFont))
                        .states_([
                            [
                                "%:%".format((i/4).floor.asInteger, i % 4),
                                NS_Style.textDark,
                                NS_Style.bGroundLight
                            ],
                            [
                                "%:%".format((i/4).floor.asInteger, i % 4),
                                NS_Style.textLight,
                                NS_Style.bGroundDark
                            ]
                        ])
                    }).clump(4))
                ),
                StaticText()
                .string_("outputs")
                .align_(\center)
                .stringColor_(NS_Style.textLight),
                HLayout(
                    *4.collect({ |i| 
                        Button()
                        .maxWidth_(30)
                        .font_(Font(*NS_Style.smallFont))
                        .states_([
                            [
                                i,
                                NS_Style.textDark,
                                NS_Style.bGroundLight
                            ],
                            [
                                i,
                                NS_Style.textLight,
                                NS_Style.bGroundDark
                            ]
                        ])
                    })
                )
            ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
        )
    }

    asView { ^view }

}
