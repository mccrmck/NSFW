NS_ControlDict {
    var <controls;

    *new { |...nsControls|
        ^super.new.init(nsControls)
    }

    init { |nsControls|

        if(nsControls.size > 0) {
            var tmp = nsControls.collect { |p| [p.label.asSymbol, p] }.flatten;
            controls = IdentityDictionary.newFrom(tmp)
        } { 
            controls = IdentityDictionary();
        }
    }

    add { |nsControl|
        controls.put(nsControl.label.asSymbol, nsControl)
    }

    addAll { |...nsControls|
        nsControls.do { |p| this.add(p) }
    }

    // don't forget to add tests!
    save { }
    load { }

    free {
        controls.do(_.free)
    }

    // copied from SCViewHolder, should delegate to dictionary
    // haven't tested to see if it works with all methods however...
    doesNotUnderstand { |selector ... args|
        var	result;
        if(controls.respondsTo(selector)) {
            result = controls.performList(selector, args);
            ^if(result === controls) { this } { result }
        } {
            DoesNotUnderstandError(this, selector, args).throw;
        };
    }
}
