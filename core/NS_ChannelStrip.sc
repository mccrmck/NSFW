NS_ChannelStripBase : NS_ControlModule {
    var <stripId, <numChans;
    var <stripGroup, <slotGroups, <faderGroup;
    var <slots;
    var <stripBus;
    var fader, sends;
    var <>paused = false;

    *new { |id, inGroup, numModules = 6|
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

        controls.addAll(
            NS_Control(\amp, \db)
            .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) }),

            NS_Control(\mute, ControlSpec(0, 1, 'lin', 1), 0)
            .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false)
        );

        // check if they need *this* nsServer server or just NS_Server
        // or maybe make nsServer a variable for all functions to access, way cleaner
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
            { |synth| fader = synth }
        )
    }

    makeSlotCtrls { |numModules|
        numModules.do({ |modIndex|
            controls.add(
                NS_Control("module" ++ modIndex, \string, "")
                .addAction(\module, { |c| 
                    if(c.value.size > 0,{
                        var className = ("NS_" ++ c.value).asSymbol.asClass;
                        this.addModule(className, modIndex);
                    },{
                        this.freeModule(modIndex)
                    })
                }, false)
            )
        })
    }

    makeSendCtrls { this.subclassResponsibility(thisMethod) }
    makeInputSynth {}

    // should I add source, target, addAction args? Could then create pre-fader sends
    addSend { |targetBus| 

        sends.put(
            targetBus.index.asSymbol,
            Synth(
                \ns_stripSend,
                [\inBus, stripBus, \outBus, targetBus],
                fader, \addAfter
            )
        )
    }

    removeSend { |targetBus|
        var key = targetBus.index.asSymbol;
        sends[key].set(\gate, 0);
        sends.removeAt(key);
    }

    addModule { |className, slotIndex| 
        var nsServer  = NSFW.servers[stripGroup.server.name];
        forkIfNeeded{
            slots[slotIndex].free;
            slots[slotIndex] = className.new(this, slotIndex);
            if(this.paused,{ slots[slotIndex].pause });
            nsServer.cond.wait { slots[slotIndex].loaded  }
        }
    }

    freeModule { |slotIndex|
        var pageIndex  = stripId.first;
        var stripIndex = stripId.last.digit;
        pageIndex      = if(pageIndex.isAlpha,{ pageIndex },{ pageIndex.digit });
        NS_Controller.allActive.do({ |ctrl| 
            ctrl.removeModuleFragment(pageIndex, stripIndex, slotIndex)
        });
        slots[slotIndex].free;
        slots[slotIndex] = nil;
    }

    gateCheck { |bool| /* must be empty for in and out strips */  }

    toggleAllVisible {
        slots.do({ |mod| mod !? {mod.toggleVisible} });
    }

    free {
        slots.do({ |slt, index| this.freeModule(index) });
        controls.do({ |ctrl| ctrl.resetValue });          // confirm this works
    }

    saveExtra { |saveArray|
        var stripArray  = List.newClear(0);
        var moduleArray = slots.collect({ |slt| slt !? { slt.save } });

        stripArray.add( moduleArray );

        ^saveArray.add( stripArray );
    }

    loadExtra { |loadArray, cond, action|

        loadArray[0].do({ |slotArray, slotIndex|
            slotArray !? {
                slots[slotIndex].load(slotArray, cond, { cond.signalOne });
                cond.wait { slots[slotIndex].loaded }
            }
        });

        action.value;
    }
}

NS_ChannelStrip : NS_ChannelStripBase {
    const numSlots = 6;
    var <inGroup, <inSynth;

    *new { |stripId, group|
        ^super.new(stripId, group, numSlots)
    }

    makeSendCtrls { |nsServer|

        nsServer.outMixer.do({ |outStrip|
            controls.add(
                NS_Control(outStrip.stripId, ControlSpec(0, 1, 'lin', 1), 0)
                .addAction(\send,{ |c|
                    if(c.value == 1,{
                        this.addSend(outStrip.stripBus)
                    },{
                        this.removeSend(outStrip.stripBus)
                    })
                })
            )
        })
    }

    makeInputSynth { |nsServer|
        var numChans = nsServer.options.numChans;
        inGroup = Group(stripGroup, \addToHead);

        nsServer.addSynthDefCreateSynth(
            inGroup,
            \ns_stripIn,
            {
                var sig = 4.collect({ |i|
                    var inBus = NamedControl.kr(("inBus" ++ i).asSymbol, -1);

                    // this bus mapping failsafe was borrowed from here:
                    // https://scsynth.org/t/leaving-control-busses-unassigned/10397/3
                    SelectX.ar(inBus < 0,[In.ar(inBus, numChans), DC.ar(0)]) * 
                    NamedControl.kr(("amp" ++ i).asSymbol, 0) *
                    (1 - NamedControl.kr(("mute" ++ i).asSymbol, 0))
                });

                sig = sig.sum;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, stripBus],
            { |synth|
                inSynth = synth;

                // 4 should be a classvar or const, like numReceives = 4;
                4.do({ |i|

                    // maybe these names could be a bit more descriptive?
                    var inBus = ("inBus" ++ i).asSymbol;
                    var amp = ("amp" ++ i).asSymbol;
                    var mute = ("mute" ++ i).asSymbol;

                    controls.addAll(
                        NS_Control(inBus, \string, "in")
                        .addAction(\synth,{ |c| 
                            var sourcePage  = c.value.first.digit;
                            var sourceStrip = c.value.last.digit; 
                            var thisPage  = stripId.first.digit;
                            var thisStrip = stripId.last.digit;

                            var earlierPage  = sourcePage  < thisPage;
                            var samePage     = sourcePage == thisPage;
                            var earlierStrip = sourceStrip < thisStrip;

                            var differentStrips = sourceStrip != thisStrip;
                            var pageOkay = case
                            { earlierPage }{ true }
                            { samePage and: earlierStrip }{ true }
                            { false };

                            // $i.digit, integer for inputStrip
                            var iIsFirstDigit = sourcePage == 18; 
                            // prevents the edge where "in" is incoming value
                            // however, if "in" is incoming value, inBus is set to -1
                            // which sets the value to in...which is maybe a weird reset?
                            // I mean, if you drag "in" to a receive, what do you expect? 
                            var nIsNotSecondDigit = sourceStrip < NS_Server.numInStrips;
                            // sourcePage == integer dvs. strip
                            var intFirstDigit = sourcePage  < 10; 

                            // sends are postfader by default
                            // consider adding a pre/post fader toggle in a context menu
                            case
                            { iIsFirstDigit and: nIsNotSecondDigit }{
                                inSynth.set(inBus, nsServer.inputs[sourceStrip].stripBus);
                            }
                            { intFirstDigit }{ 
                                if(differentStrips and: pageOkay,{
                                    var strip = nsServer.strips[sourcePage][sourceStrip];
                                    inSynth.set(inBus, strip.stripBus);
                                },{
                                    // could add color change for emphasis?
                                    fork{ c.value_("N/A"); 0.5.wait; c.resetValue }
                                })
                            }
                            { inSynth.set(inBus, -1) };
                        }),

                        NS_Control(amp, \db)
                        .addAction(\synth,{ |c| inSynth.set(amp, c.value.dbamp) }),

                        NS_Control(mute, ControlSpec(0, 1, 'lin', 1), 0)
                        .addAction(\synth,{ |c| inSynth.set(mute, c.value) })
                    );
                });
            }
        )
    }

    gateCheck {
        var modules = slots.reject({ |i| i == nil });
        var gateSum = modules.collect({ |mod| mod.gateBool.binaryValue }).sum;
        inSynth.set(\thru, gateSum.sign)
    }

    pause {
        inSynth.set(\pauseGate, 0);
        slots.do({ |mod|
            if(mod.notNil,{ mod.pause })
        });
        fader.set(\pauseGate, 0);
        sends.do({ |snd| snd.set(\pauseGate, 0) });
        stripGroup.run(false);
        this.paused = true;
    }

    unpause {
        inSynth.set(\pauseGate, 1); inSynth.run(true);
        slots.do({ |mod| 
            if(mod.notNil, { mod.unpause })
        });
        fader.set(\pauseGate, 1); fader.run(true);
        sends.do({ |snd| snd.set(\pauseGate, 1); snd.run(true) });
        stripGroup.run(true);
        this.paused = false;
    }
}

NS_ChannelStripOut : NS_ChannelStripBase {
    const numSlots = 4;

    *new { |stripId, group|
        ^super.new(stripId, group, numSlots)
    }

    makeSendCtrls { |nsServer|
        var numChans = nsServer.options.numChans;
        var outChans = nsServer.options.outChannels;

        var possibleOuts = if(outChans == numChans, {
            [[0, numChans - 1]]
        },{
            (outChans - (numChans - 1)).collect({ |startChan|
                [startChan, startChan + (numChans - 1)]
            })
        });

        possibleOuts.do({ |chanPair|
            var outBus        = nsServer.server.outputBus.subBus(chanPair[0]);
            var outChanString = "%-%".format(*chanPair);

            controls.add(
                NS_Control(outChanString, ControlSpec(0, 1, 'lin', 1), 0)
                .addAction(outChanString.asSymbol,{ |c|
                    if(c.value == 1) 
                    { this.addSend(outBus) } 
                    { this.removeSend(outBus) }
                }, false)
            )
        })
    }
}

NS_ChannelStripIn : NS_ChannelStripBase {
    const numSlots = 3;
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

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, stripBus],
            { |synth| fader = synth }
        )
    }

    makeSendCtrls { |nsServer|
        nsServer.outMixer.do({ |outStrip, i|
            controls.add(
                NS_Control(outStrip.stripId, ControlSpec(0, 1, 'lin', 1), 0)
                .addAction(\send,{ |c|
                    if(c.value == 1,{
                        this.addSend(outStrip.stripBus)
                    },{
                        this.removeSend(outStrip.stripBus)
                    })
                })
            )
        })
    }

    makeInputSynth { |nsServer|
        var numChans = nsServer.options.numChans;
        var inputBus = nsServer.server.inputBus;
        inGroup = Group(stripGroup, \addToHead);

        nsServer.addSynthDefCreateSynth(
            inGroup,
            \ns_inputMono,
            {
                var sig = In.ar(\inBus.kr());
                sig = sig ! numChans;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),1);
                Out.ar(\outBus.kr, sig)
            },
            [\inBus, inputBus.subBus(inBus), \outBus, stripBus],
            { |synth| 
                inSynth = synth;

                controls.add(
                    NS_Control(stripId ++ "_inBus", \string, "0")
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

    freeResponder { responder.free; responder = nil; }

    pause {
        inSynth.set(\pauseGate, 0);
        slots.do({ |mod|
            if(mod.notNil,{ mod.pause })
        });
        fader.set(\pauseGate, 0);
        sends.do({ |snd| snd.set(\pauseGate, 0) });
        stripGroup.run(false);
        this.paused = true;
    }

    unpause {
        inSynth.set(\pauseGate, 1); inSynth.run(true);
        slots.do({ |mod| 
            if(mod.notNil, { mod.unpause })
        });
        fader.set(\pauseGate, 1); fader.run(true);
        sends.do({ |snd| snd.set(\pauseGate, 1); snd.run(true) });
        stripGroup.run(true);
        this.paused = false;
    }
}
