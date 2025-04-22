NS_ControlMenu : NS_ControlWidget {

    *new { |ns_control, labelArray|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control, labelArray)
    }

    init { |control, labels|
        var text = if(control.label.size > 0,{
            var lbl = "%: ".format(control.label);
            StaticText()
            .string_( lbl )
            .maxWidth_( lbl.bounds.width + 2 )
            .align_(\left);
        });        

        var popUp = PopUpMenu()
        .items_( labels.asArray )
        .action_({ |menu|
            var val = control.spec.constrain( menu.value );
            control.value_( val )
        });

        view = View();
        view.layout_( HLayout( text, popUp ) );

        view.layout.spacing_(0).margins_(0);

        control.addAction(\qtGui,{ |c| { popUp.value_( c.value ) }.defer })
    }
}
