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
        var inset = NS_Style('inset');
        var halfInset = inset / 2;

        view = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
            var string;
            var normVal = control.normValue;
            var rect = v.bounds.insetBy(inset);
            var w = rect.width;
            var wIn = w + inset;
            var wHalf = w + halfInset;
            var h = rect.height;
            var hIn = h + inset;
            var hHalf = h + halfInset;
            var r = w.min(h) / 2;

            var border = case
            { control.mapped == 'listening' }{ NS_Style('listening') }
            { control.mapped == 'mapped'    }{ NS_Style('assigned')  }
            { NS_Style('bGroundDark') };

            Pen.addRoundedRect(Rect(0,0,v.bounds.width, v.bounds.height), r, r);
            Pen.clip;

            Pen.fillColor_(NS_Style('highlight'));

            if(orientation,{
                string = control.label ++ ": " ++ control.value.round(round).asString;
                Pen.addRoundedRect(Rect(halfInset, halfInset, wIn * normVal, hIn), r, r)
            },{
                string = control.label ++ ":\n" ++ control.value.round(round).asString;
                Pen.addRoundedRect(
                    Rect(halfInset, halfInset + (1 - normVal * hHalf), wIn, hIn * normVal), r, r
                );
            });
            Pen.fill;

            Pen.strokeColor_(border);
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(halfInset, halfInset, wIn, hIn), r, r);
            Pen.stroke;

            Pen.stringCenteredIn( 
                string, 
                Rect(inset, inset, w, h),
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
