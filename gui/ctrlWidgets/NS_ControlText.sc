NS_ControlText : NS_ControlWidget {

    *new { |nsControl|
        nsControl ?? { "must provide an NS_Control".warn };
        ^super.new.drawWidget(nsControl)
    }

    drawWidget { |control|

        view = UserView()
        .minHeight_(30)
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');

            var val = control.value;

            if(val.pathMatch.size == 1) {
                if(val.last == Platform.pathSeparator) 
                { val = PathName(val).folderName }
                { val = PathName(val).fileName }
            };

            // no protection here from displaying a string that is not a path

            Pen.fillColor_(NS_Style('highlight'));
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.fillStroke;
            Pen.stringCenteredIn(
                "%:\n%".format(control.label, val), 
                Rect(0, 0, w, h),
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
                    TextField().action_({ |t|
                        control.value_(t.value.asString)
                    })
                )
            ).front
        });

        control.addAction("qtText" ++ this.hash, { |c| { view.refresh }.defer });
        view.onClose_({ control.removeAction("qtText" ++ this.hash) })
    }
}
