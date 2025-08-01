NS_MatrixServerWindow {
    var <win;
    var <stripViews, <outStripViews, <swapGridView;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var gradient = Color.rand;
        nsServer.window = this;

        win = Window(nsServer.name.asString);
        win.drawFunc = {
            var vBounds = win.view.bounds;
            Pen.addRect(vBounds);
            Pen.fillAxialGradient(
                vBounds.leftTop,
                vBounds.leftBottom,
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

        swapGridView = NS_MatrixSwapGridView(nsServer.swapGrid);

        win.layout_(
            VLayout(
                GridLayout.rows(
                    *stripViews.collect({ |page|
                        HLayout(*page)
                    }).clump(2)
                ),
                View().layout_(
                    HLayout(
                        HLayout( *outStripViews ), 
                        swapGridView,
                    ).margins_(0).spacing_(0)
                )
            )
        );

        win.layout.spacing_(NS_Style.windowSpacing).margins_(NS_Style.windowMargins);
    }

    free { win.close }
}
