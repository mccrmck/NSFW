NS_AssignButton {
    var <type;
    var <view, <button;

    *new { |type = 'fader'|  // fader, multiFader, switch, button, xy, any others?
        ^super.newCopyArgs(type).init
    }

    init {
        button = Button()
        .states_([
            [ "A", Color.fromHexString("#b827e8"), Color.black ],
            [ "M", Color.black, Color.fromHexString("#b827e8") ]
        ]);

        view = View().layout_(
            HLayout().add(button)
        );

        view.layout.spacing_(0).margins_(0)
    }

    value { ^button.value }

    value_ { |val| button.value_(val) }

    maxHeight_ { |val| view.maxHeight_(val) }

    maxWidth_ { |val| view.maxWidth_(val) }

    layout { ^view.layout }

    asView { ^view }

    getAction { ^button.action }

    setAction { |module, ctrlIndex, type|
        button.action_({ |but|
            if(but.value == 0,{
                NS_Transceiver.clearAssignedController(module, ctrlIndex)
            },{
                NS_Transceiver.listenForControllers(module, ctrlIndex, type)
            })
        })
    }
}
