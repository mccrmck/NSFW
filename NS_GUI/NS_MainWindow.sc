NS_MainWindow {
    var <>win, <server;
    var <header, <pages, <outMixer, <outFX, <swapGrid, <moduleList;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var bounds = Window.availableBounds;
        var mainWidth = 1260;
        var moduleWidth = 180;
        var gradient = Color.rand; /*Color.fromHexString("#7b14ba")*/
        var headerPanel, mainPanel, controlPanel, expandButton, modulePanel;

        server = nsServer;

        win = Window("NSFW",bounds);
        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        headerPanel  = View(win).maxWidth_(mainWidth);
        mainPanel    = View(win).maxWidth_(mainWidth);
        controlPanel = View(win).maxWidth_(mainWidth).maxHeight_(180);
        modulePanel  = View(win).maxWidth_(moduleWidth).visible_(false).minWidth_(150);

        header       = NS_WindowHeader(headerPanel, nsServer);
        pages        = 6.collect({ |pageIndex| NS_PageView(mainPanel, nsServer, pageIndex) });
        outMixer     = NS_OutMixerView(controlPanel, server);
        swapGrid     = NS_SwapGrid(controlPanel);
        moduleList   = NS_ModuleList(modulePanel);
        expandButton = Button(modulePanel).maxHeight_(300).maxWidth_(15)
        .states_([["▶\n\n\n\n▶\n\n\n\n▶",Color.fromHexString("#7b14ba"),Color.black],["▶\n\n\n\n▶\n\n\n\n▶",Color.fromHexString("#7b14ba"),Color.black]])
        .action_({ |but|
            var val = but.value;
            if(val == 1,{
                modulePanel.visible_(true);
            },{
                modulePanel.visible_(false);
                win.view.resizeTo(mainWidth,bounds.height);
            })
        });

        win.layout_(
            HLayout(
                VLayout(
                    headerPanel.layout_(
                        HLayout( header )
                    ),
                    mainPanel.layout_(
                        GridLayout.rows(
                            pages[0..2],
                            pages[3..5]
                        ),
                    ),
                    controlPanel.layout_(
                        HLayout(  outMixer, swapGrid, expandButton )
                    )
                ),
                modulePanel.layout_(
                    VLayout( moduleList )
                )
            ),
        );

        win.layout.spacing_(0).margins_(0);
        headerPanel.layout.spacing_(0).margins_([8,12,8,2]);
        mainPanel.layout.spacing_(0).margins_([4,8,4,0]);
        controlPanel.layout.spacing_(0).margins_([8,0,8,8]);
        modulePanel.layout.spacing_(0).margins_([0,8,8,8]);
        win.view.maxWidth_(mainWidth);
        win.front;

        win.onClose_({
            // free and close everything, evenutally
            Window.closeAll;
            thisProcess.recompile;
        })
    }
}

NS_WindowHeader {
    var <view;

    *new { |parent, server|
        ^super.new.init(parent, server)
    }

    init { |parent, server|
    server.postln;

        view = View(parent).layout_(
            HLayout(
               // StaticText()
               // .maxWidth_(120)
               // .font_(Font.defaultMonoFace)
               // .string_("\'Nuther\n SuperCollider\n Frame\n Work")
               // .stringColor_(Color.fromHexString("#fcb314a")),
                VLayout(
                    HLayout(
                        StaticText().string_("mono ins:").stringColor_(Color.white).maxWidth_(75),
                        HLayout( 
                            *8.collect({ |i|
                                var inSynth;
                                Button().states_([[i]]).maxHeight_(30).maxWidth_(30)
                                .action_({ |button|
                                    inSynth = inSynth ?? { NS_Input(server.inGroup,server.inBussesMono[i],i) };
                                    inSynth.win.front;
                                })
                            })
                        )
                    ),
                    HLayout(
                        StaticText().string_("stereo ins:").stringColor_(Color.white).maxWidth_(75),
                        HLayout( 
                            *4.collect({ |i| 
                                var inSynth;
                                Button().states_([[i]]).maxHeight_(30).maxWidth_(62)
                                .action_({ |button|
                                    inSynth = inSynth ?? { NS_Input(server.inGroup,server.inBussesStereo[i],[i * 2, (i * 2) + 1]) };
                                    inSynth.win.front;
                                })
                            })
                        )
                    )
                ),
                Button()
                .states_([["save"]]),
                Button()
                .states_([["load"]]),
            )
        );

        view.layout.spacing_(2).margins_(0);
    }

    asView { ^view }
}

NS_PageView {
    var <view;

    *new { |parent, server, pageIndex|
        ^super.new.init(parent, server, pageIndex)
    }

    init { |parent, server, pageIndex|
        view = View(parent).layout_(
            HLayout( 
                *4.collect({ |stripIndex| NS_StripView(view, server, pageIndex, stripIndex ) })
            )
        );

        view.layout.spacing_(0).margins_([4,0]);
    }

    asView { ^view }
}

NS_StripView {
    var <view;
    var <inModule;

    *new { |parent, server, pageIndex, stripIndex|
        ^super.new.init(parent, server, pageIndex, stripIndex)
    }

    init { |parent, server, pageIndex, stripIndex|
        var inSink = DragBoth().string_("in").align_(\center)
        .receiveDragHandler_({ |drag|
            var dragString = View.currentDrag.asArray[0];
            var className  = View.currentDrag.asArray[1];
            if( className.respondsTo('isSource'),{
                if(className.isSource == true,{
                    var strip = server.strips[pageIndex][stripIndex];
                    if(inModule.notNil,{ inModule.free });
                    drag.object_(View.currentDrag);
                    drag.string = "in:" + dragString.asString;
                    if(className == NS_Input,{
                        var instance = View.currentDrag.asArray[2];
                        inModule = instance.addSend( strip.inGroup, strip.stripBus)
                    },{
                        inModule = className.new( strip.inGroup, strip.stripBus )
                    });
                })          
            })
        });

        var moduleSinks = 5.collect({ |slotIndex| 
            var strip = server.strips[pageIndex][stripIndex];
            NS_ModuleSink(view, server).moduleAssign_(strip.slotGroups[slotIndex],strip.stripBus)
        });

        view = View(parent).layout_(
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
                NS_Fader(parent, nil,\amp,{ |f| server.strips[pageIndex][stripIndex].amp_(f.value) }).maxHeight_(190),
                NS_AssignButton(),
                HLayout(
                    Button().states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]]), 
                    NS_AssignButton(),
                ),
                HLayout(
                    *4.collect({ |outChannel|
                        Button()
                        .states_([[outChannel,Color.white,Color.black],[outChannel, Color.cyan, Color.black]])
                        .value_([1,0,0,0][outChannel])
                        .action_({ |but|
                            var thisStrip = server.strips[pageIndex][stripIndex];
                            var outSend = thisStrip.sends[outChannel];
                            if(but.value == 0,{
                                if(outSend.notNil,{ outSend.set(\gate,0) });
                                outSend = nil;
                            },{
                                outSend = thisStrip.addSendSynth(server.outMixer[outChannel].stripBus, outChannel)
                            })
                        })
                    })
                )
            )
        );

        view.layout.spacing_(0).margins_(2);
    }

    asView { ^view }
}

NS_ModuleSink {
    var <view, <modSink;
    var <module;

    *new { |parent, server|
        ^super.new.init(parent, server)
    }

    init { |parent, server|

        modSink = DragBoth().align_(\left);

        view = View(parent).layout_( 
            HLayout(
                modSink,
                Button().maxHeight_(45).maxWidth_(15)
                .states_([["S", Color.black, Color.yellow]])
                .action_({ |but|
                    if(module.notNil,{ module.toggleVisible })
                }),
                Button().maxHeight_(45).maxWidth_(15)
                .states_([["X", Color.black, Color.red]])
                .action_({ |but|
                    module.free;
                    module = nil;
                    modSink.string_("")
                });
            )
        );

        view.layout.spacing_(0).margins_([0,2]);
    }

    moduleAssign_ { |stripGroup, stripBus|
        modSink.receiveDragHandler_({ |drag|
            var dragString = View.currentDrag[0];
            var className = View.currentDrag[1];
            if( className.respondsTo('isSource'),{ 
                if(module.notNil,{ module.free });
                drag.string_(dragString);
                module = className.new(stripGroup,stripBus)
            })
        })
    }

    asView { ^view }
}

NS_OutMixerView {
    var <view;

    *new { |parent, server|
        ^super.new.init(parent, server)
    }

    init { |parent, server|

        var moduleSinks = [];

        view = View(parent).layout_(
            HLayout(
                *4.collect({ |channel|
                    VLayout(
                        StaticText().string_("out: %".format(channel)).align_(\center).stringColor_(Color.white),
                        HLayout(
                            VLayout(
                                VLayout( *4.collect({ |slotIndex|
                                    var mixerStrip = server.outMixer[channel];
                                    var sink =  NS_ModuleSink(view, server)
                                    .moduleAssign_(mixerStrip.slotGroups[slotIndex],mixerStrip.stripBus);
                                    moduleSinks = moduleSinks.add( sink );
                                    sink
                                })
                            ),
                            HLayout(
                                PopUpMenu().items_(["0-1","2-3","4-5","6-7"])
                                .value_(0)
                                .action_({ |menu|
                                    server.outMixer[channel].outBus_( menu.value * 2 )
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
                            HLayout(
                                Button().states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]]), 
                                NS_AssignButton(),
                            ),
                        ),
                        VLayout( 
                            NS_Fader(view,nil,\db,{ |f| server.outMixer[channel].amp_(f.value.dbamp) }).maxWidth_(45),
                            NS_AssignButton().maxWidth_(45)
                        )
                    )
                ).margins_([2,0])
            })
        )
        );

        view.layout.spacing_(0).margins_(0);
    }

    asView { ^view }
}

NS_SwapGrid {
    var <view;
    var <buttons;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|

        buttons = 4.collect({ |column|
          var switch = NS_Switch(view,""!6,{ |switch|
                var index = switch.value.indexOf(1);
                6.do({ |page|
                    parent.parents.last // TopView
                    .children[1].children[page] // PageView
                    .children[column].background_(Color.clear)
                });
                parent.parents.last // TopView
                .children[1].children[index] // PageView
                .children[column].background_(Color.fromHexString("#fcb314"))
            }).maxWidth_(90);
            switch.buttons.do({ |but| but.maxHeight_(240)});
            switch
        });

        view = View(parent).layout_(
            HLayout(
                *4.collect({ |i|
                    VLayout(
                        buttons[i],
                        NS_AssignButton(view).maxWidth_(45)
                    )
                })
            )
        );

        4.do({ |i| buttons[i].valueAction_(0) });
        buttons.do({ |switch| switch.layout.spacing_(2).margins_(2) });
        view.layout.spacing_(2).margins_([2,0]);
    }

    asView { ^view }
}

NS_ModuleList {
    var <view;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|
        var path = "/Users/mikemccormick/Library/Application Support/SuperCollider/Extensions/NSFW/NS_Modules/";
        var moduleList = PathName(path).entries.collect({ |entry| 
            if(entry.isFile,{
                entry.fileNameWithoutExtension.split($_)[1];
            })
        });

        view = ScrollView(parent).canvas_(
            View()
            .background_(Color.fromHexString("#fcb314"))
            .layout_(
                VLayout(
                    *moduleList.collect({ |module| 
                        DragSource()
                        .object_([module,("NS_" ++ module).asSymbol.asClass ])
                        .dragLabel_(module)
                        .string_(module)
                    })
                )
            )
        );

        view.canvas.layout.spacing_(2).margins_(2);
    }

    asView { ^view }
}
