NS_ControlText : NS_ControlWidget {

    *new { |ns_control|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.drawWidget(ns_control)
    }

    drawWidget { |control|
        var inset = NS_Style('inset');
        var halfInset = inset / 2;

        view = UserView()
        .minHeight_(30)
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.width;
            var wIn = w + inset;
            var h = rect.height;
            var hIn = h + inset;
            var r = w.min(h) / 2;

            var val = control.value;

            if(val.pathMatch.size == 1) {
                if(val.last == Platform.pathSeparator) 
                { val = PathName(val).folderName }
                { val = PathName(val).fileName }
            };

            // no protection here from displaying a string that is not a path

            Pen.fillColor_(NS_Style('highlight'));
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(halfInset, halfInset, wIn, hIn), r, r);
            Pen.fillStroke;
            Pen.stringCenteredIn(
                "%:\n%".format(control.label, val), 
                Rect(inset, inset, w, h),
                Font(*NS_Style('defaultFont')),
                NS_Style('textDark')
            );
            Pen.stroke
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) });

        this.addLeftClickAction({ });
        this.addDoubleClickAction({ });
        this.addRightClickAction({
            Menu(
                CustomViewAction(
                    TextField()
                    .action_({ |t|
                        control.value_(t.value.asString)
                    })
                )
            ).front
        });

        control.addAction(\qtGui,{ |c| { view.refresh }.defer })
    }
}
