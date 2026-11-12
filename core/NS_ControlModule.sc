NS_ControlModule {
    var <controlDict;
    var <loaded = false;

    *new { ^super.new.init }

    init { controlDict = NS_ControlDict() }

    save { ^controlDict.save }

    load { |loadArray| controlDict.load(loadArray) }
}
