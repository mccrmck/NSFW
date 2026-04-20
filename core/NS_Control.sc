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

    save {}
    load {}

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
}

/**
* functions for auto mapping to MIDI/OSC controllers
*/

NS_ControlMappable : NS_AbstractControl {
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
        // consider enabling 4 controls, then disabling the second one, the last one, etc.
        if(actionDict['controller'].isNil) { NS_Transceiver.clearQueues };
        NS_Transceiver.clearAssignedController(this);
        NS_Transceiver.listenForControllers(false);
    }

    // maybe this too?
    openControlMenu {}

    save {}
    load {}

    free {
        actionDict.keysValuesChange({ nil });
        responderDict.do(_.free).keysValuesChange({ nil });
    }
}


NS_ControlInt : NS_ControlMappable {

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

    assignOSCcontroller { |path, netAddr|
        mapped = 'mapped';

        this.addResponder(\oscController,
            OSCFunc({ |msg|
                this.value_(msg[1], \oscController); // seems to get gummy without this key
            }, path, netAddr)
        );

        this.addAction(\oscController,{ |c| netAddr.sendMsg(path, c.value) });
    }

    assignMIDIcontroller {} // case statement for different midi messages?
}

NS_ControlFloat : NS_ControlMappable { 

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

        this.addAction(\oscController,{ |c| netAddr.sendMsg(path, c.value) });
    }

    assignMIDIcontroller {} // case statement for different midi messages?
}

// maybe the NS_Transceiver queue can be cleared in a smart way
// according to instance vars in each Control?
// maybe typed controls removes the need for two queues somehow?
