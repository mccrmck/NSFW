NS_ServerInputView : SCViewHolder {
    var <inStripViews;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var inStrips = nsServer.inStrips;
        var stack = StackLayout()
        .mode_(\stackOne)
        .nsMarginsSpacing('inner');

        var meters = inStrips.collect { |inStrip, index|
            NS_LevelMeter(inStrip.stripId)
            .highlight(index == 0)
            .addLeftClickAction { |l|
                if(l.isHighlighted.not) {
                    meters.do(_.highlight(false));
                    stack.index_(index);
                    l.highlight(true)
                };
            };
        };

        var playPause = inStrips.collect { |inStrip, index|
            NS_Button([ NS_Style('play'), NS_Style('pause') ])
            .fixedSize_(20)
            .addLeftClickAction({ |b|
                if(inStrip.paused)
                { inStrip.unpause; inStrip.addResponder(meters[index]) }
                {
                    inStrip.pause;
                    meters[index].value_(0, 0);
                    inStrip.freeResponder
                };
            });
        };

        inStripViews = inStrips.collect { |inStrip| NS_InStripView(inStrip) };

        inStripViews.do { |view| stack.add(view) };

        view = NS_ContainerView()
        .layout_(
            VLayout(
                NS_Header("inputs"),
                NS_HDivider(),
                GridLayout.rows( 
                    *meters.collect { |meter, index|
                        [meter, playPause[index]]
                    }
                ).nsMarginsSpacing('inner'),
                NS_HDivider(),
                stack
            ).nsMarginsSpacing('view')
        )
    }
}
