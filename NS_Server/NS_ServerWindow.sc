NS_ServerWindow {
    var <>win;
    var <swapGrid;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var gradient = Color.rand;
        var headerView;
        var saveBut, loadBut;

        win = Window(nsServer.server.asString);
        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        swapGrid     = NS_SwapGrid(nsServer).view.maxWidth_(240);

        saveBut      = Button()
        .maxWidth_(120)
        .states_([["save server", Color.white, Color.black]])
        .action_({
            Dialog.savePanel(
                { |path| 
                    var saveArray = nsServer.save; 
                    path.postln;
                    saveArray.writeArchive(path);
                }, 
                nil,
                PathName( NSFW.filenameSymbol.asString ).pathOnly +/+ "saved/servers/"
            )
        });

        loadBut      = Button()
        .maxWidth_(120)
        .states_([["load server", Color.white, Color.black]])
        .action_({
            Dialog.openPanel(
                { |path| 
                    var loadArray = Object.readArchive(path); 
                    nsServer.load(loadArray);
                }, 
                nil,
                false,
                PathName( NSFW.filenameSymbol.asString ).pathOnly +/+ "saved/servers/"
            )
        });

        headerView   = View().layout_(
            VLayout(
                VLayout( 
                    *8.collect({ |i| 
                        NS_LevelMeter("input: %".format(i))
                        .value_(1.0.rand)
                        .action_({ |lm|
                            lm.toggleHighlight;
                            ~stack.index_(i)
                        })
                    })
                ),
                View().background_(Color.black).minHeight_(15),
                VLayout(
                   // *24.collect({ |i|
                   //     NS_LevelMeter("out: %".format(i))
                   //     .value_(1.0.rand)
                   // })
                     ~stack = StackLayout( 
                         *8.collect({ |i|
                              NS_ServerInputView( nsServer.inputs[i] )
                         })
                     )
                )
            ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
        );

        win.layout_(
            HLayout(
                VLayout(
                    saveBut,
                    loadBut,
                    Button()
                    .maxWidth_(150)
                    .states_([["open o-s-c",Color.white,Color.black],["close o-s-c",Color.black,Color.white]]) 
                    .action_({ |but|
                        var val = but.value;
                        case
                        { val == 1 } { OpenStageControl.makeWindow }
                        { val == 0 } { OpenStageControl.closeWindow }
                    }),
                    headerView
                ),
                VLayout(
                    GridLayout.rows(
                        *6.collect({ |pageIndex| 
                            View().layout_(
                                HLayout( 
                                    *nsServer.strips[pageIndex]
                                ).margins_(NS_Style.viewSpacing).spacing_(NS_Style.viewMargins)
                            )
                        }).clump(2)
                    ),
                    View().maxHeight_(150).layout_(
                        HLayout(
                            HLayout( *nsServer.outMixer ), 
                            swapGrid,
                            NS_ModuleList(),
                        ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
                    ),
                )
            )
        );

        win.layout.spacing_(NS_Style.windowSpacing).margins_(NS_Style.windowMargins);
        win.view.maxWidth_(1440).maxHeight_(900);
        win.front;
        win.onClose_({ NSFW.cleanup })
    }

    save {
        var saveArray = swapGrid.save;
        ^saveArray
    }

    load { |loadArray|
        swapGrid.load(loadArray)
    }
}
