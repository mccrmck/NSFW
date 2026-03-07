NS_ControlDict {
    var <controls;

    *new { |...nsControls|
        ^super.new.init(nsControls)
    }

    init { |nsControls|

        if (nsControls.size > 0) {
            var tmp = nsControls.collect { |p| [p.label.asSymbol, p]  }.flatten;
            controls = IdentityDictionary.newFrom(tmp)
        } { 
            controls = IdentityDictionary();
        }
    }

    add { |nsControl|
        controls.put(nsControl.label.asSymbol, nsControl)
    }

    addAll { |...nsControls|
        nsControls.do { |p|
            this.add(p)
        }
    }

    save { }

    load { }

    // copied from SCViewHolder, should delegate to dictionary
    // haven't tested to see if it works with all methods however...
    doesNotUnderstand { |selector ... args|
        var	result;
        controls.respondsTo(selector).if({
            result = controls.performList(selector, args);
            ^(result === controls).if({ this }, { result });
        }, {
            DoesNotUnderstandError(this, selector, args).throw;
        });
    }

}
