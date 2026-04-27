NS_AbstractControl {
    var <label, <spec, <value;
    var defaultValue;
    var <actionDict;

    label_ { |newLabel|
        label = newLabel.asString
    }

    resetValue { this.value_(defaultValue) }

    normValue { ^spec.unmap(value) }

    normValue_ { |newVal ...excludeKeys| // actions that ~won't~ be evaluated
        value = spec.map(newVal);
        this.update(*excludeKeys)
    }

    value_ { |newVal ...excludeKeys| // actions that ~won't~ be evaluated
        value = spec.constrain(newVal);
        this.update(*excludeKeys)
    }

    addAction { |key, actionFunc, update(true)| 
        actionDict.put(key.asSymbol, actionFunc);
        if(update) { actionFunc.value(this) }
    }

    removeAction { |key|
        actionDict.removeAt(key.asSymbol)
    }

    update { |...excludeKeys|
        if(excludeKeys.isEmpty) {
            actionDict.do(_.value(this))
        } {
            var newDict = actionDict.copy;
            excludeKeys.do{ |k| newDict.removeAt(k.asSymbol) };
            newDict.do(_.value(this))
        }
    }

    // should there be a .clear method as well? Free actions/responders, resetValue?

    free {
        actionDict.keysValuesChange({ nil })
    }
}

NS_ControlString : NS_AbstractControl {

    *new { |name, initVal|
        ^super.newCopyArgs(name.asString, nil, initVal.asString).init
    }

    init {
        defaultValue = value;
        actionDict = IdentityDictionary();
    }

    value_ { |newVal ...excludeKeys| // actions that ~won't~ be evaluated
        value = newVal;
        this.update(*excludeKeys)
    }

    // these should not really be called on Strings, consider a warning?
    normValue { ^value } 
    normValue_ { |newVal ...excludeKeys|
        value = newVal;
        this.update(*excludeKeys)
    }

    // maybe ControlString also gets a popup menu with a TextField?

    // can't save actionDict because function scope must be local...
    // so we count on loaading values for Controls with existing actions
    save { ^value }
    load { |loadVal| this.value_(loadVal) }
}

/**
* methods for auto mapping to MIDI/OSC controllers
*/

NS_ControlNumber : NS_AbstractControl {
    var <>mapped;  // 'unmapped', 'listening', 'mapped'
    var <responderDict;

    init {
        defaultValue  = value;
        mapped        = 'unmapped';
        actionDict    = IdentityDictionary();
        responderDict = IdentityDictionary();
    }

    //mapped_ { |status|
    //    mapped = status;
    //    actionDict.do(_.value(this))
    //}
    
    addResponder { |key, responder|
        responderDict.put(key.asSymbol, responder);
    }

    removeResponder { |key|
        // confirm that this does what you expect
        responderDict.removeAt(key.asSymbol).free
    }

    toggleAutoAssign { 
        if(mapped == 'unmapped') 
        { this.enableAutoAssign; mapped = 'listening' } 
        { this.disableAutoAssign; mapped = 'unmapped' };
    }

    // move this to subclasses
    openControlMenu {
        Menu(
            MenuAction("autoAssign", { this.toggleAutoAssign })
            .checked_(mapped != 'unmapped'),
            Menu(MenuAction("OSC"), MenuAction("MIDI")).title_("manual Assign")
        ).front
    }
    
    // can't save actionDict because function scope must be local
    // and adding responders autmatically adds closed functions
    save { 
        // changing from IdentityDictionary to array reduced file size by about 60%
        var responders = [];
        responderDict.do{ |func| 
            var responderInfo;

            func.class.switch(
                OSCFunc, { responderInfo = ['OSC', func.path, func.srcID] },
                MIDIFunc, { 
                    // responderInfo = ['MIDI', ...] 
                    "saving MIDIFUncs not implemented yet".warn
                }
            );
            responders = responders.add(responderInfo)
        };

        ^[value, responders]
    }

    load { |loadArray| 
        loadArray[1].do { |load|
            load[0].switch(
                'OSC', { this.assignOSCcontroller(*load[1..]) },
                //'MIDI', { this.assignMIDIcontroller(*load[1..]) },
            )
        };

        this.value_(loadArray[0]) 
    }

    free {
        // I think I can also call .clear here...
        actionDict.keysValuesChange({ nil });
        responderDict.do(_.free).keysValuesChange({ nil });
    }
}

NS_ControlInt : NS_ControlNumber {

    *new { |name, minVal(0), maxVal(1), initVal|
        var initSpec = ControlSpec(minVal, maxVal, 'lin', 1);
        ^super.newCopyArgs(name, initSpec, initVal ?? { initSpec.default }).init
    }

    // controlSpec outputs floats, ensure this outputs integers!
    // .normValue will return a float, however
    value { ^super.value.asInteger }

    spec_ { |minVal, maxVal|
        var normVal = spec.unmap(value);
        spec  = ControlSpec(minVal, maxVal, 'lin', 1);
        value = spec.map(normVal)
    }

    enableAutoAssign {
        NS_Transceiver.addToQueue(this, 'discrete');
        NS_Transceiver.listenForControllers(true)
    }

    disableAutoAssign {
        if(mapped == 'mapped') 
        { this.unassignOSCcontroller } // fix for MIDI controls
        { NS_Transceiver.removeFromQueue(this, 'discrete') }
    }

    // allows one OSC source per Control - should I allow for more?
    // move this method and the next to NS_ControlNumber?
    assignOSCcontroller { |path, netAddr|
        mapped = 'mapped';

        this.addAction(\oscController, { |c| netAddr.sendMsg(path, c.value) });

        this.addResponder(\oscController,
            OSCFunc({ |msg| this.value_(msg[1]) }, path, netAddr)
        );

        this.update
    }

    unassignOSCcontroller {
        this.removeResponder(\oscController).removeAction(\oscController)
    }

    // case statement for different midi messages?
    assignMIDIcontroller { |key, src, chan, num, val|
        var responder = key.switch(
            'control', { 
                ['control', src, chan, num, val].postln
                //MIDIFunc.cc({ |val|
                //    nsControl.normValue_(val / 127)
                //}, num, chan, src)
            },
            'noteOn', { ['noteOn', src, chan, num, val].postln },
            'noteOff', { ['noteOff', src, chan, num, val].postln },
            'program', {
                //['program', src, chan, num].postln
                MIDIFunc.program({


                }, chan, src)
            }
        );

        mapped = 'mapped';

        this.addAction(\midiController,{  });

        // check controller class for two-way communication;
        // add relevant `.addActions(\midiController, { MIDIOut... })`

        //this.addResponder(\midiController, responder);

        this.update
    } 
}

NS_ControlFloat : NS_ControlNumber { 

    *new { |name, controlSpec, initVal|
        initVal = initVal ?? { initVal = controlSpec.asSpec.default };
        ^super.newCopyArgs(name.asString, controlSpec.asSpec, initVal).init
    }

    init {
        defaultValue  = value;
        mapped        = 'unmapped';
        actionDict    = IdentityDictionary();
        responderDict = IdentityDictionary();
    }

    spec_ { |newSpec|
        var normVal = spec.unmap(value);
        spec  = newSpec.asSpec;
        value = spec.map(normVal)
    }

    enableAutoAssign {
        NS_Transceiver.addToQueue(this, 'continuous');
        NS_Transceiver.listenForControllers(true)
    }

    disableAutoAssign {
        if(mapped == 'mapped') 
        { this.unassignOSCcontroller } // fix for MIDI controls
        { NS_Transceiver.removeFromQueue(this, 'continuous') }
    }

    assignOSCcontroller { |path, netAddr|
        mapped = 'mapped';

        this.addAction(\oscController, { |c| netAddr.sendMsg(path, c.normValue) });

        this.addResponder(\oscController,
            OSCFunc({ |msg| this.normValue_(msg[1]) }, path, netAddr)
        );

        this.update
    }

    unassignOSCcontroller {
        this.removeResponder(\oscController);
        this.removeAction(\oscController)
    }

    // case statement for different kinds of midi messages?
    assignMIDIcontroller {} 
}
