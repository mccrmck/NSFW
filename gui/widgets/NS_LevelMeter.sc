NS_LevelMeter : NS_Widget {
    var <isHighlighted = false;
    var <value;

    *new { |string, orientation = 'horz'|
         orientation = switch(orientation,
            \horz,       { true },
            \horizontal, { true },
            \vert,       { false },
            \vertical,   { false },
            orientation
        );
        ^super.new.drawWidget(string.asString, orientation)
    }

    drawWidget { |string, orientation|
        var inset  = NS_Style('inset');
        var halfInset = inset / 2;
        var font   = Font(*NS_Style('defaultFont'));

        value = [0, 0];

        view = UserView()
        //.background_(Color.black)
        .minHeight_(20)
        .drawFunc_({ |v|
            var colors;
            var peak = value[0].ampdb.linlin(-80, 0, 0, 1);
            var rms = value[1].ampdb.linlin(-80, 0, 0, 1);
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var wIn = w + inset;
            var wHalf = w + halfInset;
            var h = rect.bounds.height;
            var hIn = h + inset;
            var hHalf = h + halfInset;
            var r = w.min(h) / 2;
            var border = if(isHighlighted,{ 
                NS_Style('bGroundLight')
            },{
                NS_Style('bGroundDark')
            });

            Pen.addRoundedRect(Rect(0, 0, v.bounds.width, v.bounds.height), r, r);
            Pen.clip;

            colors = value.collect({ |val|
                case
                { val >=   1 } { NS_Style('red') }
                { val >= 0.9 } { NS_Style('orange') }
                { NS_Style('green') }
            });

            if(orientation,{
                var vh = v.bounds.height / 2;
                Pen.fillColor_(colors[0]);
                // peak gets a wee dot
                Pen.addOval(Rect((wIn * peak) - vh, vh / 2, vh, vh));
                Pen.fill;
                Pen.fillColor_(colors[1]);
                Pen.addRoundedRect(Rect(halfInset, halfInset, wIn * rms, hIn), r, r);
                Pen.fill;
            },{
                var vw = v.bounds.width / 2;
                Pen.fillColor_(colors[0]);
                // peak gets a wee dot
                Pen.addOval(Rect(vw / 2, (1-peak * hIn) + inset, vw, vw));
                Pen.fill;
                Pen.fillColor_(colors[1]);
                Pen.addRoundedRect(
                    Rect(halfInset, halfInset + (1 - rms * hHalf), wIn, hIn * rms), r, r
                );
                Pen.fill
            });

            Pen.strokeColor_(border);
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(halfInset, halfInset, wIn, hIn), r, r);
            Pen.stroke;


            Pen.stringCenteredIn(
                string, Rect(inset, inset, w, h), font, NS_Style('textDark')
            );
            Pen.stroke;
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .beginDragAction_({ string });

        this.addLeftClickAction({ });
        this.addDoubleClickAction({ });
    }

    value_ { |peak, rms|
        value = [peak, rms];
        view.refresh;
    }

    highlight { |boolean|
        isHighlighted = boolean;
        view.refresh;
    }
}
