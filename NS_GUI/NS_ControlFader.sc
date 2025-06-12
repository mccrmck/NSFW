NS_ControlFader : NS_ControlWidget {
    var <>round;

    *new { |ns_control, round = 0.01, orientation = 'horz'|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        orientation = switch(orientation,
            \horz,       { true },
            \horizontal, { true },
            \vert,       { false },
            \vertical,   { false },
            orientation
        );

        ^super.new.round_(round).init(ns_control, orientation)
    }

    init { |control, orientation|
        var inset = NS_Style.inset;

        view = UserView()
        .fixedHeight_(20)
        .drawFunc_({ |v|
            var string;
            var normVal = control.normValue;
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;

            var border = if(isHighlighted,{ 
                NS_Style.assigned
            },{
                NS_Style.bGroundDark
            });

            Pen.width_(inset);
            Pen.addRoundedRect(Rect(inset / 2, inset / 2, w + inset, h + inset), r, r);
            Pen.clip;

            Pen.fillColor_(NS_Style.highlight);

            if(orientation,{
                string = control.label ++ ": " ++ control.value.round(round).asString;
                Pen.addRoundedRect(Rect(inset, inset, w * normVal, h), r, r)
            },{
                string = control.label ++ ":\n" ++ control.value.round(round).asString;
                Pen.addRoundedRect(Rect(inset, (1 - normVal) * h + inset, w, h * normVal), r, r);
            });
            Pen.fill;

            Pen.strokeColor_(border);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.stroke;

            Pen.stringCenteredIn( 
                string, Rect(inset, inset, w, h), Font(*NS_Style.defaultFont), NS_Style.textLight
            );
            Pen.stroke;
        })
        .mouseDownAction_({ |v, x, y, modifiers, buttonNumber, clickCount|

            if(buttonNumber == 0,{
                if(clickCount == 1,{
                    if(orientation,{
                        control.normValue_( (x / v.bounds.width).clip(0, 1) )
                    },{
                        control.normValue_( 1 - (y / v.bounds.height).clip(0, 1) )
                    });
                },{
                    this.toggleAutoAssign(control, 'continuous')
                });
            },{
                this.openControlMenu(control, 'continuous')
            });

            view.refresh;
        })
        .mouseMoveAction_({ |v, x, y, modifiers|
            if(orientation,{
                control.normValue_( (x / v.bounds.width).clip(0, 1) )
            },{
                control.normValue_( 1 - (y / v.bounds.height).clip(0, 1) )
            });

            view.refresh;
        });

        control.addAction(\qtGui,{ |c| { view.refresh }.defer });
    }
}
