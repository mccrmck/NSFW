NS_Switch {
  var <value, <action, orientation;
  var <view;
  var <buttons;

  *new { |labels, action, orientation = 'vert'|
    switch(orientation,
      \vert, { orientation = \vertical },
      \horz, { orientation = \horizontal },
      orientation
    );

    ^super.newCopyArgs(0, action, orientation).init(labels)
  }

  init { |labels|

    view = View();
    buttons = labels.collect({ |label,index|
      Button(view) 
      .minWidth_(30)
      .states_([ [ label.asString, Color.black ] ,[ label.asString, Color.white, Color.black ] ])
      .action_({
        this.valueAction_( index );
      });
    });

    switch(orientation,
      \vertical,   { view.layout = VLayout( *buttons ) },
      \horizontal, { view.layout = HLayout( *buttons ) },
     // \grid,
    );

    view.layout.spacing_(0).margins_(0);

    this.value_(0)
  }

  layout { ^view.layout }

  asView { ^view }

  value_ { |val|
      val = val.asInteger.wrap(0, buttons.size - 1);
      buttons.do({ |but| but.value_(0) });
      buttons[val].value_(1);
      value = val;
  }

  valueAction_ { |val|
    this.value_(val);
    action.value(this)
  }

  maxHeight_ { |val| view.maxHeight_(val) }
  minHeight_ { |val| view.minHeight_(val) }
  maxWidth_  { |val| view.maxWidth_(val) }
  minWidth_  { |val| view.minWidth_(val) }

  buttonsMaxHeight_ { |val| buttons.do({ |but| but.maxHeight_(val) }) }
  buttonsMinHeight_ { |val| buttons.do({ |but| but.minHeight_(val) }) }
  buttonsMaxWidth_  { |val| buttons.do({ |but| but.maxWidth_(val) }) }
  buttonsMinWidth_  { |val| buttons.do({ |but| but.minWidth_(val) }) }

}
