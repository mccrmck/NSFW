NS_AssignButton {
    var <module, <ctrlIndex, <type;
    var <value = 0;
    var states, actions;
    var <view, <button;

    *new { |module, ctrlIndex, type|
        ^super.newCopyArgs(module, ctrlIndex, type).init
    }

    init {
        var clickFunc = { |view, x, y, mod, butNum, count|
            states.do({ |v| v.visible_(false) });

            if(mod.isShift,{ value = 2 },{ value = (value + 1).wrap(0,1) });
            states[value].visible_(true);
            actions[value].value
        };
        var dragFunc = { |view, x, y| [module, ctrlIndex] };

        states = [
            UserView()
            .drawFunc_({ |v|
                var w = v.bounds.width;
                var h = v.bounds.height;

                Pen.fillColor_( NS_Style.bGroundDark );
                Pen.addRoundedRect(Rect(0,0,w,h), NS_Style.radius, NS_Style.radius );
                Pen.fill;
            })
            .visible_( true )
            .mouseDownAction_( clickFunc )
            .beginDragAction_( dragFunc )
            .layout_(
                VLayout(
                    StaticText().align_(\center).string_("A")
                    .stringColor_( NS_Style.assButt )
                ).spacing_(0).margins_(0)
            ), 
            UserView()
            .drawFunc_({ |v|
                var w = v.bounds.width;
                var h = v.bounds.height;
                
                Pen.fillColor_( NS_Style.assButt );
                Pen.addRoundedRect(Rect(0,0,w,h), NS_Style.radius, NS_Style.radius );
                Pen.fill;
            })
            .visible_( false )
            .mouseDownAction_( clickFunc )
            .beginDragAction_( dragFunc )
            .layout_(
                VLayout( 
                    StaticText().align_(\center).string_("M")
                    .stringColor_( NS_Style.textDark )
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
            { 
                if(module.oscFuncs[ctrlIndex].isNil,{ NS_Transceiver.clearQueues });
                NS_Transceiver.clearAssignedController(module, ctrlIndex);
                NS_Transceiver.listenForControllers(false);
            },
            { 
                NS_Transceiver.addToQueue(module, ctrlIndex, type);
                NS_Transceiver.listenForControllers(true)
            },
            { 2.postln }
        ];

        view = UserView().minWidth_(NS_Style.buttonW).minHeight_(NS_Style.buttonH);
        view.layout_( HLayout( *states ) );
        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
    }

    value_ { |val|
        value = val;
        states.do({ |v| v.visible_(false) });
        states[value].visible_(true);
    }

    maxHeight_ { |val| view.maxHeight_(val) }

    maxWidth_ { |val| view.maxWidth_(val) }

    layout { ^view.layout }

    asView { ^view }
}
