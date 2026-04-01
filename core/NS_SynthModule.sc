NS_SynthModule : NS_ControlModule {
    var <modGroup, <modBus;
    var nsServer, numChans; 
    var <>synths; // this needs a setter, sometimes it gets overwritten in modules
    var <>paused = false;
    var <gateBool = false;
    var <modView;

    *new { |group, bus|

        ^super.new.initSynthModule(group, bus)
    }

    initSynthModule { |group, bus|
        modGroup = group;
        modBus = bus;

        nsServer = NSFW.servers[modGroup.server.name];
        numChans = nsServer.options.numChans;
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
        modView.onClose_({
            // confirm this removes widgets without removing controller mapping
            // controls.do { |c| c.removeAction(\qtGui) }; 

            modView = nil
        })
    }

    /*
    * - this could be a ChannelStrip instance method, checking just the instance
    * where gateBool changed; modules would then have to know in where they live
    * - also in/outStrips don't need to be gated...
    * - this method will get "slower" as more strips are filled with modules, but
    * hard to say if it has a noticeable impact; if so -> move it to the strip
    * - another alternative is to pass strip to SynthModule for only this method,
    * then do if(strip.isKindOf(NS_ChannelStrip))
    */
    gateBool_ { |bool|
        gateBool = bool.asBoolean;

        nsServer.strips.deepDo(2, { |strip| 
            var modules = strip.slots.reject{ |m| m == nil };
            if(modules.size > 0) { 
                var stripBool = modules.collect { |m| m.gateBool }.reduce('or');
                strip.inSynth.set(\thru, stripBool.binaryValue)
            };
        })
    }

    free {
        controls.do(_.free);
        if(this.paused) { synths.do(_.free) } { synths.do(_.set(\gate, 0) ) };
        this.gateBool_(false);
        if(modView.notNil) { { modView.close }.defer };
        this.freeExtra;
    }

    freeExtra { /* to be overloaded by modules */}

    pause {
        synths.do { |synth| 
            if(synth.notNil) { synth.set(\pauseGate, 0) }
        };
        modGroup.run(false);
        this.paused = true;
    }

    unpause {
        synths.do { |synth| 
            if(synth.notNil) { synth.set(\pauseGate, 1); synth.run(true) }
        };
        modGroup.run(true);
        this.paused = false;
    }

    toggleView {
        if(modView.isNil) 
        { this.makeModuleView }
        { modView.close; modView = nil }
    }
}
