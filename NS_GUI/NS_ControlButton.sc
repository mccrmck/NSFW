NS_ControlButton {
    var <view, button;

    *new { |ns_control, labelArray|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control, labelArray)
    }

    init { |control, labels|
        view = View();

        // fix the label/color input argument;
        // default should be:
        // [["",Color.black,Color.white],["",Color.white,Color.black]]

        button = Button() 
        .states_([
            [ labels[0] ? "", Color.black, Color.white ], 
            [ labels[1] ? "", Color.white, Color.black ] 
        ])
        .action_({ |but|
            var val = control.spec.constrain(but.value);
            control.value_(val, \qtGui)
        });

        view.layout_( VLayout( button ) );

        view.layout.spacing_(0).margins_(0);

        control.addAction(\qtGui,{ |c|
            { button.value_(c.value) }.defer
        })
    }

    layout { ^view.layout }
    asView { ^view }

    maxHeight_ { |val| view.maxHeight_(val) }
    minHeight_ { |val| view.minHeight_(val) }
    maxWidth_  { |val| view.maxWidth_(val) }
    minWidth_  { |val| view.minWidth_(val) }
}
