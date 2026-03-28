NS_Text : NS_Widget {
    var <value;

    *new { |string|
        ^super.new.drawWidget(string.asString)
    }

    drawWidget { |string|
        value = string;

        view = UserView()
        .minHeight_(20)
        .minWidth_(40)
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.fillColor_(NS_Style('bGroundLight'));
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.fillStroke;

            Pen.stringCenteredIn( 
                value,
                Rect(0, 0, w, h),
                Font(*NS_Style('defaultFont')),
                NS_Style('textDark')
            );
            Pen.stroke;
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) });

        this.addLeftClickAction({ });
        this.addDoubleClickAction({ });
    }

    string { ^value }

    string_ { |str|
        this.value_(str)
    }

    value_ { |str|
        value = str.asString;
        view.refresh;
    }
}
