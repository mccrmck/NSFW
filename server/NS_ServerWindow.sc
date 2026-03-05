NS_MatrixServerWindow {
    var <win;
    var <stripViews, <outStripViews, <swapGridView;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var gradient = Color.rand;
        var layout;
        nsServer.window = this;

        win = Window(nsServer.name.asString);
        win.drawFunc = {
            var vBounds = win.view.bounds;
            Pen.addRect(vBounds);
            Pen.fillAxialGradient(
                vBounds.leftTop,
                vBounds.leftBottom,
                NS_Style('bGroundDark'),
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

        layout = stripViews.collect({ |page| HLayout(*page) }).clump(2) ++
        [[[
            HLayout(
                [HLayout( *outStripViews ), stretch: 6],
                [StaticText().string_("NSFW").align_(\center), stretch: 1],
                [swapGridView, stretch: 1]
            ),
            columns: 2
        ]]];

        win.layout_( GridLayout.rows(*layout) );

        win.layout.spacing_(NS_Style('windowSpacing')).margins_(NS_Style('windowMargins'));
    }

    free { win.close }
}
