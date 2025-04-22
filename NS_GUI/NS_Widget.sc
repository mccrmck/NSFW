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


    toggleAutoAssign {
        if(isListening,{
            this.disableAutoAssign
        },{
            this.enableAutoAssign
        });

        isListening = isListening.not;
        isHighlighted = isListening;
        this.refresh
    }
    
    enableAutoAssign { |ctrlType|
        "enableAutoAssign".postln;
      //  NS_Transceiver.addToQueue(module, ctrlIndex, ctrlType);
      //  NS_Transceiver.listenForControllers(true)
    }
    
    disableAutoAssign { 
        "disableAutoAssign".postln;
       // if(module.oscFuncs[ctrlIndex].isNil,{ NS_Transceiver.clearQueues });
      //  NS_Transceiver.clearAssignedController(module, ctrlIndex);
      //  NS_Transceiver.listenForControllers(false);
      //  manualPath = nil
    }

    openControlMenu {
        Menu(
            MenuAction("autoAssign",{ this.toggleAutoAssign }).checked_(isListening),
            Menu(
                MenuAction("OSC"),
                MenuAction("MIDI"),
            ).title_("manual Assign")
        ).front
    }
}

