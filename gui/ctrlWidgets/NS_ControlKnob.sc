NS_ControlKnob : NS_ControlWidget {
    var <>round;

    *new { |ns_control, round(0.01)|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.round_(round).drawWidget(ns_control)
    }

    drawWidget { |control|

        mouseActionDict = ();

        view = UserView()
        .minHeight_(20)
        .minWidth_(20)
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

            Pen.width_(b);

            string = control.label ++ ":\n" ++ control.value.round(round).asString;

            Pen.fillColor_(NS_Style('highlight'));
            Pen.strokeColor_(bCol);
            Pen.addAnnularWedge(
                Rect(0, 0, w, h).insetBy(b).center, 
                r * 0.6,
                r - (b/2), 
                pi/2, 
                normVal * 2pi
            );
            Pen.fillStroke;

            Pen.stringCenteredIn( 
                string, 
                Rect(0, 0, w, h), 
                Font(*NS_Style('defaultFont')), 
                NS_Style('textDark')
            );
            Pen.stroke;
        })
        .beginDragAction_({ control })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseMoveAction_({ |v, x, y, modifiers|
            control.normValue_( 1 - (y / v.bounds.height).clip(0, 1) );
            view.refresh;
        });

        this.addLeftClickAction({ |k, v, x, y|
            control.normValue_( 1 - (y / v.bounds.height).clip(0, 1) )
        });
        this.addDoubleClickAction({ |...args| 
            mouseActionDict['none']['leftClick'].value(*args)
        });
        this.addLeftClickAction({ this.toggleAutoAssign(control) }, 'shift');
        this.addRightClickAction({ this.openControlMenu(control) });

        control.addAction("qtKnob" ++ this.hash, { |c| { view.refresh }.defer });
        view.onClose_({ control.removeAction("qtKnob" ++ this.hash) })
    }
}
