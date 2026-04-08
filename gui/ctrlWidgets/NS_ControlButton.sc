NS_ControlButton : NS_ControlWidget {

    *new { |nsControl, statesArray|
        if(nsControl.isNil,{ "must provide an NS_Control".warn });
        ^super.new.drawWidget(nsControl, statesArray)
    }

    *bypass { |nsControl|
        ^NS_ControlButton(nsControl, [
            [NS_Style('play'), NS_Style('textDark'), NS_Style('bGroundLight')],
            ["bypass", NS_Style('textLight'), NS_Style('bGroundDark')]
        ])
    }

    *mute { |nsControl|
        ^NS_ControlButton(nsControl, [
            [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
            [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
        ])
    }

    drawWidget { |control, states|
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
            )
        });

        view = UserView()
        .minHeight_(20)
        .minWidth_(40)
        .drawFunc_({ |v|
            var val = control.value.asInteger;
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');

            var bCol = case
            { control.mapped == 'listening' }{ NS_Style('listening') }
            { control.mapped == 'mapped'    }{ NS_Style('assigned') }
            { NS_Style('bGroundDark') };

            Pen.scale(scale, scale);
            Pen.translate((1-scale) * w / 2, (1-scale) * h / 2);
            Pen.fillColor_(states[val][2]);
            Pen.strokeColor_(bCol);
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.fillStroke;

            Pen.stringCenteredIn( 
                states[val][0],
                Rect(0, 0, w, h),
                Font(*NS_Style('smallFont')),
                states[val][1]
            );
            Pen.stroke;
        })
        .beginDragAction_({ control })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseUpAction_({ scale = 1; view.refresh });

        this.addLeftClickAction({
            var val = (control.value + 1).wrap(0, states.size);
            control.value_(val);
            scale = 0.93;
        });
        this.addDoubleClickAction({ mouseActionDict['none']['leftClick'].value });
        this.addLeftClickAction({ this.toggleAutoAssign(control) }, 'shift');
        this.addRightClickAction({ this.openControlMenu(control) });
        this.addLeftClickAction({ view.beginDrag }, 'cmd');

        control.addAction(\qtGui,{ { view.refresh }.defer })
    }
}
