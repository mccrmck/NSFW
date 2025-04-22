NS_AssignButton : NS_Widget {
    var <>module, <>ctrlIndex, <>type;
    var <value = 0;
    var manualPath = nil, manualAddr = nil;
    var stack;

    *new { |module, ctrlIndex, type|
        ^super.new.module_(module).ctrlIndex_(ctrlIndex).type_(type).init
    }

    init {
        var clickFunc = { |view, x, y, mod, butNum, count|

            case
            { butNum == 1 and: {value == 0}}{ this.showManualEntryMenu }
            { butNum == 0 }{
                if(mod.isShift, {value = 2}, { value = (value + 1).wrap(0,1) });
                stack.index_(value);
                actions[value].value
            }
        };
        var dragFunc = { |view, x, y| [module, ctrlIndex] };

        var states = [
            UserView().drawFunc_({ |v|
                var w = v.bounds.width;
                var h = v.bounds.height;
                var rect = Rect(0,0,w,h);

                Pen.fillColor_( NS_Style.bGroundDark );
                Pen.addRoundedRect(rect, NS_Style.radius, NS_Style.radius );
                Pen.fill;
                Pen.stringCenteredIn( // doesn't look centered, I think it has to do with margins/spacing
                    "A", rect, Font(*NS_Style.defaultFont), NS_Style.assButt
                )
            }),
            UserView().drawFunc_({ |v|
                var w = v.bounds.width;
                var h = v.bounds.height;
                var rect = Rect(0,0,w,h);

                Pen.fillColor_( NS_Style.assButt );
                Pen.addRoundedRect(rect, NS_Style.radius, NS_Style.radius );
                Pen.fill;
                Pen.stringCenteredIn( // not actually centered, I think it has to do with margins/spacing
                    "M", rect, Font(*NS_Style.defaultFont), NS_Style.textDark
                )
            }),
            View().layout_( 
                HLayout(
                    Button(),
                    Button().action_({ value = -1; clickFunc.(mod: 0, butNum: 0, count: 1) })
                ).spacing_(0).margins_(0)
            )
        ];

        var actions = [
            { 
                if(module.oscFuncs[ctrlIndex].isNil,{ NS_Transceiver.clearQueues });
                NS_Transceiver.clearAssignedController(module, ctrlIndex);
                NS_Transceiver.listenForControllers(false);
                manualPath = nil
            },
            { 
                NS_Transceiver.addToQueue(module, ctrlIndex, type);
                NS_Transceiver.listenForControllers(true)
            },
            { 2.postln }
        ];

        states = states.collect({ |st|
            st.mouseDownAction_( clickFunc ).beginDragAction_( dragFunc )
        });
        stack = StackLayout( *states ).index_(0);

        view = UserView().minWidth_(NS_Style.buttonW).minHeight_(NS_Style.buttonH);
        view.layout_( stack );
        //view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
        view.layout.spacing_(0).margins_(0)
    }

    value_ { |val|
        value = val;
        stack.index_(value);
    }

    showManualEntryMenu {
        var oscFunc = module.oscFuncs[ctrlIndex];

        var oscPath = oscFunc !? { oscFunc.path } ?? { manualPath };
        var oscPathEntry = TextField() 
        .string_(oscPath ? "/your/OSC/addr")
        .align_(\center)
        .stringColor_(oscPath !? { NS_Style.textDark } ?? { NS_Style.darklight })
        .background_(NS_Style.bGroundLight)
        .mouseDownAction_({ |v|
            v.stringColor_(NS_Style.textDark).string_("")
        })
        .action_({ |tf| manualPath = tf.value} ); // how can I check if this is a valid entry?

        var oscAddr = oscFunc !? { oscFunc.srcID } ?? { manualAddr };
        var oscAddrEntry = TextField()
        .string_(oscAddr ? "ip_port")
        .align_(\center)
        .stringColor_(oscAddr !? { NS_Style.textDark } ?? { NS_Style.darklight })
        .background_(NS_Style.bGroundLight)
        .mouseDownAction_({ |v|
            v.stringColor_(NS_Style.textDark).string_("")
        })
        .action_({ |tf| manualAddr = tf.value }); // how can I check if this is a valid entry?

        var mapBut = Button()
        .maxWidth_(15).maxHeight_(25)
        .states_([["M", NS_Style.assButt, NS_Style.bGroundDark]])
        .action_({
            if(manualPath.notNil /*and: {manualAddr.notNil}*/,{

                {
                    this.value_(1);
                    //  NS_Transceiver.assignOSCControllerDiscrete(manualPath, manualAddr);
                    oscPathEntry.stringColor_(NS_Style.assButt).string_("MAPPED!");
                    0.5.wait;
                    oscPathEntry.stringColor_(NS_Style.textDark).string_(manualPath);
                }.fork(AppClock)
            })
        });


        var clearBut = Button().maxWidth_(15).maxHeight_(25)
        .states_([["X", NS_Style.bGroundDark, NS_Style.muteRed]])
        .action_({ |but|
            NS_Transceiver.clearAssignedController(module, ctrlIndex);
            manualPath = nil;
            manualAddr = nil;
            oscPathEntry.string_("/your/OSC/addr").stringColor_(NS_Style.darklight) 
        });

        var oscView = View().layout_( 
            HLayout(
                oscPathEntry,
                oscAddrEntry
            ).spacing_(0).margins_(0);
        );
        var midiView = View().layout_(
            HLayout(
                PopUpMenu().items_(MIDIClient.sources), // available MIDI devices
                TextField(), // msgNum
                TextField(), // chan
                PopUpMenu() // possible MIDI messages -> filtered by type (discrete/continuous)?
                .items_(["noteOn", "noteOff", "control", "touch", "polytouch", "bend", "program"])
            ).spacing_(0).margins_(0);
        );

        var stack = StackLayout(oscView, midiView).spacing_(0).margins_(0);

        var menu = Menu(
            CustomViewAction(
                View().background_(NS_Style.bGroundDark).layout_(
                    HLayout(
                        PopUpMenu().items_(["OSC","MIDI"]).action_({ |pu| stack.index_(pu.value) }),
                        stack,
                        mapBut,
                        clearBut,
                    ).spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
                )
            )
        ).front;
    }

    free { /* what goes here?*/ }
}
