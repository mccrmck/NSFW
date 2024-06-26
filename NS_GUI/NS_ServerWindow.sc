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

        pages        = 6.collect({ |pageIndex| View().layout_( HLayout( *nsServer.strips[pageIndex] ).spacing_(0).margins_([4,0]) ) });
        outMixer     = HLayout( *nsServer.outMixer );
        swapGrid     = NS_SwapGrid(nsServer);

        saveBut   = Button()
        .states_([["save server",Color.white,Color.black]])
        .maxHeight_(60)
        .action_({
            Dialog.savePanel({ |path| nsServer.save(path) }, nil, PathName( NSFW.filenameSymbol.asString).pathOnly +/+ "saved/" )
        });

        loadBut =  Button()
        .states_([["load",Color.white,Color.black]])
        .maxHeight_(60)
        .action_({
            Dialog.openPanel({ |path| nsServer.load(path) }, nil, false, PathName( NSFW.filenameSymbol.asString).pathOnly +/+ "saved/" )
        });

        win.layout_(
            HLayout(
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
            )
        );

        win.layout.spacing_(0).margins_(0);
        mainPanel.layout.spacing_(0).margins_([4,8,4,0]);
        controlPanel.layout.spacing_(0).margins_([8,0,8,8]);
        win.view.maxWidth_(mainWidth);
        win.front;

        win.onClose_({
            // free and close everything, evenutally
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

    moduleAssign_ { |slotGroup, stripBus, strip|
        modSink.receiveDragHandler_({ |drag|
            var moduleString = View.currentDrag[0];
            var className = ("NS_" ++ moduleString).asSymbol.asClass;
            if( className.respondsTo('isSource'),{ 
                if(module.notNil,{ module.free });
                drag.object_(View.currentDrag);
                drag.string_(moduleString);
                module = className.new(slotGroup,stripBus).linkStrip(strip);
            })
        })
    }

    asView { ^view }

    save {
        var saveArray = Array.newClear(2);
        saveArray.put(0, module.class);
        saveArray.put(1, module.save );
        ^saveArray
    }

    load { |loadArray, group, bus, strip|
        var className = loadArray[0];
        var string    = className.asString.split($_)[1];

        modSink.string_( string );
        module = className.new(group, bus).linkStrip(strip);
        module.load(loadArray[1])
    }
}
