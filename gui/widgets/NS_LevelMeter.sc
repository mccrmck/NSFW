NS_LevelMeter : NS_Widget {
    var <isHighlighted = false;
    var <value;

    *new { |string, orientation('horz')|
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
        value = [0, 0];

        view = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
            var peak = value[0].ampdb.linlin(-80, 0, 0, 1);
            var rms = value[1].ampdb.linlin(-80, 0, 0, 1);

            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');
            var bCol = if(isHighlighted)
            { NS_Style('bGroundLight') }
            { NS_Style('bGroundDark') };

            var colors = value.collect({ |val|
                case
                { val >=   1 } { NS_Style('red') }
                { val >= 0.9 } { NS_Style('orange') }
                { NS_Style('green') }
            });

            Pen.addRoundedRect(Rect(0, 0, w, h), r, r);
            Pen.clip;

            if(orientation,{
                var hh = h / 2;
                Pen.fillColor_(colors[0]);
                // peak gets a wee dot
                Pen.addOval(Rect((w * peak) - hh, hh / 2, hh, hh).insetBy(b));
                Pen.fill;
                Pen.fillColor_(colors[1]);
                Pen.addRoundedRect(Rect(0, 0, w * rms, h).insetBy(b / 2), r, r);
                Pen.fill;
            },{
                var hw = w / 2;
                Pen.fillColor_(colors[0]);
                // peak gets a wee dot
                Pen.addOval(Rect(hw / 2, (1 - peak) * h, hw, hw).insetBy(b));
                Pen.fill;
                Pen.fillColor_(colors[1]);
                Pen.addRoundedRect(
                    Rect(0, (1 - rms) * h, w, h * rms).insetBy(b / 2), r, r
                );
                Pen.fill
            });

            Pen.strokeColor_(bCol);
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.stroke;

            Pen.stringCenteredIn(
                string, 
                Rect(0, 0, w, h), 
                Font(*NS_Style('defaultFont')),
                NS_Style('textDark')
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
