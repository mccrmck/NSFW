NS_ControlModule {
    var <>controls;//, <>oscFuncs, <>assignButtons;

    initControlArray { |numSlots|
        controls      = List.newClear(numSlots);
       // oscFuncs      = List.newClear(numSlots);
       // assignButtons = List.newClear(numSlots);
    }

    free { 
        controls.do(_.free);
       // oscFuncs.do(_.free);
        //assignButtons.do(_.free)
    }

    save { 
        var saveArray = List.newClear(0);
        var ctrlVals  = controls.collect({ |c| c.value }); // .collect turns List into Array
        //var oscArrays = oscFuncs.collect({ |func| func !? {[func.path, func.srcID]} });
        var responders = controls.collect({ |c|          // this is wack
            var func = c.responderDict['controllerOSC'];
            func !? {[func.path, func.srcID]}
        });

        saveArray.add(ctrlVals);   // loadArray[0]
        //saveArray.add(oscArrays);  // loadArray[1]
        saveArray.add(responders);
        this.saveExtra(saveArray); // loadArray[2]

        ^saveArray
    }

    saveExtra { |saveArray| }

    load { |loadArray|
        
        // controls
        loadArray[0].do({ |ctrlVal, index| controls[index].value_(ctrlVal) });

        // oscFuncs
        loadArray[1].do({ |pathAddr, index|
           // if(pathAddr.notNil,{
           //     var path = pathAddr[0];
           //     var addr = pathAddr[1];
           //    // var aBut = assignButtons[index];

           //     if(aBut.type == 'button' or: { aBut.type == 'switch'},{ // discrete
           //         controls[index].addAction(\controller,{ |c| addr.sendMsg(path, c.value) });
           //         oscFuncs[index] = OSCFunc({ |msg|

           //             controls[index].value_(msg[1], \controller);

           //         }, path, addr);
           //     },{ // continuous
           //         controls[index].addAction(\controller,{ |c| addr.sendMsg(path, c.normValue) });
           //         oscFuncs[index] = OSCFunc({ |msg|

           //             controls[index].normValue_(msg[1], \controller);

           //         }, path, addr);
           //     });
           //     aBut.value_(1)
           // })
        });

        this.loadExtra(loadArray[2])
    }

    loadExtra { |loadArray| }
}

NS_SynthModule : NS_ControlModule {
    // these args can be reduced to strip and slotIndex, group can be accessed through methods
    var <>modGroup, <>strip, <>slotIndex; // do these need setters?
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
        var start, stop, vBounds;
        var cols = [Color.rand, Color.rand];
        var available = Window.availableBounds;
        bounds = bounds.moveBy(
            (available.width - bounds.width).rand,
            (available.height - bounds.height).rand
        );
        win     = Window(name, bounds);
        vBounds = win.view.bounds;
        start   = [vBounds.leftTop, vBounds.rightTop].choose;
        stop    = [vBounds.leftBottom, vBounds.rightBottom].choose;

        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(start, stop, cols[0], cols[1]);
        };

        win.alwaysOnTop_(true);
        win.userCanClose_(false);
    }

    gateBool_ { |bool|
        gateBool = bool.asBoolean;
        strip.gateCheck;
    }

    free {
        if(this.paused,{
            synths.do(_.free)
        },{
            synths.do({ |synth| synth.set(\gate,0) }); 
        });
        win.close;
        controls.do(_.free);
       // oscFuncs.do(_.free);
       // assignButtons.do(_.free);
        this.gateBool_(false);

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
