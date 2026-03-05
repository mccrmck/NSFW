NS_ParamDict {
    var <params;

    *new { |...nsParams|
        ^super.new.init(nsParams)
    }

    init { |nsParams|

        if (nsParams.size > 0) {
            var tmp = nsParams.collect { |p| [p.label.asSymbol, p]  }.flatten;
            params = Dictionary.newFrom(tmp)
        } { 
            params = Dictionary();
        }
    }

    add { |nsParam|
        params.put(nsParam.label.asSymbol, nsParam)
    }

    remove { |key|
        params.removeAt(key)
    }

    size {
        ^params.size
    }

    save { }

    load { }
}
