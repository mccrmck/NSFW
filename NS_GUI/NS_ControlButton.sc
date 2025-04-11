NS_ControlButton : NS_ControlWidget {

    *new { |ns_control, statesArray|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control, statesArray)
    }

    init { |control, states|
        states = states ?? {[
            ["", NS_Style.textDark, NS_Style.bGroundLight],
            ["", NS_Style.textLight, NS_Style.bGroundDark]
        ]};

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

        view = Button() 
        .minWidth_(15)
        .font_( Font(*NS_Style.defaultFont) )
        .states_(states)
        .action_({ |but|
            var val = control.spec.constrain(but.value);
            control.value_(val, \qtGui)
        });
    
        control.addAction(\qtGui,{ |c| { view.value_(c.value) }.defer })
    }
}
