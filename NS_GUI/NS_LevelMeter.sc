NS_LevelMeter {
    var string, spec, highlight = 0;
    var <action, <view;
    var <value;

    *new { |string, orientation = 'horz'|
        orientation = switch(orientation,
            \horz, { \horizontal },
            true,  { \horizontal },
            \vert, { \vertical },
            false, { \vertical },
            orientation
        );
        ^super.newCopyArgs(string.asString).init(orientation)
    }

    init { |orientation|
        var inset = 2;
        var font = Font(*NS_Style.defaultFont);
        var borderCol = [NS_Style.darklight, NS_Style.highlight];

        spec = \amp.asSpec;
        value = spec.default;

        view = UserView()
        // these don't update on string update, obvi
        .minWidth_(string.bounds(font).width + (inset * 2) + 4) // 4px for padding?
        .minHeight_(string.bounds(font).height + (inset * 2) + 4) // 4px for padding?
        .drawFunc_({ |v|
            var val = spec.unmap(value);
            var rect = v.bounds.insetBy(inset);
            var l = inset;
            var t = inset;
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;

            Pen.addRoundedRect(Rect(l, t, w, h), r, r);
            Pen.clip;

            Pen.fillColor_(
                case
                { val >= -0.5.dbamp } { NS_Style.muteRed }
                { val > -1.5.dbamp } { NS_Style.orange }
                { NS_Style.playGreen }
            );

            if(orientation == 'vertical',{
                Pen.addRoundedRect(Rect(l, t + (1-val * h), w, h * val), r, r)
            },{
                Pen.addRoundedRect(Rect(l, t, w * val, h), r, r)
            });
            Pen.fill;

            Pen.strokeColor_(borderCol[highlight]);
            Pen.width_(inset * 2);
            Pen.addRoundedRect(Rect(l, t, w, h), r, r);
            Pen.stroke;

            Pen.stringCenteredIn(string, Rect(l, t, w, h), font, NS_Style.textDark);
            Pen.stroke;
        })
        .mouseDownAction_({ |view, x, y, modifiers, buttonNumber, clickCount|
            switch(buttonNumber, 
                0,{ action.value(this) },
                1,{ 
                    var menu = Menu().front;
                    menu.addAction( 
                        CustomViewAction( 
                            TextField().action_({ |tf|
                                this.string_( tf.string );
                                menu.visible_(false)
                            })
                        )
                    )
                }
            )
        })
        .dragLabel_(string)
        .beginDragAction_({ |view, x, y|
            string.split($ )[1].asInteger;
        });
    }

    value_ { |val|
        value = spec.constrain(val);
        view.refresh;
    }

    action_ { |func|
        action = func;
    }

    string_ { |inString|
        string = inString;
        view.refresh
    }

    toggleHighlight {
        highlight = 1 - highlight;
        view.refresh
    }

    asView { ^view }
}
