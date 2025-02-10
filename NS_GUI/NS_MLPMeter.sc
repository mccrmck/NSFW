NS_MLPMeter {
    var modName, argLabel, <control;
    var text, uView, <view;

    *new {
        ^super.new.init
    }

    init {
        text = StaticText().stringColor_( Color.white );
        uView = UserView().drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            // Draw the frame
            Pen.strokeColor_( Color.black );
            Pen.addRect( Rect(0, 0, w, h) );
            Pen.stroke;
        })
        .canReceiveDragHandler_({ |view, x, y|
            var mod = View.currentDrag[0];
            var index = View.currentDrag[1];
            mod.controls[index].isKindOf( NS_Control )
        })
        .receiveDragHandler_({ |view, x, y|
            var module = View.currentDrag[0];
            var ctrlIndex = View.currentDrag[1];
            // var type = View.currentDrag[2]; 

            this.assignControl(module, ctrlIndex);
        });

        view = View().minHeight_(30);
        view.layout_( StackLayout(uView, text).mode_(1) );
        view.layout.spacing_(0).margins_(0)
    }

    assignControl { |module, ctrlIndex|
        modName = module.class.asString.split($_)[1];
        control = module.controls[ctrlIndex];
        argLabel = control.label;

        if(control.actionDict['mlpMeter'].notNil,{
            "this control already assigned to an MLPMeter".warn
        },{
            control.addAction(\mlpMeter,{ |c| { uView.refresh }.defer  })
        });

        uView.drawFunc_({ |v|
            var normVal = control.normValue;
            var w = v.bounds.width;
            var h = v.bounds.height;

            // Draw the fill
            Pen.fillColor_( Color.white.alpha_( 0.25 ) );
            Pen.addRect( Rect(0, 0, normVal * w, h) );
            Pen.fill;
            // Draw the mark 
            Pen.strokeColor_( Color.black );
            Pen.moveTo( Point(normVal * w, 0) );
            Pen.lineTo( Point(normVal * w, h) );
            Pen.stroke;
            // Draw the frame
            Pen.strokeColor_( Color.black );
            Pen.addRect( Rect(0, 0, w, h) );
            Pen.stroke;

            // left align + space to avoid update jitter
            text.align_(\left).string_( " %\n %: %".format(modName, argLabel, control.value.round(0.01)) )
        })
        .mouseDownAction_({ |v, x, y, mod, butNum, clicks| 
            if(mod.isShift,{ this.free },{ control.normValue_( x/v.bounds.width ) })
        })
        .mouseMoveAction_({ |v, x, y, mod|
            control.normValue_( x/v.bounds.width )
        });
    }

    asView { ^view }

    free { 
        text.string_("");
        uView.drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            // Draw the frame
            Pen.strokeColor_( Color.black );
            Pen.addRect( Rect(0, 0, w, h) );
            Pen.stroke;
        })
        .mouseDownAction_( nil )
        .mouseMoveAction_( nil )
        .refresh;
        control.removeAction(\mlpMeter);
        control = nil;
    }

}
