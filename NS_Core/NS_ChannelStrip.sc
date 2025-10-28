NS_ChannelStripBase : NS_ControlModule {
    var <>stripId, <>numChans;
    var <stripGroup, <slotGroups, <faderGroup;
    var <slots;
    var <stripBus;
    var fader, sends;
    var <>paused = false;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_stripFader,{
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0, 0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0, 0.01));

                ReplaceOut.ar(\bus.kr, sig)
            }).add;

            SynthDef(\ns_stripSend,{
                var sig = In.ar(\inBus.kr, numChans);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1, 0.01));
                Out.ar(\outBus.kr, sig);
            }).add
        }
    }

    *new { |id, inGroup, numChannels, numModules = 6|
        ^super.new.init(id, inGroup, numChannels, numModules)
    }

    /* controls:
    - controls[0] == \amp
    - controls[1] == \mute
    - makeGroups == 0
    - makeFaderSynth == 0
    - makeSlotCtrls == numModules, dvs. 3 (in), 4 (out), or 6 (matrix) // turn into const
    - makeInputSynth == cStrip adds 4 inBusses, 4 amps; inCStrip adds 1 inBus
    - makeSendCtrls == 4 (in), lots(out), 4(matrix) // depends on numOutchannels for outStrip
    */

    // consider moving makeSendCtrls before makeInputSynth?

    init { |id, group, numChannels, numModules|
        var nsServer = NSFW.servers[group.server.name];
        this.initControlArray(2); // \amp, \mute

        stripId  = id;
        numChans = numChannels;
        slots    = Array.newClear(numModules);
        sends    = IdentityDictionary();

        controls[0] = NS_Control(\amp, \db)
        .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) });

        controls[1] = NS_Control(\mute, ControlSpec(0, 1, 'lin', 1), 0)
        .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false);

        this.makeGroups(group, numModules);
        this.makeFaderSynth(numChans, faderGroup);
        this.makeSlotCtrls(numModules);
        this.makeInputSynth(nsServer);
        this.makeSendCtrls(nsServer);
    }

    makeGroups { |group, numModules|
        var allSlots;
        stripGroup = Group(group,\addToTail);
        allSlots   = Group(stripGroup,\addToTail);
        slotGroups = numModules.collect({ |i| Group(allSlots, \addToTail) });
        faderGroup = Group(stripGroup,\addToTail);
    }

    makeInputSynth { }

    makeFaderSynth { |numChans, group|
        stripBus = Bus.audio(group.server, numChans);
        fader    = Synth(\ns_stripFader, [\bus, stripBus], group);
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
        });
    }

    makeSendCtrls { this.subclassResponsibility(thisMethod) }

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
        slots.do({ |mod| mod !? mod.toggleVisible });
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

NS_ChannelStripMatrix : NS_ChannelStripBase {
    const numSlots = 6;
    var <inGroup, <inSynth;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_matrixStripIn,{
                var sig = 4.collect({ |i|
                    var inBus = NamedControl.kr(("inBus" ++ i).asSymbol, -1);

                    // this bus mapping failsafe was borrowed from here:
                    // https://scsynth.org/t/leaving-control-busses-unassigned/10397/3
                    SelectX.ar(inBus < 0,[In.ar(inBus, numChans), DC.ar(0)]) * 
                    NamedControl.kr(("amp" ++ i).asSymbol, 0)
                });

                sig = sig.sum;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    *new { |stripId, group|
        ^super.new(stripId, group, NSFW.numChans(group.server), numSlots)
    }

    makeInputSynth { |nsServer|
        inGroup = Group(stripGroup, \addToHead);
        inSynth = Synth(\ns_matrixStripIn, [\bus, stripBus], inGroup);

        4.do({ |i|
            var inBus = ("inBus" ++ i).asSymbol;
            controls.add(
                NS_Control(inBus, \string, "in")
                .addAction(\synth,{ |c|
                    var sourcePage = c.value.first.digit;
                    var sourceStrip = c.value.last.digit;

                    case
                    // $i.digit, integer for inputStrip
                    { sourcePage == 18 and: {sourceStrip < NS_MatrixServer.numInStrips} }{
                        // this is post fader, is it what we want?
                        inSynth.set(inBus, nsServer.inputs[sourceStrip].stripBus);
                    }
                    // if sourcePage == integer, it must be a matrixStrip
                    { sourcePage < 10 }{ 
                        var thisPage = stripId.first.digit;
                        var thisStrip = stripId.last.digit;

                        var stripBool = sourceStrip != thisStrip;
                        var pageBool = case
                        { sourcePage < thisPage}{ true }
                        { sourcePage == thisPage and: {sourceStrip < thisStrip} }{ true }
                        { false };

                        if(stripBool and: pageBool,{
                            // this is post fader, is it what we want?
                            inSynth.set(inBus, nsServer.strips[sourcePage][sourceStrip].stripBus);
                        },{
                            fork{
                                // could add color change for emphasis?
                                c.value_("N/A");
                                0.5.wait;
                                c.resetValue
                            }
                        })
                    }
                    { inSynth.set(inBus, -1) };
                })
            )
        });

        4.do({ |i|
            var amp = ("amp" ++ i).asSymbol;
            controls.add(
                NS_Control(amp, \db)
                .addAction(\synth,{ |c| inSynth.set(amp, c.value.dbamp) })
            )
        })
    }

    makeSendCtrls { |nsServer|

        nsServer.outMixer.do({ |outStrip|
            controls.add(
                NS_Control(outStrip.stripId, ControlSpec(0,1,'lin', 1), 0)
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
        ^super.new(stripId, group, NSFW.numChans(group.server), numSlots)
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
                    if(c.value == 1,{
                        this.addSend(outBus);
                    },{
                        this.removeSend(outBus);
                    })
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

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_inputMono,{
                var sig = In.ar(\inBus.kr());
                sig = sig ! numChans;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),1);
                Out.ar(\outBus.kr, sig)
            }).add;

            // SynthDef(\ns_inputStereo,{
            //     var inBus = \inBus.kr();
            //     var sig = SoundIn.ar([inBus,inBus + 1]).sum * -3.dbamp;
            //     sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
            //     Out.ar(\outBus.kr, sig)
            // }).add;

            SynthDef(\ns_inStripFader,{
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0, 0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0, 0.01));
                SendPeakRMS.ar(sig.sum * numChans.reciprocal.sqrt, cmdName: '/peakRMS');

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add;
        }
    }

    *new { |stripId, group|
        ^super.new(stripId, group, NSFW.numChans(group.server), numSlots)
    }

    makeInputSynth { |nsServer|
        inGroup = Group(stripGroup, \addToHead);
        inSynth = Synth(\ns_inputMono, [
            \inBus, nsServer.server.inputBus.subBus(inBus), \outBus, stripBus
        ], inGroup);

        controls.add(
            NS_Control(stripId ++ "_inBus", \string, "0")
            .addAction(\synth,{ |c|
                var val = c.value.asInteger;
                if(val < nsServer.options.inChannels,{
                    inBus = val;
                    inSynth.set(\inBus, nsServer.server.inputBus.subBus(inBus))
                },{
                    fork{
                        // could add color change for emphasis?
                        c.value_("N/A");
                        0.5.wait;
                        c.resetValue
                    }
                })
            })
        )
    }

    makeFaderSynth { |numChans, group|
        stripBus = Bus.audio(group.server, numChans);
        fader    = Synth(\ns_inStripFader, [\bus, stripBus], faderGroup);
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
