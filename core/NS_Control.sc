NS_Control {
    var <label, <spec, <value;
    var defaultValue;
    var <>mapped;  // 'unmapped', 'listening', 'mapped'
    var <actionDict, <responderDict;

    *new { |name, controlSpec, initVal|
        if(initVal.isNil) { initVal = controlSpec.asSpec.default };
        ^super.newCopyArgs(name.asString, controlSpec.asSpec, initVal).init
    }

    init {
        defaultValue  = value;
        mapped        = 'unmapped';
        actionDict    = IdentityDictionary();
        responderDict = IdentityDictionary();
    }

    label_ { |newLabel|
        label = newLabel.asString
    }

    //mapped_ { |status|
    //    mapped = status;
    //    actionDict.do(_.value(this))
    //}

    resetValue {
        this.value_(defaultValue)
    }

    normValue {
        spec !? { ^spec.unmap(value) } ?? { ^value }
    }

    normValue_ { |val ...excludeKeys| // actions that ~won't~ be evaluated
        spec !? 
        { this.value_(spec.map(val), *excludeKeys) } ?? 
        { this.value_(val, *excludeKeys) };
    }

    value_ { |val ...excludeKeys| // actions that ~won't~ be evaluated
        spec !? { value = spec.constrain(val) } ?? { value = val };

        if(excludeKeys.isEmpty) {
            actionDict.do(_.value(this))
        } {
            var newDict = actionDict.copy;
            excludeKeys.do{ |k| newDict.removeAt(k.asSymbol) };
            newDict.do(_.value(this))
        }
    }

    spec_ { |newSpec|
        spec !?
        {
            var normVal = spec.unmap(value);
            spec  = newSpec.asSpec;
            value = spec.map(normVal)
        } ??
        { "spec was nil, this is probably not what you want".warn }
    }

    addAction { |key, actionFunc, update(true)| 
        actionDict.put(key.asSymbol, actionFunc);
        if(update, { actionFunc.value(this) })
    }

    removeAction { |key|
        actionDict.removeAt(key.asSymbol)
    }

    addResponder { |key, responder|
        responderDict.put(key.asSymbol, responder);
    }

    removeResponder { |key|
        responderDict.removeAt(key.asSymbol).free
    }

    // mapping functions go here

    enableAutoAssign {
        var controlType = if(spec.step > 0) { 'discrete' } { 'continuous' };
        NS_Transceiver.addToQueue(this, controlType);
        NS_Transceiver.listenForControllers(true)
    }

    disableAutoAssign {
        // this needs to be reconsidered:
        // what happens to the queue when autoAssign is disabled for a control?
        // consider enabling 4 controls, then disabling the second one, the last one, etc.
        if(actionDict['controller'].isNil) { NS_Transceiver.clearQueues };
        NS_Transceiver.clearAssignedController(this);
        NS_Transceiver.listenForControllers(false);
    }

    // reconsider this method:
    // this is called when freeing SynthModules, it should ensure the
    // control and all its goodies are removed from memory
    free {
        actionDict.keysValuesChange({ nil });
        responderDict.do(_.free).keysValuesChange({ nil });
    }
}


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
    toggleAutoAssign {}
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

    assignOSCcontroller {} // set value
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

    assignOSCcontroller {} // set normValue
    assignMIDIcontroller {} // case statement for different midi messages?
}

// maybe the NS_Transceiver queue can be cleared in a smart way
// according to instance vars in each Control?
// maybe typed controls removes the need for two queues somehow?
