NS_MLPMeter : NS_Widget { 
    var <>control;

    *new { |nsControl|
        ^super.new.control_(nsControl).drawWidget
    }

    drawWidget { 

        view = UserView()
        .background_(Color.red)
        .minHeight_(20)
        .drawFunc_({ |v|
            var value = control !? { control.normValue } ?? { 0 };
            var string = control !? {
                "%: %".format(control.label, control.value.round(0.01)) 
            } ?? { "" };
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');

            // clip outline
            Pen.addRoundedRect(Rect(0, 0, w, h), r, r);
            Pen.clip;

            //draw fader
            Pen.fillColor_( NS_Style('highlight') );
            Pen.addRoundedRect(Rect(0, 0, w * value, h).insetBy(b / 2), r, r);
            Pen.fill;

            // draw border
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.stroke;

            // draw label
            Pen.stringLeftJustIn(
                string, 
                Rect((b * 2).max(6), 0, w, h),
                Font(*NS_Style('smallFont')),
                NS_Style('textDark'),
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
