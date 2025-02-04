NS_ControlKnob {
    var <view, text, knob, numBox;
    var <round = 0.01;

    *new { |ns_control|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        
        ^super.new.init(ns_control)
    }

    init { |control, orientation|
        view = View();

        text = if(control.label.size > 0,{
            StaticText()
            .string_(control.label)
            .align_(\center);
        },{
            nil
        });

        knob = Knob()
        .mode_(\vert)
        .color_([Color.white, Color.black, Color.clear, Color.black])
        .value_( control.normValue )
        .action_({ |kn|
            control.normValue_(kn.value);
        });

        numBox = NumberBox()
        .align_(\center)
        .value_(control.value)
        .action_({ |nb| 
            var val = control.spec.constrain(nb.value);
            control.value_(val);
        });


        view.layout_( VLayout( text, knob, numBox) );
        view.layout.spacing_(0).margins_(0);

        control.addAction(\qtGui,{ |c| 
           { 
                knob.value_(c.normValue);
                numBox.value_(c.value);
            }.defer 
        });

    }

    layout { ^view.layout }
    asView { ^view }

    round_ { |val|
        var decimals = val.asString.split($.)[1].size;
        numBox.decimals = decimals;
        round = val
    }

    maxHeight_ { |val| view.maxHeight_(val) }
    minHeight_ { |val| view.minHeight_(val) }
    maxWidth_  { |val| view.maxWidth_(val) }
    minWidth_  { |val| view.minWidth_(val) }

    stringColor_ { |val| text.stringColor_(val) }
}
