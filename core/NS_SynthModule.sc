NS_SynthModule : NS_ControlModule {
    var <modGroup, <strip, <slotIndex;
    var nsServer, numChans; 
    var <>synths; // this needs a setter, sometimes it gets overwritten in modules
    var <>paused = false;
    var <gateBool = false;
    var <modView;

    *new { |strip, slotIndex|
        var group = strip.slotGroups[slotIndex];

        ^super.new.initSynthModule(group, strip, slotIndex)
    }

    initSynthModule { |modGroupIn, stripIn, slotIndexIn|
        modGroup = modGroupIn;
        strip = stripIn;
        slotIndex = slotIndexIn;

        nsServer = NSFW.servers[modGroupIn.server.name];
        numChans = strip.numChans;
        synths = List.newClear(0);

        this.buildSynthModule
    }

    makeWindow { |name, bounds|
        var vBounds;
        var cols = [Color.rand, Color.rand];
        var available = Window.availableBounds;
        bounds   = bounds.moveBy(
            (available.width - bounds.width).rand,
            (available.height - bounds.height).rand
        );

        modView = NS_Window(name, bounds).front;
        modView.alwaysOnTop_(true);
        modView.onClose_({ modView = nil })
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
            synths.do({ |synth| synth.set(\gate, 0) }); 
        });
        this.gateBool_(false);
        { modView.close }.defer;
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

    toggleView {
        if(modView.isNil) {
            this.makeModuleView
        } {
            modView.close;
            modView = nil
        }
    }
}
