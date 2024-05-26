NS_AssignButton {
  var <view;
  var <value, <>action;
  var <button;

  *new { |parent|
    ^super.new.init(parent)
  }

  init { |parent|
    button = Button()
    .states_([ [ "A", Color.fromHexString("#b827e8"), Color.black ] ,[ "M", Color.black, Color.fromHexString("#b827e8") ] ])
    .action_({ |but|
      action.value( but );
    });

    view = View(parent).layout_(
      HLayout( button )
    );

    view.layout.spacing_(0).margins_(0)
  }

  maxHeight_ { |val| view.maxHeight_(val) }

  maxWidth_ { |val| view.maxWidth_(val) }

  layout { ^view.layout }

  asView { ^view }

}
