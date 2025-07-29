NS_LevelMeter : NS_Widget {
    var string, spec, isHighlighted = false;
    var <value;

    *new { |string, orientation = 'horz'|
         orientation = switch(orientation,
            \horz,       { true },
            \horizontal, { true },
            \vert,       { false },
            \vertical,   { false },
            orientation
        );
        ^super.new.init(string.asString, orientation)
    }

    init { |string, orientation|
        var inset  = NS_Style.inset;
        var font   = Font(*NS_Style.defaultFont);

        mouseActionDict = ();

        //spec = ControlSpec(-80.dbamp, 1, \amp);
        //value = [spec.default, spec.default];
        value = [0, 0];

        view = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
            var colors;
            var peak = value[0].ampdb.linlin(-80, 0, 0, 1);
            var rms = value[1].ampdb.linlin(-80, 0, 0, 1);
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;
            var border = if(isHighlighted,{ 
                NS_Style.bGroundLight
            },{
                NS_Style.bGroundDark
            });

            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.clip;

            colors = value.collect({ |val|
                case
                { val >=   1 } { NS_Style.red }
                { val >= 0.9 } { NS_Style.orange }
                { NS_Style.green }
            });

            if(orientation,{
                Pen.fillColor_(colors[0]);
                // peak gets a wee dot
                Pen.addOval(Rect((w * peak) - (h/2) + inset, inset + (h/4), h/2, h/2));
                Pen.fill;
                Pen.fillColor_(colors[1]);
                Pen.addRoundedRect(Rect(inset, inset, w * rms, h), r, r);
                Pen.fill;
            },{
                Pen.fillColor_(colors[0]);
                // peak gets a wee dot
                Pen.addOval(Rect(inset + (w/4), (1-peak * h) + inset, w/2, w/2));
                Pen.fill;
                Pen.fillColor_(colors[1]);
                Pen.addRoundedRect( Rect(inset, inset + (1-rms * h), w, h * rms), r, r);
                Pen.fill
            });

            Pen.strokeColor_(border);
            Pen.width_(inset * 2);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.stroke;

            Pen.stringCenteredIn(
                string, Rect(inset, inset, w, h), font, NS_Style.textDark
            );
            Pen.stroke;
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .beginDragAction_({ string });
    }

    value_ { |peak, rms|
        //value = [spec.constrain(peak), spec.constrain(rms)];
        value = [peak, rms];
        view.refresh;
    }

    highlight { |boolean|
        isHighlighted = boolean;
        view.refresh;
    }
}
