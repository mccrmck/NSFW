NS_ControlFader {
    var <view, text, slider, numBox;
    var <round = 0.01;

    *new { |ns_control, orientation = 'horz'|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        orientation = switch(orientation,
            \horz, { \horizontal },
            true,  { \horizontal },
            \vert, { \vertical },
            false, { \vertical },
            orientation
        );
        ^super.new.init(ns_control, orientation)
    }

    init { |control, orientation|
        view = View();

        text = if(control.label.notNil,{
            StaticText()
            .string_(control.label)
            .align_(\center);
        },{
            nil
        });

        slider = Slider()
        .background_(Color.clear)
        .knobColor_(Color.clear)
        .thumbSize_(10)
        .value_(control.normValue)
        .orientation_( orientation )
        .action_({ |sl|
            control.normValue_(sl.value);
        });

        numBox = NumberBox()
        .maxHeight_(90)
        .maxWidth_(75)
        .align_(\center)
        .value_(control.value)
        .action_({ |nb| 
            var val = control.spec.constrain(nb.value);
            control.value_(val);
        });

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

    stringColor_ { |val| text !? text.stringColor_(val) ?? { "no label".warn } }
    showLabel_ { |bool| text !? text.visible_(bool) ?? { "no label".warn } }
}
