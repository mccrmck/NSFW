NS_ServerInputView : SCViewHolder {
    var <inStripViews;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var stack = StackLayout().mode_(\stackOne).nsMarginsSpacing('inner');
        var meters = nsServer.inputs.collect({ |inStrip, index|
            NS_LevelMeter(inStrip.stripId)
            .highlight(index < 1)
            .addLeftClickAction({ |l|
                if(l.isHighlighted.not,{
                    meters.do(_.highlight(false));
                    stack.index_(index);
                    l.highlight(true)
                });
            });
        });

        var playPause = nsServer.inputs.collect({ |inStrip, index|
            NS_Button([
                NS_Style('play'), NS_Style('pause')
            ])
            .fixedSize_(20)
            .addLeftClickAction({ |b|
                if(inStrip.paused,{
                    inStrip.unpause;
                    inStrip.addResponder(meters[index])
                },{
                    inStrip.pause;
                    meters[index].value_(0, 0);
                    inStrip.freeResponder
                });
            });
        });

        inStripViews = nsServer.inputs.collect({ |inStrip|
            NS_ChannelStripInView(inStrip)
        });

        inStripViews.do({ |view| stack.add(view) });

        view = NS_ContainerView()
        .layout_(
            VLayout(
                StaticText()
                .string_("inputs")
                .align_(\center)
                .maxHeight_(20)
                .stringColor_( NS_Style('textDark') ),
                NS_HDivider(),
                GridLayout.rows( 
                    *meters.collect({ |meter, index|
                        [meter, playPause[index]]
                    })
                ).nsMarginsSpacing('inner'),
                NS_HDivider(),
                stack
            ).nsMarginsSpacing('view')
        )
    }
}
