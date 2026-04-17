NS_Control {
    var <label, <spec, <value;
    var defaultValue;
    var <>mapped;  // 'unmapped', 'listening', 'mapped'
    var <actionDict, <responderDict;

    *new { |name, controlSpec, initVal|
        if(initVal.isNil,{ initVal = controlSpec.asSpec.default });
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
            excludeKeys.do{ |k| newDict.removeAt( k.asSymbol ) };
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
    // - does it get used anywhere? 
    // - should it destroy the dicts?
    // I think this is used when freeing SynthModules, it should ensure the
    // control and all its goodies are removed from memory
    free {
        actionDict.keysValuesChange({ nil });
        actionDict = nil;

        responderDict.do(_.free);
        responderDict = nil;
    }
}
