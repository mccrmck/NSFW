NS_Button : NS_Widget {
    var <value = 0;

    *new { |statesArray|
        ^super.new.init(statesArray)
    }

    init { |states|
        var inset = NS_Style('inset');
        var scale = 1;
        
        mouseActionDict = ();

        states = states ?? {[
            ["", NS_Style('textDark'), NS_Style('bGroundLight')],
            ["", NS_Style('textLight'), NS_Style('bGroundDark')]
        ]};

        states = states.collect({ |state, index|

            switch(state.class,
                String, {
                    [
                        [state, NS_Style('textDark'), NS_Style('bGroundLight')],
                        [state, NS_Style('textLight'), NS_Style('bGroundDark')]
                    ].at(index)
                },
                Array, { state }
            );
        });

        view = UserView()
        .fixedHeight_(20)
        .minWidth_(40)
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;

            Pen.scale(scale, scale);
            Pen.translate((1-scale) * w / 2, (1-scale) * h / 2);
            Pen.fillColor_(states[value][2]);
            
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.fillStroke;

            Pen.stringCenteredIn( 
                states[value][0],
                Rect(inset, inset, w, h),
                Font(*NS_Style('defaultFont')),
               states[value][1]
            );
            Pen.stroke;
        })
        .mouseDownAction_({ |...args|
            value = (value + 1).wrap(0, states.size - 1);
            scale = 0.9;

            this.onMouseDown(*args)
        })
        .mouseUpAction_({ scale = 1; view.refresh });

        this.addLeftClickAction({ });
        this.addDoubleClickAction({ });
        this.addRightClickAction({ });
    }

    value_ { |val|
        value = val; // widget doesn't store state, so I can't wrap/clip
        view.refresh;
    }
}
