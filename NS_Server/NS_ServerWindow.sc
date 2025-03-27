NS_ServerWindow {
    var <>win;
    var <swapGrid;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var gradient = Color.rand;

        win = Window(nsServer.name.asString);
        win.drawFunc = {
            var vBounds = win.view.bounds;
            Pen.addRect(vBounds);
            Pen.fillAxialGradient(vBounds.leftTop, vBounds.rightBottom, Color.black, gradient);
        };

        swapGrid = NS_SwapGrid(nsServer);

        win.layout_(
            VLayout(
                GridLayout.rows(
                    *6.collect({ |pageIndex| 
                        View().maxWidth_(600).layout_(
                            HLayout( 
                                *nsServer.strips[pageIndex]
                            ).margins_(NS_Style.viewSpacing).spacing_(NS_Style.viewMargins)
                        )
                    }).clump(2)
                ),
                View().maxHeight_(150).maxWidth_(1200).layout_(
                    HLayout(
                        HLayout( *nsServer.outMixer ), 
                        swapGrid,
                    ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
                )
            )
        );

        win.layout.spacing_(NS_Style.windowSpacing).margins_(NS_Style.windowMargins);
        //win.view.maxWidth_(1440).maxHeight_(900);
        win.front;
    }

    free {
        win.close
    }

    save {
        var saveArray = swapGrid.save;
        ^saveArray
    }

    load { |loadArray|
        swapGrid.load(loadArray)
    }
}
