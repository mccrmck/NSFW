NS_Fader {
    var <value, <action, orientation;
    var text, slider, numBox, widgets;
    var <view, <spec, <>round = 0.01;

    *new { |parent, label, controlSpec, action, orientation = 'vert', initVal|
        switch(orientation,
            \vert, { orientation = \vertical },
            \horz, { orientation = \horizontal },
            orientation
        );

        ^super.newCopyArgs(initVal, action, orientation).init(parent, label, controlSpec)
    }

    init { |parent, label, controlSpec|
        widgets = [];
        view = View(parent);
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

NS_XY {
    var <x, <y, <action;
    var label, slider, numBoxX, numBoxY;
    var <view, <specX, <specY, <>round = #[0.01,0.01];

    *new { |parent, labelX, controlSpecX, labelY, controlSpecY, action, initVal = #[0.5,0.5]|
        ^super.newCopyArgs(initVal[0],initVal[1], action).init(parent, labelX, controlSpecX, labelY, controlSpecY)
    }

    init { |parent, labelX, controlSpecX, labelY, controlSpecY|
        view = View(parent);
        specX = controlSpecX.asSpec;
        specY = controlSpecY.asSpec;

        labelX = StaticText(view)
        .string_("X:" + labelX)
        .align_(\left);

        labelY = StaticText(view)
        .string_("Y:" + labelY)
        .align_(\left);

        slider = Slider2D(view)
        .action_({ |slider|  this.valueAction = [specX.map(slider.x), specY.map(slider.y)]  });

        numBoxX = NumberBox(view)
        .maxWidth_(60)
        .action_({ this.valueAction = [numBoxX.value, numBoxY.value] })
        .step_( specX.guessNumberStep )
        .scroll_step_( specX.guessNumberStep )
        .align_(\center);

        numBoxY = NumberBox(view)
        .maxWidth_(60)
        .action_({ this.valueAction = [numBoxX.value, numBoxY.value] })
        .step_( specY.guessNumberStep )
        .scroll_step_( specY.guessNumberStep )
        .align_(\center);

        view.layout_(
            VLayout(
                HLayout( labelX, numBoxX ),
                HLayout( labelY, numBoxY),
                slider,
            )
        );

        view.layout.spacing_(0).margins_(0);

        this.value_( this.value ? [specX.default, specY.default]);
    }

    layout { ^view.layout }

    asView { ^view }

    value { ^[x, y] }

	value_ { |vals|
        x = specX.constrain(vals[0]);
        y = specY.constrain(vals[1]);
        numBoxX.value = x.round(round[0]);
        numBoxY.value = y.round(round[1]);
        slider.setXY(specX.unmap(x), specY.unmap(y))
    }

    valueAction_ { |vals|
        this.value_(vals);
        action.value(this)
    }

}
