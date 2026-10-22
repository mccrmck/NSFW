NS_ControlDict {
    var <controls;

    *new { |...nsControls|
        ^super.new.init(nsControls)
    }

    init { |nsControls|

        if(nsControls.size > 0) {
            var tmp = nsControls.collect { |c| [c.label.asSymbol, c] }.flatten;
            controls = Event.newFrom(tmp)
        } { 
            controls = Event();
        }
    }

    add { |nsControl|
        controls.put(nsControl.label.asSymbol, nsControl)
    }

    addAll { |...nsControls|
        nsControls.do { |c| this.add(c) }
    }

    clear { controls.do(_.clear) }

    save { 
        ^controls.collect { |c| c.save }
    }

    load { |loadArray|
        loadArray.keysValuesDo { |key, load|
            var ctrl = controls.atFail(
                key, { "control: % not found".format(key).warn }
            );
            ctrl.load(load)
        }
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
