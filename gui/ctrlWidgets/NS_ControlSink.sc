NS_ControlSink : NS_ControlWidget {

    *new { |ns_control|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.drawWidget(ns_control)
    }

    drawWidget { |control|

        view = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');

            var val = control.value;

            if(val.pathMatch.size == 1 and: { PathName(val).isFile },{
                val = PathName(val).fileName
            });

            Pen.fillColor_(NS_Style('highlight'));
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.fillStroke;
            Pen.stringCenteredIn(
                val, 
                Rect(0, 0, w, h),
                Font(*NS_Style('defaultFont')),
                NS_Style('textDark')
            );
            Pen.stroke
        })
        .canReceiveDragHandler_({ View.currentDrag.isString })
        .receiveDragHandler_({
            var string = View.currentDrag;
            string = control.spec !? { control.spec.constrain(string) } ?? { string };
            control.value_(string);
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .beginDragAction_({ control.value });

        this.addLeftClickAction({ });
        this.addDoubleClickAction({ });
        this.addLeftClickAction({ control.resetValue }, 'alt');
        
        control.addAction(\qtGui,{ |c| { view.refresh }.defer })
    }
}
