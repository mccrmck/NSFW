NS_MLPMeter : NS_Widget { 
    var <module, <control;

    *new {
        ^super.new.init
    }

    init {
        var inset  = NS_Style('inset');
        var font   = Font(*NS_Style('defaultFont'));

        mouseActionDict = ();

        view = UserView()
        .minHeight_(36)
        .drawFunc_({ |v|
            var value = control !? { control.normValue } ?? { 0 };
            var string = control !? {
                var modName = module.class.asString.split($_)[1];
                "% %:\n%".format(modName, control.label, control.value.round(0.01)) 
            } ?? { "" };
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;
            var border = NS_Style('bGroundDark');

            // clip outline
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.clip;

            //draw fader
            Pen.fillColor_( NS_Style('highlight') );
            Pen.addRoundedRect(Rect(inset, inset, w * value, h), r, r);
            Pen.fill;

            // draw border
            Pen.strokeColor_(border);
            Pen.width_(inset * 2);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.stroke;

            // draw label
            Pen.stringCenteredIn(
                string, Rect(inset, inset, w, h), font, NS_Style('textDark')
            );
            Pen.stroke;
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseMoveAction_({ |v, x, y, modifiers|
           control !? { control.normValue_(x / v.bounds.width) }
        });

        this.addLeftClickAction({ |m, v, x, y|
            control !? { control.normValue_(x / view.bounds.width) } 
        });
        this.addLeftClickAction({ this.free }, 'alt');
    }

    assignControl { |nsModule, nsControl|
        module  = nsModule;
        control = nsControl;

        if(control.actionDict['mlpMeter'].notNil,{
            "this control already assigned to an MLPMeter".warn
        },{
            control.addAction(\mlpMeter,{ |c| { view.refresh }.defer  });
        });

        view.refresh;
    }

    free { 
        control !? { control.removeAction(\mlpMeter) };
        control = nil;
        view.refresh
    }
}
