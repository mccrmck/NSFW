NS_ControlKnob : NS_ControlWidget {
    var <>round;

    *new { |ns_control, round = 0.01|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.round_(round).init(ns_control)
    }

    init { |control|
        var inset = NS_Style('inset');

        mouseActionDict = ();

        view = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
            var string;
            var normVal = control.normValue;
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;

            var border = case
            { control.mapped == 'listening' }{ NS_Style('listening') }
            { control.mapped == 'mapped'    }{ NS_Style('assigned')  }
            { NS_Style('bGroundDark') };

            Pen.width_(inset);

            string = control.label ++ ":\n" ++ control.value.round(round).asString;

            Pen.stringCenteredIn( 
                string, 
                Rect(inset, inset, w, h), 
                Font(*NS_Style('defaultFont')), 
                NS_Style('textDark')
            );
            Pen.stroke;

            Pen.fillColor_(NS_Style('highlight'));
            Pen.strokeColor_(border);
            Pen.addAnnularWedge(
                Rect(inset, inset, w, h).center, r * 0.75, r, pi/2, normVal * 2pi
            );
            Pen.fillStroke;
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
        this.addLeftClickAction({ this.toggleAutoAssign(control, 'continuous') }, 'shift');
        this.addRightClickAction({ this.openControlMenu(control, 'continuous') });
        this.addLeftClickAction({ view.beginDrag }, 'cmd');

        control.addAction(\qtGui,{ |c| { view.refresh }.defer });
    }
}
