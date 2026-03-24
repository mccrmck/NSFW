NS_Button : NS_Widget {
    var <value = 0;

    *new { |statesArray|
        ^super.new.drawWidget(statesArray)
    }

    *show {
        ^this.new([
            [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
        ])
    }

    *clear {
        ^this.new([
            [NS_Style('clear'), NS_Style('textDark'), NS_Style('red')]
        ])
    }

    drawWidget { |states|
        var inset = NS_Style('inset');
        var halfInset = inset / 2;
        var scale = 1;

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
        .minHeight_(20)
        .minWidth_(40)
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.width;
            var h = rect.height;
            var r = w.min(h) / 2;

            Pen.scale(scale, scale);
            Pen.translate((1-scale) * w / 2, (1-scale) * h / 2);
            Pen.fillColor_(states[value][2]);

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(halfInset, halfInset, w + inset, h + inset), r, r);
            Pen.fillStroke;

            Pen.stringCenteredIn( 
                states[value][0],
                Rect(inset, inset, w, h),
                Font(*NS_Style('smallFont')),
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
        this.addDoubleClickAction({ |...args|
            mouseActionDict['none']['leftClick'].value(*args)
        });
        this.addRightClickAction({ });
    }

    value_ { |val|
        // widget doesn't store state, so I can't wrap/clip
        // consider storing state size upon instantiation, wrap around that?
        value = val;
        view.refresh;
    }
}
