NS_StripBase : NS_ControlModule {
    var <stripId, <numChans;
    var <stripGroup, <slotGroups, <faderGroup;
    var <slots;
    var <stripBus;
    var fader, sends;
    var <>paused = false;

    /**
    * Consider refactoring these classes...again:
    * NS_StripBase, NS_ChannelStrip, NS_InStrip, NS_OutStrip
    * Base class establishes a bunch of methods, strip classes inherit
    * each class constructor calls the methods they need, ie:
    ```
    *new { ^super.new.buildStrip }
    buildStrip {

        this.basicSetupStuff;
        this.makeGroups;
        this.makeFaderSynth;
        this.makeSlotCtrls(numSlots);
        // only ChannelStrip needs this, doesn't need to be in base class:
        this.makeReceiveCtrls;
        this.makeSendCtrls(nsServer);
        this.makeInputSynth(nsServer);
    }
    ```
    */

    *new { |id, inGroup, numModules(6)|
        ^super.new.buildStrip(id, inGroup, numModules)
    }

    buildStrip { |id, group, numModules|
        var nsServer = NSFW.servers[group.server.name];
        var cond = nsServer.cond;
        stripId  = id;
        numChans = nsServer.options.numChans;
        slots    = Array.newClear(numModules);
        sends    = IdentityDictionary();

        nsServer.addSynthDef(\ns_stripSend,{
            var sig = In.ar(\inBus.kr, numChans);
            sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1, 0.01));
            Out.ar(\outBus.kr, sig);
        }, { cond.signalOne });

        cond.wait { nsServer.synthLib.at(\ns_stripSend).notNil };

        // do I need to pass *this* nsServer or just NS_Server
        // or maybe make nsServer is an instance variable for all functions
        this.makeGroups(group, numModules);
        this.makeFaderSynth(nsServer, faderGroup);
        this.makeSlotCtrls(numModules);
        this.makeSendCtrls(nsServer);
        this.makeInputSynth(nsServer);
    }

    makeGroups { |group, numModules|
        var allSlots;
        stripGroup = Group(group,\addToTail);
        allSlots   = Group(stripGroup,\addToTail);
        slotGroups = numModules.collect({ |i| Group(allSlots, \addToTail) });
        faderGroup = Group(stripGroup,\addToTail);
    }

    makeFaderSynth { |nsServer, group|
        var numChans = nsServer.options.numChans;
        stripBus = Bus.audio(group.server, numChans);

        nsServer.addSynthDefCreateSynth(
            group, 
            \ns_stripFader,
            {
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0, 0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0, 0.01));

                ReplaceOut.ar(\bus.kr, sig)
            },
            [\bus, stripBus],
            { |synth| 
                fader = synth;

                controlDict.addAll(
                    NS_ControlFloat(\amp, \db)
                    .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) }),

                    NS_ControlInt(\mute, 0, 1, 0)
                    .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false)
                )
            }
        )
    }

    makeSlotCtrls { |numModules|
        numModules.do({ |modIndex|
            controlDict.add(
                NS_ControlString("module" ++ modIndex, "")
                .addAction(\module, { |c| 
                    if(c.value.size > 0) {
                        var className = ("NS_" ++ c.value).asSymbol.asClass;
                        this.addModule(className, modIndex);
                    } { 
                        this.freeModule(modIndex) 
                    }
                }, false)
            )
        })
    }

    makeSendCtrls { this.subclassResponsibility(thisMethod) }
    makeInputSynth {}

    // should I add addAction arg? Could then create pre-fader sends
    addSend { |target| 
        var key, bus;
        if(target.isInteger) { 
            var nsServer = NSFW.servers[stripGroup.server.name];
            key = "hwOut_%".format(target);
            bus = nsServer.server.outputBus.subBus(target);
        } {
            key = target.stripId;
            bus = target.stripBus;
        };

        // this needs to get the value of the .stripId ++ "Knob" control
        // ...which doesn't quite work with outStrips yet, must resolve naming
        sends.put(
            key.asSymbol,
            Synth(\ns_stripSend, [\inBus, stripBus, \outBus, bus], fader, \addAfter)
        )
    }

    removeSend { |target|
        var key, synth;
        if(target.isInteger) { key = "hwOut_%".format(target) } { key = target.stripId };
        synth = sends.removeAt(key.asSymbol);
        if(paused) { synth.free } { synth.set(\gate, 0) }
    }

    addModule { |className, slotIndex| 
        forkIfNeeded {
            slots[slotIndex].free;
            slots[slotIndex] = className.new(slotGroups[slotIndex], stripBus);
            if(this.paused) { slots[slotIndex].pause };
            NSFW.servers[stripGroup.server.name].cond.wait { slots[slotIndex].loaded }
        }
    }

    freeModule { |slotIndex|
        var pageIndex  = stripId.first;
        var stripIndex = stripId.last.digit;
        pageIndex      = if(pageIndex.isAlpha) { pageIndex } { pageIndex.digit };
        NS_Controller.allActive.do { |ctrl| 
            ctrl.removeModuleFragment(pageIndex, stripIndex, slotIndex)
        };
        slots[slotIndex].free;
        slots[slotIndex] = nil;
    }

    toggleAllVisible { 
        slots.do { |mod| mod !? { mod.toggleView } } 
    }

    free {
        slots.do { |slt, index| this.freeModule(index) };
        controlDict.do { |ctrl| ctrl.resetValue };
    }

    saveExtra {
        ^slots.collect { |slt| slt !? { slt.save } }
    }

    loadExtra { |loadArray, cond, action|

        loadArray.do({ |slotLoad, slotIndex|
            slotLoad !? {
                slots[slotIndex].load(slotLoad, cond, { cond.signalOne });
                cond.wait { slots[slotIndex].loaded }
            }
        });

        action.value;
    }
}

NS_ChannelStrip : NS_StripBase {
    const <numSlots = 6; // do these need to be getters??
    var <inGroup, <inSynth;

    *new { |stripId, group|
        ^super.new(stripId, group, numSlots)
    }

    makeSendCtrls { |nsServer|

        NS_Server.numOutStrips.do { |stripNum|
            var stripId = "O:%".format(stripNum);
            controlDict.addAll(
                NS_ControlInt(stripId ++ "Toggle", 0, 1, 0)
                .addAction(\send,{ |c|
                    if(c.value == 1)
                    { this.addSend(nsServer.outStrips[stripNum]) }
                    { this.removeSend(nsServer.outStrips[stripNum]) }
                }),
                NS_ControlFloat(stripId ++ "Knob", \db, 0)
                .addAction(\send,{ |c|
                    sends[stripId.asSymbol].set(\amp, c.value.dbamp)
                })
            )
        }
    }

    makeInputSynth { |nsServer|
        var numChans = nsServer.options.numChans;
        inGroup = Group(stripGroup, \addToHead);

        nsServer.addSynthDefCreateSynth(
            inGroup,
            \ns_stripIn,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = sig * \gateBool.kr(0, 0.01);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, 1, 1)
            },
            [\bus, stripBus],
            { |synth| inSynth = synth }
        )
    }

    pause {
        inSynth.set(\pauseGate, 0);
        slots.do { |mod| mod !? { mod.pause } };
        fader.set(\pauseGate, 0);
        sends.do { |snd| snd.set(\pauseGate, 0) };
        stripGroup.run(false);
        this.paused = true;
    }

    unpause {
        inSynth.set(\pauseGate, 1); inSynth.run(true);
        slots.do { |mod| mod !? { mod.unpause } };
        fader.set(\pauseGate, 1); fader.run(true);
        sends.do { |snd| snd.set(\pauseGate, 1); snd.run(true) };
        stripGroup.run(true);
        this.paused = false;
    }
}

NS_OutStrip : NS_StripBase {
    const <numSlots = 4;

    *new { |stripId, group|
        ^super.new(stripId, group, numSlots)
    }

    makeSendCtrls { |nsServer|
        var numChans = nsServer.options.numChans;
        var outChans = nsServer.options.outChannels;

        var possibleOuts = (outChans - (numChans - 1)).collect { |startChan|
            [startChan, startChan + (numChans - 1)]
        };

        possibleOuts.do({ |chanPair|
            var outChanString = "%-%".format(*chanPair);

            controlDict.add(
                NS_ControlInt(outChanString, 0, 1, 0)
                .addAction(outChanString.asSymbol, { |c|
                    if(c.value == 1) 
                    { this.addSend(chanPair[0]) } 
                    { this.removeSend(chanPair[0]) }
                }, false)
            )
        })
    }
}

NS_InStrip : NS_StripBase {
    const <numSlots = 3;
    var <inBus = 0;
    var <inGroup, <inSynth;
    var <responder;

    *new { |stripId, group|
        ^super.new(stripId, group, numSlots)
    }

    makeFaderSynth { |nsServer, group|
        var numChans = nsServer.options.numChans;
        stripBus = Bus.audio(group.server, numChans);

        nsServer.addSynthDefCreateSynth(
            group, 
            \ns_inStripFader,
            {
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0, 0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0, 0.01));
                SendPeakRMS.ar(sig.sum * numChans.reciprocal.sqrt, cmdName: '/peakRMS');

                NS_Out(sig, numChans, \bus.kr, 1, 1)
            },
            [\bus, stripBus],
            { |synth| 
                fader = synth;

                controlDict.addAll(
                    NS_ControlFloat(\amp, \db)
                    .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) }),

                    NS_ControlInt(\mute, 0, 1, 0)
                    .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false)
                )
            }
        )
    }

    makeSendCtrls { |nsServer|

        NS_Server.numPages.do { |pageNum|
            NS_Server.numStrips.do { |stripNum|
                var stripId = "%:%".format(pageNum, stripNum);
                controlDict.addAll(
                    NS_ControlInt(stripId ++ "Toggle", 0, 1, 0)
                    .addAction(\send,{ |c|
                        if(c.value == 1)
                        { this.addSend(nsServer.strips[pageNum][stripNum]) }
                        { this.removeSend(nsServer.strips[pageNum][stripNum]) }
                    }),
                    NS_ControlFloat(stripId ++ "Knob", \db, 0)
                    .addAction(\send,{ |c|
                        sends[stripId.asSymbol].set(\amp, c.value.dbamp)
                    })
                )
            }
        };

        NS_Server.numOutStrips.do { |stripNum|
            var stripId = "O:%".format(stripNum);
            controlDict.addAll(
                NS_ControlInt(stripId ++ "Toggle", 0, 1, 0)
                .addAction(\send,{ |c|
                    if(c.value == 1)
                    { this.addSend(nsServer.outStrips[stripNum]) }
                    { this.removeSend(nsServer.outStrips[stripNum]) }
                }),
                NS_ControlFloat(stripId ++ "Knob", \db, 0)
                .addAction(\send,{ |c|
                    sends[stripId.asSymbol].set(\amp, c.value.dbamp)
                })
            )
        }
    }

    makeInputSynth { |nsServer|
        var numChans = nsServer.options.numChans;
        var inputBus = nsServer.server.inputBus;
        inGroup = Group(stripGroup, \addToHead);

        nsServer.addSynthDefCreateSynth(
            inGroup,
            \ns_inputMono,
            {
                var sig = In.ar(\inBus.kr()); // mono in
                sig = sig ! numChans;         // expand to numChans
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), 1);
                Out.ar(\outBus.kr, sig)
            },
            [\inBus, inputBus.subBus(inBus), \outBus, stripBus],
            { |synth| 
                inSynth = synth;

                controlDict.add(
                    NS_ControlString(stripId ++ "_inBus", "0")
                    .addAction(\synth,{ |c|
                        var val = c.value.asInteger;
                        if(val < nsServer.options.inChannels,{
                            inBus = val;
                            inSynth.set(\inBus, inputBus.subBus(inBus))
                        },{
                            // could add color change for emphasis?
                            fork{ c.value_("N/A"); 0.5.wait; c.resetValue }
                        })
                    })
                )
            }
        )
    }

    addResponder { |levelMeter|
        responder = OSCFunc({ |msg|
            var peak = msg[3];
            var rms = msg[4];

            { levelMeter.value_(peak, rms) }.defer;

        }, '/peakRMS', stripGroup.server.addr, nil, [fader.nodeID])
    }

    freeResponder { responder.free; responder = nil }

    pause {
        inSynth.set(\pauseGate, 0);
        slots.do { |mod| mod !? { mod.pause } };
        fader.set(\pauseGate, 0);
        sends.do { |snd| snd.set(\pauseGate, 0) };
        stripGroup.run(false);
        this.paused = true;
    }

    unpause {
        inSynth.set(\pauseGate, 1); inSynth.run(true);
        slots.do { |mod| mod !? { mod.unpause } };
        fader.set(\pauseGate, 1); fader.run(true);
        sends.do { |snd| snd.set(\pauseGate, 1); snd.run(true) };
        stripGroup.run(true);
        this.paused = false;
    }
}
