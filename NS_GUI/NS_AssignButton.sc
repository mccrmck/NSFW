NS_AssignButton {
    var <value = 0;
    var states, actions;
    var <view, <button;

    *new { |module, ctrlIndex, type|
        ^super.new.init(module, ctrlIndex, type)
    }

    init { |module, ctrlIndex, type|
        var clickFunc = { |view, x, y, mod, butNum, count|
            states.do({ |v| v.visible_(false) });

            if(mod.isShift,{ value = 2 },{ value = (value + 1).wrap(0,1) });
            states[value].visible_(true);
            actions[value].value
        };
        var dragFunc = { |view, x, y| [module, ctrlIndex, type ] };

        states = [
            View()
            .background_( Color.black )
            .visible_( true )
            .mouseDownAction_( clickFunc )
            .beginDragAction_( dragFunc )
            .layout_(
                VLayout(
                    StaticText().align_(\center).string_("A")
                    .stringColor_( Color.fromHexString("#b827e8") )
                ).spacing_(0).margins_(0)
            ), 
            View()
            .background_( Color.fromHexString("#b827e8") )
            .visible_( false )
            .mouseDownAction_( clickFunc )
            .beginDragAction_( dragFunc )
            .layout_(
                VLayout( 
                    StaticText().align_(\center).string_("M")
                    .stringColor_( Color.black )
                ).spacing_(0).margins_(0)
            ),
            View()
            .visible_( false )
            .mouseDownAction_( clickFunc )         
            .beginDragAction_( dragFunc )
            .layout_( 
                HLayout(
                    Button(),
                    Button().action_({ value = -1; clickFunc.(mod: 0) })
                ).spacing_(0).margins_(0)
            )
        ];

        actions = [
            { NS_Transceiver.clearAssignedController(module, ctrlIndex) },
            { NS_Transceiver.listenForControllers(module, ctrlIndex, type) },
            { 2.postln }
        ];

        view = UserView().minWidth_(30).minHeight_(20);
        view.layout_( HLayout( *states ) );
        view.layout.spacing_(0).margins_(0)
    }

    value_ { |val|
        value = val;
        states.do({ |v| v.visible_(false) });
        states[value].visible_(true);
        actions[value].value
    }

    maxHeight_ { |val| view.maxHeight_(val) }

    maxWidth_ { |val| view.maxWidth_(val) }

    layout { ^view.layout }

    asView { ^view }
}
