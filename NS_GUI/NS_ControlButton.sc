NS_ControlButton : NS_ControlWidget {

    *new { |ns_control, statesArray|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control, statesArray)
    }

    init { |control, states|
        var inset = NS_Style.inset;
        var scale = 1;

        states = states ?? {[
            ["", NS_Style.textDark, NS_Style.bGroundLight],
            ["", NS_Style.textLight, NS_Style.bGroundDark]
        ]};

        states = states.collect({ |state, index|

            switch(state.class,
                String, {
                    [
                        [state, NS_Style.textDark, NS_Style.bGroundLight],
                        [state, NS_Style.textLight, NS_Style.bGroundDark]
                    ].at(index)
                },
                Array, { state }
            );
        });

        view = UserView()
        .fixedHeight_(20)
        .drawFunc_({ |v|
            var string;
            var val = control.value.asInteger;
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;

            var border = if(isHighlighted,{ 
                NS_Style.assigned
            },{
                NS_Style.bGroundDark
            });

            Pen.scale(scale, scale);
            Pen.translate((1-scale) * w / 2, (1-scale) * h / 2);
            Pen.fillColor_(states[val][2]);
            Pen.strokeColor_(border);
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.fillStroke;

            Pen.stringCenteredIn( 
                states[val][0], Rect(inset, inset, w, h), Font(*NS_Style.defaultFont), states[val][1]
            );
            Pen.stroke;

        })
        .mouseDownAction_({ |v, x, y, modifiers, buttonNumber, clickCount|

            if(buttonNumber == 0,{
                if(clickCount == 1,{
                    var val = (control.value + 1).wrap(0, states.size);
                    control.value_(val)
                },{
                    this.toggleAutoAssign(control, 'discrete')
                });
            },{
                this.openControlMenu(control, 'discrete')
            });

            scale = 0.97;

            view.refresh;
        })
        .mouseUpAction_({
            scale = 1;
            view.refresh
        });

        control.addAction(\qtGui,{ |c| { view.refresh }.defer })
    }

}
