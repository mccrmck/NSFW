NS_Fader {
    var <value, <action, orientation;
    var <text, <slider, <numBox, widgets;
    var <view, <spec, <>round = 0.01;

    *new { |label, controlSpec, action, orientation = 'vert', initVal|
        switch(orientation,
            \vert, { orientation = \vertical },
            \horz, { orientation = \horizontal },
            orientation
        );

        ^super.newCopyArgs(initVal, action, orientation).init(label, controlSpec)
    }

    init { |label, controlSpec|
        widgets = [];
        view = View();
        spec = controlSpec.asSpec;

        if(label.notNil,{
            text = StaticText(view)
            .string_(label)
            .align_(\center);
            widgets = widgets.add( text )
        },{
            text = nil
        });

        slider = Slider(view)
        .background_(Color.clear)
        .knobColor_(Color.clear)
        .thumbSize_(10)
        .action_({ this.valueAction = spec.map(slider.value) });
        widgets = widgets.add( slider );

        numBox = NumberBox(view)
        .maxHeight_(90)
        .action_({ this.valueAction = numBox.value })
        .step_( spec.guessNumberStep )
        .scroll_step_( spec.guessNumberStep )
        .align_(\center);
        widgets = widgets.add( numBox );

        switch(orientation,
            \vertical,   { slider.orientation = \vertical; view.layout = VLayout( *widgets ) },
            \horizontal, { slider.orientation = \horizontal; view.layout = HLayout( *widgets ) },
        );

        view.layout.spacing_(0).margins_(0);
        this.value_(value ? spec.default);
    }

    layout { ^view.layout }

    asView { ^view }

    value_ { |val|
        value = spec.constrain(val);
        numBox.value = value.round(round);
        slider.value = spec.unmap(value);
    }

    valueAction_ { |val|
        this.value_(val);
        action.value(this)
    }

    maxHeight_ { |val| view.maxHeight_(val) }

    maxWidth_ { |val| view.maxWidth_(val) }

    stringColor_ { |val| text.stringColor_(val) }
}

//NS_MultiFader {
//    var <value, <action, orientation;
//    var label, slider, numBox;
//    var <view, <spec;
//
//    *new { |numSliders, controlSpec, action, orientation = 'vert', origin = 0, initVal|
//        switch(orientation,
//            \vert, { orientation = \vertical },
//            \horz, { orientation = \horizontal },
//            orientation
//        );
//
//        ^super.newCopyArgs(initVal, action, orientation).init(numSliders, controlSpec, origin)
//    }
//
//    init { |numSliders, controlSpec,origin|
//        view = View();
//        spec = controlSpec.asSpecc
//
//        label =  StaticText(view)
//        .string_("currentVal:")
//        .align_(\left);
//
//        numBox = NumberBox(view)
//        .maxHeight_(90)
//        .action_({ this.valueAction = numBox.value })
//        .step_( spec.guessNumberStep )
//        .scroll_step_( spec.guessNumberStep )
//        .align_(\center);
//
//        slider = MultiSliderView()
//        .size_(numSliders)
//        .elasticMode_(1)
//        .reference_(origin)
//        .action_({ |ms| 
//            //numBox.value = spec.map( ms.currentvalue )
//        });
//
//        switch(orientation,
//            \vertical,   { slider.indexIsHorizontal_(false) },
//            \horizontal, { slider.indexIsHorizontal_(true) },
//        );
//
//        view = View.layout_(
//            HLayout(
//                VLayout(label, numBox),
//                slider
//            )
//        );
//
//        view.layout.spacing_(0).margins_(0);
//        this.value_(value ? spec.default);
//    }
//
//    layout { ^view.layout }
//
//    asView { ^view }
//}
