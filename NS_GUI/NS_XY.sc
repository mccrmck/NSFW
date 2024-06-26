NS_XY {
    var <x, <y, <action;
    var label, slider, numBoxX, numBoxY;
    var <view, <specs, <round = #[0.01,0.01];

    *new { |labelX, controlSpecX, labelY, controlSpecY, action, initVal = #[0.5,0.5]|
    ^super.newCopyArgs(initVal[0],initVal[1], action).init(labelX, controlSpecX, labelY, controlSpecY)
}

init { |labelX, controlSpecX, labelY, controlSpecY|
    view = View();
    specs = [ controlSpecX.asSpec,controlSpecY.asSpec ];

    labelX = StaticText(view)
    .string_("X:" + labelX)
    .align_(\left);

    labelY = StaticText(view)
    .string_("Y:" + labelY)
    .align_(\left);

    slider = Slider2D(view)
    .background_(Color.clear)
    .knobColor_(Color.clear)
    .thumbSize_
    .action_({ |slider|  this.valueAction = [specs[0].map(slider.x), specs[1].map(slider.y)]  });

    numBoxX = NumberBox(view)
    .maxWidth_(60)
    .action_({ this.valueAction = [numBoxX.value, numBoxY.value] })
    .step_( specs[0].guessNumberStep )
    .scroll_step_( specs[0].guessNumberStep )
    .align_(\center);

    numBoxY = NumberBox(view)
    .maxWidth_(60)
    .action_({ this.valueAction = [numBoxX.value, numBoxY.value] })
    .step_( specs[1].guessNumberStep )
    .scroll_step_( specs[1].guessNumberStep )
    .align_(\center);

    view.layout_(
        VLayout(
            HLayout( labelX, numBoxX ),
            HLayout( labelY, numBoxY),
            slider,
        )
    );

    view.layout.spacing_(0).margins_(0);

    this.value_( this.value ? [specs[0].default, specs[1].default]);
}

layout { ^view.layout }

asView { ^view }

value { ^[x, y] }

round_ { |vals|
    var decimals = vals.collect({|i| i.asString.split($.)[1].size });
    numBoxX.decimals = decimals[0];
    numBoxY.decimals = decimals[1];

    round = vals
}

value_ { |vals|
    x = specs[0].constrain(vals[0]);
    y = specs[1].constrain(vals[1]);
    numBoxX.value = x.round(round[0]);
    numBoxY.value = y.round(round[1]);
    slider.setXY(specs[0].unmap(x), specs[1].unmap(y))
}

valueAction_ { |vals|
    this.value_(vals);
    action.value(this)
}
}
