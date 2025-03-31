NS_ControlButton {
    var <view, button;

    *new { |ns_control, statesArray|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control, statesArray)
    }

    init { |control, states|
        view = View();
        states = states ?? { [["", Color.black, Color.white], ["", Color.white, Color.black]] };

        states = states.collect({ |state, index|

            switch(state.class,
                String,{
                    [
                        [state, NS_Style.textDark, NS_Style.bGroundLight],
                        [state, NS_Style.textLight, NS_Style.bGroundDark]
                    ].at(index)
                },
                Array,{
                    state
                }
            );
        });

        button = Button() 
        .minWidth_(15)
        .font_( Font(*NS_Style.defaultFont) )
        .states_(states)
        .action_({ |but|
            var val = control.spec.constrain(but.value);
            control.value_(val, \qtGui)
        });
    
        view.layout_( VLayout( button ) );

        view.layout.spacing_(0).margins_(0);

        control.addAction(\qtGui,{ |c| { button.value_(c.value) }.defer })
    }

    layout { ^view.layout }
    asView { ^view }

    maxHeight_ { |val| view.maxHeight_(val) }
    minHeight_ { |val| view.minHeight_(val) }
    maxWidth_  { |val| view.maxWidth_(val) }
    minWidth_  { |val| view.minWidth_(val) }
}
