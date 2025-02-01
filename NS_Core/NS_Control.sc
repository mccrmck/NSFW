NS_Control {
    var <label, <spec, <value;
    var <actionDict;
    
    *new { |name, controlSpec, initVal|
        if( initVal.isNil,{ initVal = controlSpec.default });
        ^super.newCopyArgs(name, controlSpec, initVal).init
    }

    init {
        actionDict = IdentityDictionary()
    }

    label_ { |newLabel|
        label = newLabel
    }

    normValue {
        ^spec.unmap( value )
    }

    normValue_ { |val ...excludeKeys| // functions in actionDict that *won't* be evaluated
        var specVal = spec.map(val);
        this.value_(specVal, *excludeKeys)
    }

    value_ { |val ...excludeKeys| // functions in actionDict that *won't* be evaluated
        value = val;
        if(excludeKeys.isEmpty,{
            actionDict.do(_.value(this))
        },{
            var newDict = actionDict.copy;
            excludeKeys.do{ |k| newDict.removeAt( k.asSymbol ) };
            newDict.do(_.value(this))
        })
    }

    spec_ { |newSpec|
        var normVal = spec.unmap( value );
        spec = newSpec.asSpec;
        value = spec.map( normVal )
    }

    addAction { |key, actionFunc, update = true| 
        actionDict.put(key.asSymbol,actionFunc);
        if(update,{ actionFunc.value(this) })
    }

    removeAction { |key|
        actionDict.removeAt(key.asSymbol)
    }
}
