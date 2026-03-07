NS_ParamDict {
    var <params;

    *new { |...nsParams|
        ^super.new.init(nsParams)
    }

    init { |nsParams|

        if (nsParams.size > 0) {
            var tmp = nsParams.collect { |p| [p.label.asSymbol, p]  }.flatten;
            params = IdentityDictionary.newFrom(tmp)
        } { 
            params = IdentityDictionary();
        }
    }

    add { |nsParam|
        params.put(nsParam.label.asSymbol, nsParam)
    }

    addAll { |...nsParams|
        nsParams.do { |p|
            this.add(p)
        }
    }

    removeAt { |key|
        params.removeAt(key)
    }

    size {
        ^params.size
    }

    save { }

    load { }

    // copied from SCViewHolder, should delegate to dictionary
    // haven't tested to see if it works  with all methods however...
    doesNotUnderstand { |selector ... args|
        var	result;
        params.respondsTo(selector).if({
            result = params.performList(selector, args);
            ^(result === params).if({ this }, { result });
        }, {
            DoesNotUnderstandError(this, selector, args).throw;
        });
    }

}
