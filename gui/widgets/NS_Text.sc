NS_Text : NS_Widget {
    var <value;

    *new { |string|
        ^super.new.drawWidget(string.asString)
    }

    drawWidget { |string|
        var inset = NS_Style('inset');
        var halfInset = inset / 2;
        value = string;

        view = UserView()
        .minHeight_(20)
        .minWidth_(40)
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.width;
            var wIn = w + inset;
            var h = rect.height;
            var hIn = h + inset;
            var r = w.min(h) / 2;

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.fillColor_(NS_Style('bGroundLight'));
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(halfInset, halfInset, wIn, hIn), r, r);
            Pen.fillStroke;

            Pen.stringCenteredIn( 
                value,
                Rect(inset, inset, w, h),
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
