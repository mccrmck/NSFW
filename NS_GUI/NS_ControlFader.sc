NS_ControlFader : NS_ControlWidget {
    var text, slider, numBox;
    var <round;

    *new { |ns_control, round, orientation = 'horz'|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        orientation = switch(orientation,
            \horz, { \horizontal },
            true,  { \horizontal },
            \vert, { \vertical },
            false, { \vertical },
            orientation
        );
        ^super.new.init(ns_control, round ? 0.01, orientation)
    }

    init { |control, inRound, orientation|

        round = inRound;

        text = if(control.label.notNil,{
            StaticText()
            .string_(control.label)
            .align_(\center);
        });

        slider = Slider()
        .background_(NS_Style.transparent)
        .knobColor_(NS_Style.transparent)
        .thumbSize_(10)
        .value_(control.normValue)
        .orientation_( orientation )
        .action_({ |sl|
            control.normValue_(sl.value);
        });

        numBox = NumberBox()
        .align_(\center)
        .normalColor_(NS_Style.textDark)
        .stringColor_(NS_Style.textDark)
        .background_(NS_Style.bGroundLight)
        .decimals_(round.asString.split($.)[1].size)
        //.maxHeight_(90)
        .fixedWidth_(45)
        .value_(control.value)
        .action_({ |nb| 
            var val = control.spec.constrain(nb.value);
            control.value_(val);
        });

        view = View();

        switch(orientation,
            \vertical,   { view.layout = VLayout() },
            \horizontal, { view.layout = HLayout() },
        );

        view.layout.add(text).add(slider).add(numBox);
        view.layout.spacing_(0).margins_(0);

        control.addAction(\qtGui,{ |c| 
           { 
                slider.value_(c.normValue);
                numBox.value_(c.value);
            }.defer 
        });
    }

    round_ { |val|
        var decimals = val.asString.split($.)[1].size;
        numBox.decimals = decimals;
        round = val
    }

    stringColor_ { |val| text !? { text.stringColor_(val) } ?? { "no label".warn } }

    showLabel_ { |bool| text !? { text.visible_(bool) } ?? { "no label".warn } }
}
