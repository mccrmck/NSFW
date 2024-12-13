NS_AssignButton {
    var <view, <button;

    *new { |module, ctrlIndex, type|
        ^super.new.init(module, ctrlIndex, type)
    }

    init { |module, ctrlIndex, type|
        button = Button()
        .states_([
            [ "A", Color.fromHexString("#b827e8"), Color.black ],
            [ "M", Color.black, Color.fromHexString("#b827e8") ]
        ])
        .action_({ |but|
            if(but.value == 0,{
                NS_Transceiver.clearAssignedController(module, ctrlIndex)
            },{
                NS_Transceiver.listenForControllers(module, ctrlIndex, type)
            })
        });

        view = View().layout_( HLayout().add( button ) );
        view.layout.spacing_(0).margins_(0)
    }

    value { ^button.value }

    value_ { |val| button.value_(val) }

    maxHeight_ { |val| view.maxHeight_(val) }

    maxWidth_ { |val| view.maxWidth_(val) }

    layout { ^view.layout }

    asView { ^view }
}
