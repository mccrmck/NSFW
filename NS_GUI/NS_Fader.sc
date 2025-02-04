NS_Fader {
    var <value, <action, orientation;
    var <text, <slider, <numBox, widgets;
    var <view, <spec, <round = 0.01;

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

    round_ { |val|
        var decimals = val.asString.split($.)[1].size;
        numBox.decimals = decimals;

        round = val
    }

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
