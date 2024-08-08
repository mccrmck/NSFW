NS_ServerWindow {
    var <>win;
    var <pages, <outMixer, <swapGrid;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var bounds = Window.availableBounds;
        var mainWidth = 1260;
        var gradient = Color.rand; /*Color.fromHexString("#7b14ba")*/
        var headerPanel, mainPanel, controlPanel, modulePanel;
        var saveBut, loadBut;

        win = Window(nsServer.server.asString,bounds);
        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        mainPanel    = View(win).maxWidth_(mainWidth);
        controlPanel = View(win).maxWidth_(mainWidth).maxHeight_(180);

        pages        = 6.collect({ |pageIndex| View().layout_( HLayout( *nsServer.strips[pageIndex] ).spacing_(0).margins_([2,0]) ) });
        outMixer     = HLayout( *nsServer.outMixer );
        swapGrid     = NS_SwapGrid(nsServer);

        saveBut      = Button()
        .states_([["save\nserver", Color.white, Color.black]])
        .maxHeight_(60)
        .action_({
            Dialog.savePanel(
                { |path| 
                    var saveArray = nsServer.save; 
                    saveArray.writeArchive(path);
                    //NSFW.controllers.do({ |ctrl| ctrl.write(path) })
                }, 
                nil,
                PathName( NSFW.filenameSymbol.asString ).pathOnly +/+ "saved/servers/"
            )
        });

        loadBut     = Button()
        .states_([["load\nserver", Color.white, Color.black]])
        .maxHeight_(60)
        .action_({
            Dialog.openPanel(
                { |path| 
                    var loadArray = Object.readArchive(path); 
                    nsServer.load(loadArray);
                    //NSFW.controllers.do({ |ctrl| ctrl.read(path) })
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
                        pages[0..2],
                        pages[3..5]
                    )
                ),
                controlPanel.layout_( 
                    HLayout(
                        outMixer, 
                        swapGrid, 
                        VLayout( saveBut, loadBut )
                    )
                )
            )
        );

        win.layout.spacing_(0).margins_(0);
        mainPanel.layout.spacing_(0).margins_([4,8,4,0]);
        controlPanel.layout.spacing_(0).margins_([8,0,8,8]);
        win.view.maxWidth_(mainWidth);
        win.front;

        win.onClose_({
            // free and close everything, evenutally
            // maybe this just frees all resources and kills this server? 
            Window.closeAll;
            thisProcess.recompile;
        })
    }

    save {
        var saveArray = swapGrid.save;
        ^saveArray
    }

    load { |loadArray|
        swapGrid.load(loadArray)
    }
}
