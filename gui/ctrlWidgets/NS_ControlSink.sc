NS_ControlSink : NS_ControlWidget {

    *new { |ns_control|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.drawWidget(ns_control)
    }

    drawWidget { |control|
        var inset = NS_Style('inset');
        var halfInset = inset / 2;

        view = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.width;
            var wIn = w + inset;
            var h = rect.height;
            var hIn = h + inset;
            var r = w.min(h) / 2;

            var val = control.value;

            if(val.pathMatch.size == 1 and: { PathName(val).isFile },{
                val = PathName(val).fileName
            });

            Pen.fillColor_(NS_Style('highlight'));
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(halfInset, halfInset, wIn, hIn), r, r);
            Pen.fillStroke;
            Pen.stringCenteredIn(
                val, 
                Rect(inset, inset, w, h),
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
