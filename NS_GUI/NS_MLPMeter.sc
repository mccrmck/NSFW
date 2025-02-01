NS_MLPMeter {
    var <module, <ctrlIndex, <type, <label, <value = 0;
    var <spec;
    var string, uView, <view;

    *new {
        ^super.new.init
    }

    init {
        spec = spec ?? ControlSpec(0,1,'lin');
        string = StaticText().align_(\center);
        uView = UserView();

        uView.drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            // Draw the frame
            Pen.strokeColor = Color.black;
            Pen.addRect(Rect(0, 0, w, h));
            Pen.stroke;
        })
        .canReceiveDragHandler_({ |view, x, y|
            View.currentDrag[0].class.superclasses.includes( NS_ControlModule )
        })
        .receiveDragHandler_({ |view, x, y|
            module = View.currentDrag[0];
            ctrlIndex = View.currentDrag[1];
            type = View.currentDrag[2];

            // must be able to call .label on all GUI elements here!
            label = ctrlIndex; // this is temporary

            this.drawSliders( module.controls[ctrlIndex].value );

            this.value_( module.controls[ctrlIndex].value );
            // [module, index, type].postln;
        });

        view = View();
        view.layout_( StackLayout( uView, string ).mode_(1) );
        view.layout.spacing_(0).margins_(0)
    }

    

    // this is complete garbage, needs to be cleaned up when I refactor all gui elements
    drawSliders { |ctlVal|

        if( ctlVal.asArray.size == 1,{

            spec = module.controls[ctrlIndex].spec;
            uView.drawFunc_({ |v|
                var w = v.bounds.width;
                var h = v.bounds.height;

                // Draw the fill
                Pen.fillColor = Color.gray.alpha_(0.2);
                Pen.addRect(Rect(0, 0, w * spec.unmap( value ), h));
                Pen.fill;
                // Draw the mark 
                Pen.strokeColor_( Color.black );
                Pen.moveTo(Point(w * spec.unmap( value ), 0));
                Pen.lineTo(Point(w * spec.unmap( value ), h));
                Pen.stroke;
                // Draw the frame
                Pen.strokeColor = Color.black;
                Pen.addRect(Rect(0, 0, w, h));
                Pen.stroke;
            })
            .mouseDownAction_({ |view, x, y, mod, butNum, clicks|
                var val = spec.map( x / view.bounds.width );
                this.value_( val )
            })
        },{
            spec = module.controls[ctrlIndex].specs;
            uView.drawFunc_({ |v|
                var w = v.bounds.width;
                var h = v.bounds.height;
                // Draw the fills
                Pen.fillColor = Color.gray.alpha_(0.2);
                Pen.addRect(Rect(0, 0, w * spec[0].unmap( value[0] ), h/2));
                Pen.addRect(Rect(0, h/2, w * spec[1].unmap( value[1] ), h/2));
                Pen.fill;
                // Draw the marks
                Pen.strokeColor_( Color.black );
                Pen.moveTo(Point(w * spec[0].unmap( value[0] ), 0));
                Pen.lineTo(Point(w * spec[0].unmap( value[0] ), h/2));
                Pen.moveTo(Point(w * spec[1].unmap( value[1] ), h/2));
                Pen.lineTo(Point(w * spec[1].unmap( value[1] ), h));
                Pen.stroke;
                // Draw the frame
                Pen.strokeColor = Color.black;
                Pen.addRect(Rect(0, 0, w, h));
                Pen.stroke;
            })
            .mouseDownAction_({ |view, x, y, mod, butNum, clicks|
                var val = value;
                if(y < (view.bounds.height/2),{
                    val[0] = spec[0].map( x / view.bounds.width )
                },{
                    val[1] = spec[1].map( x / view.bounds.width )
                });
                this.value_( val )
            })

        });

        uView.mouseMoveAction = uView.mouseDownAction;
    }

    value_ { |val|
        value = val;
        string.string_( "%/%: %".format(module.class, label, value.round(0.01)) );
        if(module.notNil,{
            // gotta MVC this shit right here:
            module.controls[ctrlIndex].value_( value )
        });
        uView.refresh
    }

    asView { ^view }

}
