NS_ControlModule {
    var <controlDict;
    var <loaded = false;

    *new { ^super.new.init }

    init { controlDict = NS_ControlDict() }

    save { ^controlDict.save }

    load { |loadArray, something, here|
        var cond = CondVar();
        var loadBool = false;

        controlDict.load(loadArray, cond, { loadBool = true; cond.signalOne } );
        cond.wait { loadBool };
    }
}
