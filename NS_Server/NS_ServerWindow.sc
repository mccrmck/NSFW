NS_MatrixServerWindow {
    var <win;
    var <stripViews, <outStripViews, <swapGrid;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
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
            strip.view 
        });

        outStripViews = nsServer.outMixer.collect({ |strip|
            strip.view
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
                .layout_(
                    HLayout(
                        HLayout( *outStripViews ), 
                        swapGrid,
                    )
                )
            )
        );

        win.layout.spacing_(NS_Style.windowSpacing).margins_(NS_Style.windowMargins);
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
