NS_ServerWindow { // NS_MatrixServerView
    var <>win;
    var <swapGrid, <stripViews, <outStripViews;

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

        stripViews = nsServer.strips.deepCollect(2,{ |strip|
            NS_ChannelStripView(strip).highlight(true) 
        });

        outStripViews = nsServer.outMixer.collect({ |strip|
            NS_ChannelStripView(strip)
        });

        swapGrid = NS_SwapGrid(nsServer);

        win.layout_(
            VLayout(
                GridLayout.rows(
                    *stripViews.collect({ |page|
                        HLayout(*page)
                    }).clump(2)
                ),
                View()
                //.maxHeight_(150).maxWidth_(1200)
                .layout_(
                    HLayout(
                        HLayout( *outStripViews ), 
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
