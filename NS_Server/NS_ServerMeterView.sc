NS_ServerOutMeter {
    var <view;

    *new { |numChans = 2|
        ^super.new.init(numChans)
    }

    init { |numChans|
        var meterView = View()
        .background_(NS_Style.transparent)
        .layout_(
            VLayout(
               *numChans.collect({ |i|
                   NS_LevelMeter(i).value_(1.0.rand)
               })
            ).spacing_(NS_Style.viewSpacing).margins_(0);
        );

        var meterViewContainer = View()
        .background_(NS_Style.transparent)
        .layout_(
            VLayout(
                meterView 
            ).spacing_(0).margins_(0)
        );

        var scrollY = 0;
        var scrollYLast = 0;
        var scroll = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var halfKnob = w / 10;

            Pen.color_(NS_Style.textDark);
            Pen.moveTo((w/2)@halfKnob);
            Pen.lineTo((w/2)@(h - halfKnob));
            Pen.addOval(
                Rect(
                    w/2 - halfKnob,
                    (scrollY * h).clip(halfKnob, h-halfKnob),
                    halfKnob * 2, halfKnob * 2
                )
            );
            Pen.fillStroke;
        })
        .mouseDownAction_({ |v, x, y, modifiers, buttonNumber, clickCount|
            scrollY = (y / v.bounds.height).clip(0, 1);
            scroll.refresh;
        })
        .mouseMoveAction_({ |v, x, y, modifiers|
            var scrollDelta;
            var clippedY = (y / v.bounds.height).clip(0, 1);

            scrollY = clippedY;
            scrollDelta = scrollY - scrollYLast;
            scrollYLast = scrollY;

            meterView.moveTo(
                meterView.bounds.left, 
                meterView.bounds.top - (scrollDelta * meterView.bounds.height)
            );

            scroll.refresh;
        });

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var rect = Rect(0,0,w,h);

            Pen.fillColor_( NS_Style.highlight );
            Pen.addRoundedRect(rect, NS_Style.radius, NS_Style.radius);
            Pen.fill;
        })
        .onResize_({
            scrollY = 0;
            scrollYLast = 0;
            scroll.refresh;
        })
        .layout_(
            VLayout(
                StaticText()
                .string_("outputs")
                .align_(\center)
                .stringColor_(NS_Style.textDark),
                HLayout(
                    meterView
                    //[ scroll, s: 1]
                )
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins);
    }

    asView { ^view }
}
