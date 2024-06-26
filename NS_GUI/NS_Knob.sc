NS_Knob {
  var <value, <action;
  var text, knob, numBox;
  var <view, <spec, <>round = 0.01;

  *new { |label, controlSpec, action, centered = false, initVal|
    ^super.newCopyArgs(initVal, action).init(label, controlSpec, centered)
  }

  init { |label, controlSpec, centered|
    view = View();
    spec = controlSpec.asSpec;

    if(label.notNil,{
      text = StaticText(view)
      .string_(label)
      .align_(\center);
    },{
      text = nil
    });

    knob = Knob(view)
    .mode_(\vert)
    .centered_(centered)
    .action_({ this.valueAction = spec.map( knob.value) });

    numBox = NumberBox(view)
    .maxHeight_(30)
    .action_({ this.valueAction = numBox.value })
    .step_( spec.guessNumberStep )
    .scroll_step_( spec.guessNumberStep )
    .align_(\center);

    view.layout_(
      VLayout(
        text,
        knob,
        numBox
      )
    );

    view.layout.spacing_(0).margins_(0);
    this.value_(value ? spec.default);
  }

  layout { ^view.layout }

  asView { ^view }

  value_ { |val|
    value = spec.constrain(val);
    numBox.value = value.round(round);
    knob.value = spec.unmap(value);
  }

  valueAction_ { |val|
    this.value_(val);
    action.value(this)
  }

  maxHeight_ { |val| view.maxHeight_(val) }

  maxWidth_ { |val| view.maxWidth_(val) }

  stringColor_ { |val| text.stringColor_(val) }

}
