NS_ServerInputView : NS_Widget {
    var <inStripViews;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var stack = StackLayout().mode_(\stackOne);
        var meters = nsServer.inputs.collect({ |inStrip, index|
            NS_LevelMeter(inStrip.stripId)
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
        .maxHeight_(
            NS_Style('viewMargins')[1] + // top margin
            20 + 2 +                     // label + divider
            (8 * (20 + 2)) +                   // NS_LevelMeter height
            180 +                      // chanInView Height
            NS_Style('viewMargins')[3]   // bottom margin
        )
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
                ).margins_(NS_Style('viewMargins')),
                stack
            ).spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'))
        )
    }
}
