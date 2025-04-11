NS_ControlKnob : NS_ControlWidget {
    var text, knob, numBox;
    var <round;

    *new { |ns_control, round|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        
        ^super.new.init(ns_control, round ? 0.01)
    }

    init { |control, inRound|

        round = inRound;

        text = if(control.label.notNil,{
            StaticText()
            .string_(control.label)
            .align_(\center);
        });

        knob = Knob()
        .mode_(\vert)
        .color_([
            NS_Style.bGroundLight,
            NS_Style.textDark,
            NS_Style.transparent, 
            NS_Style.bGroundDark
        ])
        .value_( control.normValue )
        .action_({ |kn|
            control.normValue_(kn.value);
        });

        numBox = NumberBox()
        .align_(\center)
        .stringColor_(NS_Style.textDark)
        .background_(NS_Style.bGroundLight)
        .decimals_(round.asString.split($.)[1].size)
        .maxWidth_(60)
        .value_(control.value)
        .action_({ |nb| 
            var val = control.spec.constrain(nb.value);
            control.value_(val);
        });

        view = View();
        view.layout_( VLayout( text, knob, numBox) );
        view.layout.spacing_(0).margins_(0);

        control.addAction(\qtGui,{ |c| 
           { 
                knob.value_(c.normValue);
                numBox.value_(c.value);
            }.defer 
        });

    }

    round_ { |val|
        var decimals = val.asString.split($.)[1].size;
        numBox.decimals = decimals;
        round = val
    }

    stringColor_ { |val| text.stringColor_(val) }
}
