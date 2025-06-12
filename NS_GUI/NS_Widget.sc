NS_Widget : SCViewHolder {

    // highlight
    // onClick
    // onRightClick
    // onDoubleClick
}

NS_ControlWidget : NS_Widget {
    var <>controlAddr; // this is a space for a OSC/MIDI controller
    var <isListening = false;
    var <isHighlighted = false;


    toggleAutoAssign { |nsControl, controlType|
        if(isListening,{
            this.disableAutoAssign(nsControl)
        },{
            this.enableAutoAssign(nsControl, controlType)
        });

        // move this to the nsControl so it can be saved/recalled
        isListening = isListening.not;
        isHighlighted = isListening;
        this.refresh;

        "something toggled".postln;
    }
    
    enableAutoAssign { |ns_control, controlType|
        "enableAutoAssign".postln;
        NS_Transceiver.addToQueue(ns_control, controlType);
        NS_Transceiver.listenForControllers(true)
    }
    
    disableAutoAssign { |nsControl|
        "disableAutoAssign".postln;
        // something here to clear cues if control not mapped
        if(nsControl.actionDict['controller'].isNil,{ NS_Transceiver.clearQueues });
        NS_Transceiver.clearAssignedController(nsControl);
        NS_Transceiver.listenForControllers(false);
        //  manualPath = nil
    }

    openControlMenu { |ns_control, controlType|
        Menu(
            MenuAction("autoAssign",{ 
                this.toggleAutoAssign(ns_control, controlType)
            }).checked_(isListening),
            Menu(
                MenuAction("OSC"),
                MenuAction("MIDI"),
            ).title_("manual Assign")
        ).front
    }
}

// this was WIP for Assignbutton, could use some of it here also

//    showManualEntryMenu {
//        var oscFunc = module.oscFuncs[ctrlIndex];
//
//        var oscPath = oscFunc !? { oscFunc.path } ?? { manualPath };
//        var oscPathEntry = TextField() 
//        .string_(oscPath ? "/your/OSC/addr")
//        .align_(\center)
//        .stringColor_(oscPath !? { NS_Style.textDark } ?? { NS_Style.darklight })
//        .background_(NS_Style.bGroundLight)
//        .mouseDownAction_({ |v|
//            v.stringColor_(NS_Style.textDark).string_("")
//        })
//        .action_({ |tf| manualPath = tf.value} ); // how can I check if this is a valid entry?
//
//        var oscAddr = oscFunc !? { oscFunc.srcID } ?? { manualAddr };
//        var oscAddrEntry = TextField()
//        .string_(oscAddr ? "ip_port")
//        .align_(\center)
//        .stringColor_(oscAddr !? { NS_Style.textDark } ?? { NS_Style.darklight })
//        .background_(NS_Style.bGroundLight)
//        .mouseDownAction_({ |v|
//            v.stringColor_(NS_Style.textDark).string_("")
//        })
//        .action_({ |tf| manualAddr = tf.value }); // how can I check if this is a valid entry?
//
//        var mapBut = Button()
//        .maxWidth_(15).maxHeight_(25)
//        .states_([["M", NS_Style.assButt, NS_Style.bGroundDark]])
//        .action_({
//            if(manualPath.notNil /*and: {manualAddr.notNil}*/,{
//
//                {
//                    this.value_(1);
//                    //  NS_Transceiver.assignOSCControllerDiscrete(manualPath, manualAddr);
//                    oscPathEntry.stringColor_(NS_Style.assButt).string_("MAPPED!");
//                    0.5.wait;
//                    oscPathEntry.stringColor_(NS_Style.textDark).string_(manualPath);
//                }.fork(AppClock)
//            })
//        });
//
//
//        var clearBut = Button().maxWidth_(15).maxHeight_(25)
//        .states_([["X", NS_Style.bGroundDark, NS_Style.muteRed]])
//        .action_({ |but|
//            NS_Transceiver.clearAssignedController(module, ctrlIndex);
//            manualPath = nil;
//            manualAddr = nil;
//            oscPathEntry.string_("/your/OSC/addr").stringColor_(NS_Style.darklight) 
//        });
//
//        var oscView = View().layout_( 
//            HLayout(
//                oscPathEntry,
//                oscAddrEntry
//            ).spacing_(0).margins_(0);
//        );
//        var midiView = View().layout_(
//            HLayout(
//                PopUpMenu().items_(MIDIClient.sources), // available MIDI devices
//                TextField(), // msgNum
//                TextField(), // chan
//                PopUpMenu() // possible MIDI messages -> filtered by type (discrete/continuous)?
//                .items_(["noteOn", "noteOff", "control", "touch", "polytouch", "bend", "program"])
//            ).spacing_(0).margins_(0);
//        );
//
//        var stack = StackLayout(oscView, midiView).spacing_(0).margins_(0);
//
//        var menu = Menu(
//            CustomViewAction(
//                View().background_(NS_Style.bGroundDark).layout_(
//                    HLayout(
//                        PopUpMenu().items_(["OSC","MIDI"]).action_({ |pu| stack.index_(pu.value) }),
//                        stack,
//                        mapBut,
//                        clearBut,
//                    ).spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
//                )
//            )
//        ).front;
//    }
