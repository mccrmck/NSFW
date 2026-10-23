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

    reset { controls.do(_.resetValue) }

    save { ^controls.collect { |c| c.save } }

    load { |loadArray|
        loadArray.keysValuesDo { |key, load|
            var ctrl = controls.atFail(
                key, { "control: % not found".format(key).warn }
            );
            ctrl.load(load);
        };
    }

    loadAsync { |loadArray, cond, action|
        var numCtrls = controls.size;
        var numLoaded = 0;
        forkIfNeeded {
            loadArray.keysValuesDo { |key, load|
                var ctrl = controls.atFail(
                    key, { "control: % not found".format(key).warn }
                );
                ctrl.load(load);
                numLoaded = numLoaded + 1
            };
            cond.wait { numCtrls == numLoaded };

            action.value
        }
    } 

    // copied from SCViewHolder, delegates to controls Event
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
