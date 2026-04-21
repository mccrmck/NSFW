NS_ControlModule {
    var <controlDict;
    var <loaded = false;

    *new { ^super.new.init }

    init { controlDict = NS_ControlDict() }

    free { controlDict.free }

    save { ^[controlDict.save, this.saveExtra] }

    saveExtra { ^nil }
    
    load { |loadArray, cond, action|
        loaded = false;

        controlDict.load(loadArray[0]);

        this.loadExtra(loadArray[1], cond, { loaded = true; cond.signalOne });
        cond.wait { loaded };
        action.value;
    }

    loadExtra { |loadArray, cond, action|
        // this needs to be in every overloaded .loadExtra
        action.value
    }
}
