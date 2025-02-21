NS_ServerWindow {
    var <>win;
    var <pages, <outMixer, <swapGrid;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var bounds = Window.availableBounds;
        var mainWidth = bounds.width * 0.8;
        var gradient = Color.rand;
        var headerPanel, mainPanel, controlPanel, modulePanel;
        var saveBut, loadBut;

        win = Window(nsServer.server.asString);
        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        mainPanel    = View(win).maxWidth_(mainWidth);
        controlPanel = View(win).maxWidth_(mainWidth).maxHeight_(150);

        pages        = 6.collect({ |pageIndex| View().layout_( HLayout( *nsServer.strips[pageIndex] ).spacing_(4).margins_([4,4]) ) });
        outMixer     = HLayout( *nsServer.outMixer );
        swapGrid     = NS_SwapGrid(nsServer);

        saveBut      = Button()
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

        loadBut     = Button()
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

        win.layout_(
            VLayout(
                mainPanel.layout_(
                    GridLayout.rows(
                        pages[0..1],
                        pages[2..3],
                        pages[4..5],
                    )
                ),
                controlPanel.layout_( 
                    HLayout(
                        outMixer, 
                        swapGrid,
                        NS_ModuleList(),
                        VLayout( 
                            saveBut, 
                            loadBut, 
                            Button()
                            .states_([["open o-s-c",Color.white,Color.black],["close o-s-c",Color.black,Color.white]]) 
                            .action_({ |but|
                                var val = but.value;
                                case
                                { val == 1 } { OpenStageControl.makeWindow }
                                { val == 0 } { OpenStageControl.closeWindow }
                            })
                        )
                    )
                )
            )
        );

        win.layout.spacing_(0).margins_(8);
        mainPanel.layout.spacing_(0).margins_(0);
        controlPanel.layout.spacing_(4).margins_(0);
        win.view.maxWidth_(mainWidth);
        win.view.maxHeight_(bounds.height * 0.75);
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
