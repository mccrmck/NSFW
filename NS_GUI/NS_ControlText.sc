NS_ControlText : NS_ControlWidget {

    *new { |ns_control|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control)
    }

    init { |control|
        var inset = NS_Style('inset');

        mouseActionDict = ();

        view = UserView()
        .minHeight_(30)
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;

            var val = control.value;

            if(val.pathMatch.size == 1 and: { PathName(val).isFile },{
                val = PathName(val).fileName
            });

            Pen.fillColor_(NS_Style('highlight'));
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
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
