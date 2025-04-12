NS_ControlSwitch : NS_ControlWidget {

    *new { |ns_control, labelArray, numColumns = 1|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control, labelArray, numColumns.max(1))
    }

    init { |control, labels, columns|

        var buttons = labels.collect({ |label, index|
            Button() 
            .states_([ 
                [label.asString, NS_Style.textDark, NS_Style.bGroundLight], 
                [label.asString, NS_Style.textLight, NS_Style.bGroundDark]
            ])
            .action_({
                var val = control.spec.constrain(index);
                control.value_(val)
            });
        });

        view = View();
        view.layout_(
            VLayout(
                *buttons.clump(columns.asInteger).collect({ |row, index|
                    HLayout(*row)
                })
            )
        );

        view.layout.spacing_(2).margins_(0);

        control.addAction(\qtGui,{ |c|
            var val = c.value.asInteger.wrap(0, buttons.size - 1);
            {
                buttons.do({ |but| but.value_(0) });
                buttons[val].value_(1);
            }.defer
        })
    }
}
