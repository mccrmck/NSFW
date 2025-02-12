NS_ControlModule {
    var <>controls, /*<>controlTypes,*/ <>oscFuncs, <>assignButtons;

    initControlArrays { |numSlots|
        controls      = List.newClear(numSlots);
       // controlTypes  = List.newClear(numSlots);
        oscFuncs      = List.newClear(numSlots);
        assignButtons = List.newClear(numSlots);
    }

    free { oscFuncs.do({ |func| func.free }) } // should I clear all the Lists?

    save { 
        var saveArray = List.newClear(0);
        var ctrlVals  = controls.collect({ |c| c.value }); // .collect turns List into Array
        var oscArrays = oscFuncs.collect({ |func, index|

            if(func.notNil,{
                [func.path,func.srcID]
            })
        });
        
        saveArray.add(ctrlVals); // loadArray[0]
      //  saveArray.add(controlTypes);
        saveArray.add(oscArrays);  // loadArray[1]
        this.saveExtra(saveArray); // loadArray[2]

        ^saveArray
    }

    saveExtra { |saveArray| }

    load { |loadArray|
       // controlTypes = loadArray[1];
        
        // controls
        controls.do({ |ctrl, index|
            ctrl.value_( loadArray[0][index] )
            //ctrl.valueAction_( loadArray[0][index] );
        });

        // oscFuncs
        loadArray[1].do({ |pathAddr, index|
            if(pathAddr.notNil,{
                var path = pathAddr[0];
                var addr = pathAddr[1];

                controls[index].addAction(\controller,{ |c| addr.sendMsg(path, c.normValue) });
                oscFuncs[index] = OSCFunc({ |msg|

                    controls[index].normValue_( msg[1], \controller);

                },path, addr);
            })
        });

        // oscFuncs
       // controlTypes.do({ |controlType, index|
       //     var funcArray = oscFuncArray[index];

       //     if(controlType.notNil,{

       //         case
       //         {controlType == 'OSCcontinuous'}{
       //             NS_Transceiver.assignOSCControllerContinuous(this,index,funcArray[0],funcArray[1]);
       //             assignButtons[index].value_(1)
       //         }
       //         {controlType == 'OSCdiscrete'}{
       //             NS_Transceiver.assignOSCControllerDiscrete(this,index,funcArray[0],funcArray[1]);
       //             assignButtons[index].value_(1)
       //         }
       //         { "control % has controlType: %".format(index, controlType).postln }
       //     })
       // });

        this.loadExtra(loadArray[3])
    }

    loadExtra { |loadArray| }
}

NS_SynthModule : NS_ControlModule {
    var <>modGroup, <>bus;
    var <>synths;
    var <>strip;
    var <>paused = false;
    var <>win, <layout;

    *new { |group, bus, strip|
        ^super.new.modGroup_(group).bus_(bus).strip_(strip).init
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
            Pen.fillAxialGradient(start, stop, cols[0], cols[1]);
        };

        win.alwaysOnTop_(true);
        win.userCanClose_(false);
        win.front
    }

    free {
        //if(,{
        //    this.strip.inSynthGate_(0);
        //});
        if( this.paused,{
            synths.do(_.free);
        },{
            synths.do({ |synth| synth.set(\gate,0) }); 
        });
        win.close;
        oscFuncs.do({ |func| func.free });

        this.freeExtra;
    }

    freeExtra { /* to be overloaded by modules */}

    pause {
        synths.do({ |synth| if(synth.notNil,{ synth.set(\pauseGate, 0) }) });
        modGroup.run(false);
        this.paused = true;
    }

    unpause {
        synths.do({ |synth| if(synth.notNil,{ synth.set(\pauseGate, 1); synth.run(true) }) });
        modGroup.run(true);
        this.paused = false;
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
