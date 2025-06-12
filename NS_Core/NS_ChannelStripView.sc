NS_ChannelStripMatrixView : NS_Widget {

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;
        var slotViews = strip.slots.size.collect({ |slotIndex| 
            NS_ModuleSlotView(strip, slotIndex)
        });
        var routing = NS_MatrixRoutingView(strip);

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style.radius;
            var fill = if(strip.paused,{ 
                NS_Style.transparent
            },{
                NS_Style.highlight
            });

            Pen.fillColor_(fill);
            Pen.strokeColor_(NS_Style.bGroundDark);
            Pen.width_(2);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(1), r, r);
            Pen.fillStroke;
        })
        .layout_(
            VLayout(
                UserView()
                .minHeight_(strip.stripId.bounds(Font(*NS_Style.defaultFont)).height + 2)
                .setContextMenuActions( CustomViewAction(routing.view) )
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var rect = Rect(0, 0, w, h);

                    Pen.stringCenteredIn(
                        strip.stripId,
                        rect,
                        Font(*NS_Style.defaultFont),
                        NS_Style.textLight
                    )
                }),
                VLayout( *slotViews ),
                NS_ControlFader(controls[0], 0.1),
                HLayout( 
                    NS_ControlButton(controls[1], [["S", NS_Style.textDark, NS_Style.yellow]]),
                    NS_ControlButton(controls[2], [
                        ["M", NS_Style.red, NS_Style.bGroundDark],
                        [NS_Style.play, NS_Style.green, NS_Style.bGroundDark]
                    ]),
                )
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
    }

    // after loading, for example
    refresh {
        // slotViews

    }
}

NS_ChannelStripOutView : NS_Widget { 

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;
        var modSinks = strip.slots.size.collect({ |slotIndex| NS_ModuleSlotView(strip, slotIndex) });
        var routing = NS_MatrixRoutingOutView(strip);

        view = View().layout_(
            VLayout(
                UserView()
                .minHeight_(strip.stripId.bounds(Font(*NS_Style.defaultFont)).height + 2)
                .setContextMenuActions( CustomViewAction(routing.view) )
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var rect = Rect(0, 0, w, h);

                    Pen.stringCenteredIn(
                        strip.stripId,
                        rect,
                        Font(*NS_Style.defaultFont),
                        NS_Style.textLight
                    )
                }),
                VLayout( *modSinks ),
                NS_ControlFader(controls[0], 0.1),
                HLayout( 
                    NS_ControlButton(controls[1], [["S", NS_Style.textDark, NS_Style.yellow]]),
                    NS_ControlButton(controls[2], [
                        ["M", NS_Style.red, NS_Style.bGroundDark],
                        [NS_Style.play, NS_Style.green, NS_Style.bGroundDark]
                    ]),
                )
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
    }
}
