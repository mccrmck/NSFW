A {
    var <>params;

    *new {
        ^super.new.init()
    }

    init {
        params = Dictionary()
    }
}


B : A {
    var <>synths;

    *new {
        ^super.new.init()
    }

    init {
        params = Dictionary();
        synths = Dictionary()
    }
}
