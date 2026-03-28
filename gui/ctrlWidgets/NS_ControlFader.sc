NS_ControlFader : NS_ControlWidget {
    var <>round;

    *new { |nsControl, round = 0.01, orientation = 'horz'|
        if(nsControl.isNil,{ "must provide an NS_Control".warn });
        orientation = switch(orientation,
            \horz,       { true },
            \horizontal, { true },
            \vert,       { false },
            \vertical,   { false },
            orientation
        );

        ^super.new.round_(round).drawWidget(nsControl, orientation)
    }

    drawWidget { |control, orientation|

        view = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
            var string;
            var normVal = control.normValue;
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');

            var bCol = case
            { control.mapped == 'listening' }{ NS_Style('listening') }
            { control.mapped == 'mapped'    }{ NS_Style('assigned')  }
            { NS_Style('bGroundDark') };

            Pen.addRoundedRect(Rect(0, 0, w, h), r + (b / 2), r + (b / 2));
            Pen.clip;

            Pen.fillColor_(NS_Style('highlight'));

            if(orientation,{
                string = control.label ++ ": " ++ control.value.round(round).asString;
                Pen.addRoundedRect(Rect(0, 0, w * normVal, h), r, r)
            },{
                string = control.label ++ ":\n" ++ control.value.round(round).asString;
                Pen.addRoundedRect(
                    Rect(0, (1 - normVal) * h, w, h * normVal), r, r
                );
            });
            Pen.fill;

            Pen.strokeColor_(bCol);
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.stroke;

            Pen.stringCenteredIn( 
                string, 
                Rect(0, 0, w, h),
                Font(*NS_Style('defaultFont')),
                NS_Style('textLight')
            );
            Pen.stroke;
        })
        .beginDragAction_({ control })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseMoveAction_({ |v, x, y, modifiers|
            var val = if(orientation, {
                (x / v.bounds.width).clip(0, 1)
            },{
                1 - (y / v.bounds.height).clip(0, 1)
            });

            control.normValue_(val);
        });

        this.addLeftClickAction({ |f, v, x, y|
            var val = if(orientation, {
                (x / v.bounds.width).clip(0, 1)
            },{
                1 - (y / v.bounds.height).clip(0, 1)
            });

            control.normValue_(val)
        });
        this.addDoubleClickAction({ |...args| 
            mouseActionDict['none']['leftClick'].value(*args)
        });
        this.addLeftClickAction({ this.toggleAutoAssign(control) }, 'shift');
        this.addRightClickAction({ this.openControlMenu(control) });
        this.addLeftClickAction({ view.beginDrag }, 'cmd');

        control.addAction(\qtGui,{ |c| { view.refresh }.defer  });
    }
}
