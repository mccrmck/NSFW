NS_Control {
    var <label, <spec, <value;
    var <actionDict;
    var <responderDict;

    *new { |name, controlSpec, initVal|
        if(initVal.isNil,{ initVal = controlSpec.asSpec.default });
        ^super.newCopyArgs(name.asString, controlSpec.asSpec, initVal).init
    }

    init {
        actionDict    = IdentityDictionary();
        responderDict = IdentityDictionary();
    }

    label_ { |newLabel|
        label = newLabel.asString
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

        if(excludeKeys.isEmpty,{
            actionDict.do(_.value(this))
        },{
            var newDict = actionDict.copy;
            excludeKeys.do{ |k| newDict.removeAt( k.asSymbol ) };
            newDict.do(_.value(this))
        })
    }

    spec_ { |newSpec|
        spec !?
        {
            var normVal = spec.unmap( value );
            spec  = newSpec.asSpec;
            value = spec.map(normVal)
        } ??
        { "spec was nil, this is probably not what you want".warn }
    }

    addAction { |key, actionFunc, update = true| 
        actionDict.put(key.asSymbol, actionFunc);
        if(update,{ actionFunc.value(this) })
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

    free {
        actionDict.keysValuesChange({ nil });
        actionDict = nil;
        
        responderDict.do(_.free);
        responderDict = nil;
    }
}
