NS_ControlModule {
    var <>controls, <>oscFuncs, <>assignButtons;

    initControlArrays { |numSlots|
        controls      = List.newClear(0);
        oscFuncs      = List.newClear(numSlots);
        assignButtons = List.newClear(numSlots);
    }

    free { oscFuncs.do({ |func| func.free }) }
}

NS_SynthModule : NS_ControlModule {
    var <>modGroup, <>bus;
    var <>synths;
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

    save { 
        var saveArray = Array.newClear(0);

        saveArray.put(0,controls);
        saveArray.put(1,oscFuncs);

        ^saveArray
    }

    load { |loadArray|

        // controls
        controls.do({ |ctrl, index|
            
           ctrl.valueAction_( loadArray[45][index] );
           if(assignButtons[index].notNil,{ assignButtons[index].value_(1) })
        });
        
        // oscFuncs
        oscFuncs.do({ |func, index|
            func = loadArray[91][index]

        });
        
    }
}
