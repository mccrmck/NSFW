NS_AbstractControl {
    var <label, <spec, <value;
    var defaultValue;
    var <actionDict;

    label_ { |newLabel|
        label = newLabel.asString
    }

    resetValue { this.value_(defaultValue) }

    normValue { ^spec.unmap(value) }

    normValue_ { |newVal ...excludeKeys| // actions that ~won't~ be evaluated
        this.value_(spec.map(newVal), *excludeKeys)
    }

    value_ { |newVal ...excludeKeys| // actions that ~won't~ be evaluated
        value = spec !? { spec.constrain(newVal) } ?? { newVal };

        if(excludeKeys.isEmpty) {
            actionDict.do(_.value(this))
        } {
            var newDict = actionDict.copy;
            excludeKeys.do{ |k| newDict.removeAt(k.asSymbol) };
            newDict.do(_.value(this))
        }
    }

    addAction { |key, actionFunc, update(true)| 
        actionDict.put(key.asSymbol, actionFunc);
        if(update) { actionFunc.value(this) }
    }

    removeAction { |key|
        actionDict.removeAt(key.asSymbol)
    }

    // should there be a .clear method as well? Free actions/responders, resetValue?

    free {
        actionDict.keysValuesChange({ nil })
    }
}

NS_ControlString : NS_AbstractControl {

    *new { |name, initVal|
        ^super.newCopyArgs(name.asString, nil, initVal.asString).init
    }

    init {
        defaultValue = value;
        actionDict = IdentityDictionary();
    }

    // these should not really be called on Strings, consider a warning?
    normValue { ^value } 
    normValue_ { |newVal ...excludeKeys| this.value_(newVal, *excludeKeys) }

    // maybe ControlString also gets a popup menu with a TextField?

    // can't save actionDict because function scope must be local...
    // so we count on loaading values for Controls with existing actions
    save { ^value }
    load { |loadVal| this.value_(loadVal) }
}

/**
* methods for auto mapping to MIDI/OSC controllers
*/

NS_ControlNumber : NS_AbstractControl {
    var <>mapped;  // 'unmapped', 'listening', 'mapped'
    var <responderDict;

    init {
        defaultValue  = value;
        mapped        = 'unmapped';
        actionDict    = IdentityDictionary();
        responderDict = IdentityDictionary();
    }

    //mapped_ { |status|
    //    mapped = status;
    //    actionDict.do(_.value(this))
    //}
    
    addResponder { |key, responder|
        responderDict.put(key.asSymbol, responder);
    }

    removeResponder { |key|
        // confirm that this does what you expect
        responderDict.removeAt(key.asSymbol).free
    }

    // move this here from NS_ControlWidget
    toggleAutoAssign { 
        if(mapped == 'unmapped') 
        { mapped = 'listening'; this.enableAutoAssign } 
        { mapped = 'unmapped';  this.disableAutoAssign };
    }

    disableAutoAssign {
        // this needs to be reconsidered:
        // what happens to the queue when autoAssign is disabled for a control?
        // f. eks. queueing 4 controls, disabling the second one, the last one, etc.
        if(actionDict['controller'].isNil) { NS_Transceiver.clearQueues };
        NS_Transceiver.clearAssignedController(this);
        NS_Transceiver.listenForControllers(false);
    }

    // maybe this too?
    openControlMenu {
        Menu(
            MenuAction("autoAssign",{ 
                this.toggleAutoAssign
            }).checked_(mapped != 'unmapped'),
            Menu(
                MenuAction("OSC"),
                MenuAction("MIDI"),
            ).title_("manual Assign")
        ).front
    }
    
    // can't save actionDict because function scope must be local
    // and adding responders autmatically adds closed functions
    save { 
        // returns IdentityDictionary with keys mapped to return array
        // is this necessary? Are the keys superfluous?
        var responders = responderDict.collect({ |oscFunc| 
            // consider using key to determine OSC/MIDI, or rather .respondsTo
            [oscFunc.path, oscFunc.srcID]
        });
        
        ^[value, responders]
    }


    load { |loadArray| 
        // loadArray[1] is an IdentityDictionary with keys from .assignOSCcontroller
        // use these keys to determine if MIDI/OSC/etc.
        loadArray[1].do { |load|
            this.assignOSCcontroller(*load)
        };

        this.value_(loadArray[0]) 
    }

    free {
        actionDict.keysValuesChange({ nil });
        responderDict.do(_.free).keysValuesChange({ nil });
    }
}

NS_ControlInt : NS_ControlNumber {

    *new { |name, minVal(0), maxVal(1), initVal|
        var initSpec = ControlSpec(minVal, maxVal, 'lin', 1);
        ^super.newCopyArgs(name, initSpec, initVal ?? { initSpec.default }).init
    }

    // controlSpec will output floats, ensure this sucker outputs integers!
    // .normValue will return a float, however
    value { ^super.value.asInteger }

    spec_ { |minVal, maxVal|
        var normVal = spec.unmap(value);
        spec  = ControlSpec(minVal, maxVal, 'lin', 1);
        value = spec.map(normVal)
    }

    enableAutoAssign {
        NS_Transceiver.addToQueue(this, 'discrete');
        NS_Transceiver.listenForControllers(true)
    }

    // this only allows one OSC source per Control - should I allow for more?
    // might be relevant if I eventually implement visualizers or something...
    assignOSCcontroller { |path, netAddr|
        mapped = 'mapped';

        this.addResponder(\oscController,
            OSCFunc({ |msg|
                this.value_(msg[1], \oscController); // seems to get gummy without this key
            }, path, netAddr)
        );

        this.addAction(\oscController, { |c| netAddr.sendMsg(path, c.value) });
    }

    assignMIDIcontroller {} // case statement for different midi messages?
}

NS_ControlFloat : NS_ControlNumber { 

    *new { |name, controlSpec, initVal|
        initVal = initVal ?? { initVal = controlSpec.asSpec.default };
        ^super.newCopyArgs(name.asString, controlSpec.asSpec, initVal).init
    }

    init {
        defaultValue  = value;
        mapped        = 'unmapped';
        actionDict    = IdentityDictionary();
        responderDict = IdentityDictionary();
    }
   
    spec_ { |newSpec|
        var normVal = spec.unmap(value);
        spec  = newSpec.asSpec;
        value = spec.map(normVal)
    }

    enableAutoAssign {
        NS_Transceiver.addToQueue(this, 'continuous');
        NS_Transceiver.listenForControllers(true)
    }

    assignOSCcontroller { |path, netAddr|
        mapped = 'mapped';

        this.addResponder(\oscController,
            OSCFunc({ |msg|
                this.normValue_(msg[1], \oscController); // seems to get gummy without this key
            }, path, netAddr)
        );

        this.addAction(\oscController, { |c| netAddr.sendMsg(path, c.value) });
    }

    assignMIDIcontroller {} // case statement for different midi messages?
}

// maybe the NS_Transceiver queue can be cleared in a smart way
// according to instance vars in each Control?
// maybe typed controls removes the need for two queues somehow?
