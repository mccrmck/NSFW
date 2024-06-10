NS_MainWindow {
    var <>win;
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

        win = Window(nsServer.server.asString,bounds);
        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        headerPanel  = View(win).maxWidth_(mainWidth);
        mainPanel    = View(win).maxWidth_(mainWidth);
        controlPanel = View(win).maxWidth_(mainWidth).maxHeight_(180);
        modulePanel  = View(win).maxWidth_(moduleWidth).visible_(false).minWidth_(150);

        header       = NS_WindowHeader(nsServer);
        pages        = 6.collect({ |pageIndex| 
            View().layout_( HLayout( *nsServer.strips[pageIndex] ).spacing_(0).margins_([4,0]) )
        });
        outMixer     = HLayout( *nsServer.outMixer );
        swapGrid     = NS_SwapGrid(nsServer);
        moduleList   = NS_ModuleList();
        expandButton = Button().maxHeight_(300).maxWidth_(15)
        .states_(["▶\n\n\n\n▶\n\n\n\n▶",Color.fromHexString("#7b14ba"),Color.black]!2)
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
                    headerPanel.layout_( HLayout( header ) ),
                    mainPanel.layout_(
                        GridLayout.rows(
                            pages[0..2],
                            pages[3..5]
                        )
                    ),
                    controlPanel.layout_( HLayout( outMixer, swapGrid, expandButton ) )
                ),
                modulePanel.layout_( VLayout( moduleList ) )
            )
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

    *new { |server|
        ^super.new.init(server)
    }

    init { |server|

        view = View().layout_(
            HLayout(
                VLayout(
                    HLayout(
                        StaticText().string_("mono ins:").stringColor_(Color.white).minWidth_(90),
                        HLayout( 
                            *8.collect({ |i|
                                var inSynth;
                                HLayout(
                                    Button().states_([[i]]).maxHeight_(30).maxWidth_(30)
                                    .action_({ |button|
                                        inSynth = inSynth ?? { NS_Input(server.inGroup,server.inBussesMono[i],i) };
                                        inSynth.toggleVisible;
                                    }),
                                    Button().maxHeight_(30).maxWidth_(15)        // maybe make this button visible when input is active?
                                    .states_([["X", Color.black, Color.red]])
                                    .action_({ |but|
                                        inSynth.free;
                                        inSynth = nil;
                                    });
                                )
                            })
                        )
                    ),
                    HLayout(
                        StaticText().string_("stereo ins:").stringColor_(Color.white).maxWidth_(90),
                        HLayout( 
                            *4.collect({ |i| 
                                var inSynth;
                                HLayout(
                                    Button().states_([[i]]).maxHeight_(30).maxWidth_(79)
                                    .action_({ |button|
                                        inSynth = inSynth ?? { NS_Input(server.inGroup,server.inBussesStereo[i],[i * 2, (i * 2) + 1]) };
                                        inSynth.toggleVisible;
                                    }),
                                    Button().maxHeight_(30).maxWidth_(15)
                                    .states_([["X", Color.black, Color.red]])
                                    .action_({ |but|
                                        inSynth.free;
                                        inSynth = nil;
                                    });
                                )
                            })
                        )
                    )
                ),
                Button()
                .states_([["save"]])
                .maxHeight_(60),
                Button()
                .states_([["load"]])
                .maxHeight_(60)
            )
        );

        view.layout.spacing_(2).margins_(0);
    }

    asView { ^view }
}

NS_ModuleSink {
    var <view, <modSink;
    var <>module;

    *new { 
        ^super.new.init
    }

    init {

        modSink = DragBoth().align_(\left);

        view = View().layout_( 
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

    save {
        var saveArray = Array.newClear(2); // what if module == nil? Is this okay to pass through?

        if(module.notNil,{
            saveArray.put(0, module.class);
            saveArray.put(1, module.save );
        })

        ^saveArray
    }

    load { |loadArray|
        var className = loadArray[0];
        var string    = className.asString.split($_)[1];

        this.string_( string );
       // module = className.new(stripGroup, stripBus);
        module.load(loadArray[1])
    }
}

NS_ModuleList {
    var <view;

    *new {
        ^super.new.init
    }

    init {
        var path = "/Users/mikemccormick/Library/Application Support/SuperCollider/Extensions/NSFW/NS_Modules/";
        var moduleList = PathName(path).entries.collect({ |entry| 
            if(entry.isFile,{
                entry.fileNameWithoutExtension.split($_)[1];
            })
        });

        // add a PopUpMenu or FileDialog to search for paths?

        view = ScrollView().canvas_(
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
