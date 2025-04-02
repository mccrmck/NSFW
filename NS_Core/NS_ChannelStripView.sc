NS_ChannelStripMatrixView {
    var <view;
    var highlight = false;

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;
        var assignButtons = strip.assignButtons;
        var modSinks = strip.slots.collect({ |slot| slot.view });
        var labelString = "%:%".format(strip.pageIndex, strip.stripIndex);
        var routing = NS_MatrixRoutingView(strip);

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style.radius;
            var fill = if(highlight,{ NS_Style.highlight },{ NS_Style.transparent });

            Pen.fillColor_(fill);
            Pen.strokeColor_(NS_Style.bGroundDark);
            Pen.width_(2);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(1), r, r);
            Pen.fillStroke;
        })
        .layout_(
            VLayout(
                UserView()
                .minHeight_(labelString.bounds(Font(*NS_Style.defaultFont)).height + 2)
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var rect = Rect(0, 0, w, h);
                    var r = NS_Style.radius;//w.min(h) / 2;

                    Pen.stringCenteredIn(
                        labelString,
                        rect,
                        Font(*NS_Style.defaultFont),
                        NS_Style.textLight
                    )
                })
                .mouseDownAction_({ |view, x, y, modifiers, buttonNumber, clickCount|
                    if(buttonNumber == 1,{
                        Menu(
                            CustomViewAction(routing.view)
                        ).front
                    })
                }) ,
                VLayout( *modSinks ),
                HLayout(
                    NS_ControlFader(controls[0]).round_(1).showLabel_(false),
                    assignButtons[0]
                ),
                HLayout( 
                    NS_ControlButton(controls[1], [["S", Color.black, Color.yellow]]),
                    assignButtons[1],
                    NS_ControlButton(controls[2], [
                        ["M", NS_Style.muteRed, NS_Style.textDark],
                        [NS_Style.play, NS_Style.playGreen, NS_Style.bGroundDark]
                    ]),
                    assignButtons[2]
                ),
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
    }

    toggleAllVisible { }

    highlight { |bool|
        highlight = bool;
        view.refresh
    }

    asView { ^view }
}

// NS_ChannelStripOutView { }
