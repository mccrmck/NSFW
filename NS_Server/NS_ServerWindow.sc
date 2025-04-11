NS_MatrixServerWindow {
    var <nsServer;
    var <win;
    var <stripViews, <outStripViews, <swapGrid;

    *new { |server|
        ^super.newCopyArgs(server).init
    }

    init {
        var gradient = Color.rand;

        win = Window(nsServer.name.asString);
        win.drawFunc = {
            var vBounds = win.view.bounds;
            Pen.addRect(vBounds);
            Pen.fillAxialGradient(
                vBounds.leftTop,
                vBounds.rightBottom,
                NS_Style.bGroundDark, 
                gradient
            );
        };

        stripViews = nsServer.strips.deepCollect(2,{ |strip|
            NS_ChannelStripMatrixView(strip)
        });

        outStripViews = nsServer.outMixer.collect({ |strip|
            NS_ChannelStripOutView(strip)
        });

        swapGrid = NS_SwapGrid(this);

        win.layout_(
            VLayout(
                GridLayout.rows(
                    *stripViews.collect({ |page|
                        HLayout(*page)
                    }).clump(2)
                ),
                View()
                .layout_(
                    HLayout(
                        HLayout( *outStripViews ), 
                        swapGrid,
                    )
                )
            )
        );

        win.layout.spacing_(NS_Style.windowSpacing).margins_(NS_Style.windowMargins);
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
