NS_MLPMeter : NS_Widget { 
    var <>control;

    *new { |nsControl|
        ^super.new.control_(nsControl).init
    }

    init { 
        var inset  = NS_Style('inset');
        var font   = Font(*NS_Style('defaultFont'));

        mouseActionDict = ();

        view = UserView()
        .minHeight_(22)
        .drawFunc_({ |v|
            var value = control !? { control.normValue } ?? { 0 };
            var string = control !? {
                "%: %".format(control.label, control.value.round(0.01)) 
            } ?? { "" };
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;
            var border = NS_Style('bGroundDark');

            // clip outline
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.clip;

            //draw fader
            Pen.fillColor_( NS_Style('highlight') );
            Pen.addRoundedRect(Rect(inset, inset, w * value, h), r, r);
            Pen.fill;

            // draw border
            Pen.strokeColor_(border);
            Pen.width_(inset * 2);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.stroke;

            // draw label
            Pen.stringLeftJustIn(
                string, Rect(inset + 6, inset, w - 6, h), font, NS_Style('textDark'),
            );
            Pen.stroke;
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseMoveAction_({ |v, x, y, modifiers|
           control !? { control.normValue_(x / v.bounds.width) }
        });

        this.addLeftClickAction({ |m, v, x, y|
            control !? { control.normValue_(x / v.bounds.width) } 
        });
        this.addDoubleClickAction({ |...args| 
            mouseActionDict['none']['leftClick'].value(*args)
        });

        control.addAction(\mlpMeter,{ |c| { view.refresh }.defer  });
    }

    free { 
        control.removeAction(\mlpMeter);
        control = nil;
        view.refresh
    }
}
