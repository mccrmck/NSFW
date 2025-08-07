NS_ControlModule {
    var <>controls;
    var <loaded = false;

    initControlArray { |numSlots|
        controls = List.newClear(numSlots);
    }

    free { 
        controls.do(_.free);
    }

    save { 
        var saveArray = List.newClear(0);
        var ctrlVals  = controls.collect({ |c| c.value }); // .collect turns List into Array
        var responders = controls.collect({ |c|          // this is wack
            var func = c.responderDict['controller'];
            func !? { [func.path, func.srcID] }
        });

        saveArray.add(ctrlVals);   // loadArray[0]
        saveArray.add(responders); // loadArray[1]
        this.saveExtra(saveArray); // loadArray[2]

        ^saveArray
    }

    saveExtra { |saveArray| }

    load { |loadArray, cond, action|
        loaded = false;

        // oscFuncs
        loadArray[1].do({ |pathAddr, index|
            pathAddr !? {
                var path = pathAddr[0];
                var addr = pathAddr[1];
                var ctrl = controls[index];

                ctrl.mapped = 'mapped';

                if(ctrl.spec.step == 1,{
                    NS_Transceiver.assignOSCControllerDiscrete(ctrl, path, addr)  
                },{       
                    NS_Transceiver.assignOSCControllerContinuous(ctrl, path, addr)
                });
                // cond.wait { ctrl.responderDict['controller'].notNil }
            }
        });

        // controls
        loadArray[0].do({ |ctrlVal, index|
            ctrlVal !? {
                controls[index].value_(ctrlVal);
            }
        });

        // anything extra
        this.loadExtra(loadArray[2], cond, { loaded = true; cond.signalOne });

        cond.wait { loaded };
        action.value
    }

    // this needs to be in every overloaded .loadExtra
    loadExtra { |loadArray, cond, action|
        action.value
    }
}

NS_SynthModule : NS_ControlModule {
    var <>modGroup, <>strip, <>slotIndex; // these setters only used for initting?
    var <>synths; // this needs a setter, sometimes it gets overwritten in modules
    var <>paused = false;
    var <gateBool = false;
    var win;

    *new { |strip, slotIndex|
        var group = strip.slotGroups[slotIndex];

        ^super.new.modGroup_(group).strip_(strip).slotIndex_(slotIndex).init
    }

    initModuleArrays { |numSlots|
        synths = List.newClear(0);
        this.initControlArray(numSlots)
    }

    makeWindow { |name, bounds|
        var vBounds;
        var cols = [Color.rand, Color.rand];
        var available = Window.availableBounds;
        bounds   = bounds.moveBy(
            (available.width - bounds.width).rand,
            (available.height - bounds.height).rand
        );
        win      = Window(name, bounds);

        win.drawFunc = {
            vBounds  = win.view.bounds;
            Pen.addRect(vBounds);
            Pen.fillAxialGradient(vBounds.leftTop, vBounds.leftBottom, cols[0], cols[1]);
        };

        win.alwaysOnTop_(true);
        win.userCanClose_(false);
    }

    gateBool_ { |bool|
        gateBool = bool.asBoolean;
        strip.gateCheck;
    }

    free {
        controls.do(_.free);
        if(this.paused,{
            synths.do(_.free)
        },{
            synths.do({ |synth| synth.set(\gate,0) }); 
        });
        this.gateBool_(false);
        { win.close }.defer;
        this.freeExtra;
    }

    freeExtra { /* to be overloaded by modules */}

    pause {
        synths.do({ |synth| 
            if(synth.notNil,{ 
                synth.set(\pauseGate, 0)
            })
        });
        modGroup.run(false);
        this.paused = true;
    }

    unpause {
        synths.do({ |synth| 
            if(synth.notNil,{ 
                synth.set(\pauseGate, 1);
                synth.run(true)
            })
        });
        modGroup.run(true);
        this.paused = false;
    }

    toggleVisible {
        var bool = win.visible.not;
        win.visible = bool;
        if(bool,{ win.front })
    }
}
