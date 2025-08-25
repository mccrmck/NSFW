NS_Text : NS_Widget {
    var <value;

    *new { |string|
        ^super.new.init(string.asString)
    }

    init { |string|
        var inset = NS_Style('inset');
        value = string;

        mouseActionDict = ();

        view = UserView()
        .fixedHeight_(20)
        .minWidth_(40)
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.fillColor_(NS_Style('bGroundLight'));
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
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
