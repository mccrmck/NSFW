NS_AssignButton {
  var <value, <action;
  var button;

  *new { |parent|
    ^super.new.init(parent)
  }

  init { |parent|
    button = Button(parent)
    .states_([ [ "A", Color.fromHexString("#b827e8"), Color.black ] ,[ "M", Color.black, Color.fromHexString("#b827e8") ] ])
    .action_({ |but|
      action.value( but );
    })
    
  }

  maxHeight_ { |val| button.maxHeight_(val) }

  maxWidth_ { |val| button.maxWidth_(val) }

  layout { ^button }

  asView { ^button }

}
