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

        mouseActionDict = ();

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

            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.clip;

            Pen.fillColor_(NS_Style.highlight);

            if(orientation,{
                string = control.label ++ ": " ++ control.value.round(round).asString;
                Pen.addRoundedRect(Rect(inset, inset, w * normVal, h), r, r)
            },{
                string = control.label ++ ":\n" ++ control.value.round(round).asString;
                Pen.addRoundedRect(
                    Rect(inset, inset + (1 - normVal * h), w, h * normVal), r, r
                );
            });
            Pen.fill;

            Pen.strokeColor_(border);
            Pen.width_(inset * 2);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.stroke;

            Pen.stringCenteredIn( 
                string, 
                Rect(inset, inset, w, h),
                Font(*NS_Style.defaultFont),
                NS_Style.textDark
            );
            Pen.stroke;
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseMoveAction_({ |v, x, y, modifiers|
            if(orientation,{
                control.normValue_( (x / v.bounds.width).clip(0, 1) )
            },{
                control.normValue_( 1 - (y / v.bounds.height).clip(0, 1) )
            });

            v.refresh;
        });

        this.addLeftClickAction({ |f, v, x, y|
            if(orientation,{
                control.normValue_( (x / v.bounds.width).clip(0, 1) )
            },{
                control.normValue_( 1 - (y / v.bounds.height).clip(0, 1) )
            });
        });
        this.addLeftClickAction({ this.toggleAutoAssign(control, 'continuous') }, 'cmd');
        this.addRightClickAction({ this.openControlMenu(control, 'continuous') });

        control.addAction(\qtGui,{ |c| { view.refresh }.defer });
    }
}
