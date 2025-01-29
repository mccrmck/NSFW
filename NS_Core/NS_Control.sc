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

    normValue_ { |val ... keys| // needs a better name
       var specVal = spec.map(val);
        this.value_(specVal, *keys)
    }

    value_ { |val ...keys|
        value = val;
        if(keys.isEmpty,{
            actionDict.do(_.value(this))
        },{
            keys.do{ |k| actionDict[k.asSymbol].value(this) }
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
