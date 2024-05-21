NS_Fader {
    var <value, <action;
    var label, slider, numBox, <layout;
    var <spec, <>round = 0.01;

    *new { |label, controlSpec, action, orientation = 'vert', initVal|
        ^super.newCopyArgs(initVal, action).init(label, controlSpec, orientation)
    }

    init { |label, controlSpec, orientation|
        spec = controlSpec.asSpec;

        label = StaticText()
        .string_(label)
        .maxHeight_(15)
        .maxWidth_(45)
        .align_(\center);

        slider = Slider()
        .maxHeight_(210)
        .maxWidth_(45)
        .action_({ this.valueAction = spec.map(slider.value) });

        numBox = NumberBox()
        .maxHeight_(30)
        .maxWidth_(45)
        .action_({ this.valueAction = numBox.value })
        .step_( spec.guessNumberStep )
        .scroll_step_( spec.guessNumberStep )
        .align_(\center);

        switch(orientation,
            \vert,       { slider.orientation = \vertical;   layout = VLayout(label, slider, numBox) },
            \vertical,   { slider.orientation = \vertical;   layout = VLayout(label, slider, numBox) },
            \horz,       { 
                label.maxHeight_(45).maxWidth_(60);
                slider.orientation_(\horizontal).maxHeight_(45).maxWidth_(210);
                numBox.maxHeight_(45).maxWidth_(60);  
                layout = HLayout(label, slider, numBox)
            },
            \horizontal, { 
                label.maxHeight_(45).maxWidth_(60);
                slider.orientation_(\horizontal).maxHeight_(45).maxWidth_(150);
                numBox.maxHeight_(45).maxWidth_(60);
                layout = HLayout(label, slider, numBox)
            },
        );

        this.value_(value ? spec.default);
    }

    asView { ^layout }

    value_ { |val|
        value = spec.constrain(val);
        numBox.value = value.round(round);
        slider.value = spec.unmap(value);
    }

    valueAction_ { |val|
        this.value_(val);
        action.value(this)
    }

}

NS_XY {
    var <x, <y, <action;
    var label, slider, numBoxX, numBoxY, <layout;
    var <specX, <specY, <>round = #[0.01,0.01];

    *new { |labelX, controlSpecX, labelY, controlSpecY, action, initVal = #[0.5,0.5]|
        ^super.newCopyArgs(initVal[0],initVal[1], action).init(labelX, controlSpecX, labelY, controlSpecY)
    }

    init { |labelX, controlSpecX, labelY, controlSpecY|
        specX = controlSpecX.asSpec;
        specY = controlSpecY.asSpec;

        labelX = StaticText()
        .string_("X:" + labelX)
        .maxHeight_(15)
        .maxWidth_(90)
        .align_(\left);

        labelY = StaticText()
        .string_("Y:" + labelY)
        .maxHeight_(15)
        .maxWidth_(90)
        .align_(\left);

        slider = Slider2D()
        .maxHeight_(210)
        .maxWidth_(210)
        .action_({ |slider|  this.valueAction = [specX.map(slider.x), specY.map(slider.y)]  });

        numBoxX = NumberBox()
        .maxHeight_(30)
        .maxWidth_(45)
        .action_({ this.valueAction = [numBoxX.value, numBoxY.value] })
        .step_( specX.guessNumberStep )
        .scroll_step_( specX.guessNumberStep )
        .align_(\center);
        
        numBoxY = NumberBox()
        .maxHeight_(30)
        .maxWidth_(45)
        .action_({ this.valueAction = [numBoxX.value, numBoxY.value] })
        .step_( specY.guessNumberStep )
        .scroll_step_( specY.guessNumberStep )
        .align_(\center);

        layout = VLayout(
            HLayout( labelX, numBoxX ),
            HLayout( labelY, numBoxY),
            slider,
        );

        this.value_( this.value ? [specX.default, specY.default]);
    }

    asView { ^layout }

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
