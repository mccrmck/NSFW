NS_ControlModule {
    var <>controls, <>controlTypes, <>oscFuncs, <>assignButtons;

    initControlArrays { |numSlots|
        controls      = List.newClear(0);
        controlTypes  = List.newClear(numSlots);
        oscFuncs      = List.newClear(numSlots);
        assignButtons = List.newClear(numSlots);
    }

    free { oscFuncs.do({ |func| func.free }) }

    save { 
        var saveArray = Array.newClear(4);
        var ctrlVals  = controls.collect({ |c| c.value });
        var oscArrays = oscFuncs.collect({ |func, index|

            if(func.notNil,{
                [func.path,func.srcID]
            })
        });
        //var sinkArrays = if(this.respondsTo(\moduleSinks),{
        //    this.moduleSinks.collect({ |sink, index|
        //        if(sink.module.notNil,{
        //            sink.save
        //        })
        //    })
        //});

        saveArray.put(0,ctrlVals);
        saveArray.put(1,controlTypes);
        saveArray.put(2,oscArrays);
        // saveArray.put(3,sinkArrays);
        saveArray.put(3,this.saveExtra);

        ^saveArray
    }

    saveExtra {}

    load { |loadArray|

        // controls
        controls.do({ |ctrl, index|
            ctrl.valueAction_( loadArray[0][index] );
        });

        // oscFuncs
        loadArray[1].do({ |controlType, index|
            var funcArray = loadArray[2][index];

            if(controlType.notNil,{

                case
                {controlType == 'OSCcontinuous'}{
                    NS_Transceiver.assignOSCControllerContinuous(this,index,funcArray[0],funcArray[1]);
                    assignButtons[index].value_(1)
                }
                {controlType == 'OSCdiscrete'}{
                    NS_Transceiver.assignOSCControllerDiscrete(this,index,funcArray[0],funcArray[1]);
                    assignButtons[index].value_(1)
                }
            })
        });

        //if(this.respondsTo(\moduleSinks),{
        //    loadArray[3].do({ |sinkArray, index|
        //        if(sinkArray.notNil,{
        //            this.moduleSinks[index].load(sinkArray, this.slotGroups[index], this.stripBus, this)
        //        })
        //    })
        //});

        this.loadExtra(loadArray[3])
    }

    loadExtra { |loadArray| }
}

NS_SynthModule : NS_ControlModule {
    var <>modGroup, <>bus;
    var <>synths;
    var <strip;
    var paused = false;
    var <>win, <layout;

    *new { |group, bus ...args|
        ^super.new.modGroup_(group).bus_(bus).init(*args)
    }

    initModuleArrays { |numSlots|
        synths = List.newClear(0);
        this.initControlArrays(numSlots)
    }

    makeWindow { |name, bounds|
        var start, stop;
        var cols = [Color.rand, Color.rand];
        var available = Window.availableBounds;
        bounds = bounds.moveBy((available.width - bounds.width).rand, (available.height - bounds.height).rand);
        win   = Window(name,bounds,false);
        start = [win.view.bounds.leftTop,win.view.bounds.rightTop].choose;
        stop  = [win.view.bounds.leftBottom,win.view.bounds.rightBottom].choose;

        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(start, stop, cols[0], cols[1] );
        };

        win.alwaysOnTop_(true);
        win.userCanClose_(false);
        win.front
    }

    free {
        oscFuncs.do({ |func| func.free });
        win.close;
        synths.do({ |synth| synth.set(\gate,0) }); 
        this.freeExtra
    }

    freeExtra { /* to be overloaded by modules */}

    linkStrip { |stripIn| strip = stripIn }

    pause {
        synths.do({ |synth| synth.set(\pauseGate, 0) });
        modGroup.run(false);
        paused = true;
    }

    unpause {
        synths.do({ |synth| synth.set(\pauseGate, 1); synth.run(true) });
        modGroup.run(true);
        paused = false;
    }

    show {
        win.visible = true;
        win.front;
    }

    hide {
        win.visible = false;
    }

    toggleVisible {
        var bool = win.visible.not;
        win.visible = bool;
        if( bool,{ win.front })
    }

}
