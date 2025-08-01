NS_ChannelStripBase : NS_ControlModule {
    var <stripId, <numChans;
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
                var mute = 1 - \mute.kr(0,0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));

                ReplaceOut.ar(\bus.kr, sig)
            }).add;

            SynthDef(\ns_stripSend,{
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                Out.ar(\outBus.kr,sig);
            }).add
        }
    }

    *new { |stripId, inGroup, numChans, numModules = 6|
        ^super.new.init(stripId, inGroup, numChans, numModules)
    }

    // break this up into a bunch of factory functions:
    // .addFader
    // .addInputSynth
    // .addModuleCtrls
    // .addsendCtrls
    // etc.

    // also consider moving the save extra functions up here
    // maybe the pause functions too?
    init { |id, group, chansIn, numModules|
        var allSlots;
        this.initControlArray(3 + numModules);
        stripId    = id;
        numChans   = chansIn;

        stripGroup = Group(group,\addToTail);
        allSlots   = Group(stripGroup,\addToTail);
        slotGroups = numModules.collect({ |i| Group(allSlots, \addToTail) });
        faderGroup = Group(stripGroup,\addToTail);

        slots      = Array.newClear(numModules);
        stripBus   = Bus.audio(group.server, numChans);

        fader      = Synth(\ns_stripFader,[\bus, stripBus], faderGroup);
        sends      = IdentityDictionary();

        controls[0] = NS_Control(\amp,\db)
        .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) });

        controls[1] = NS_Control(\visible,ControlSpec(0,0,'lin',1))
        .addAction(\synth, { |c| this.toggleAllVisible }, false);

        controls[2] = NS_Control(\mute,ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false);

        numModules.do({ |modIndex|
            controls[modIndex + 3] = NS_Control("module" ++ modIndex, \string, "")
            .addAction(\module, { |c| 
                if(c.value.size > 0,{
                    var className = ("NS_" ++ c.value).asSymbol.asClass;
                    this.addModule(className, modIndex);
                },{
                    this.freeModule(modIndex)
                })
            }, false)
        });

        this.createSendCtrls;
    }

    createSendCtrls { this.subclassResponsibility(thisMethod) }

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

    gateCheck { this.subclassResponsibility(thisMethod) }

    toggleAllVisible {
        slots.do({ |mod| if( mod.notNil,{ mod.toggleVisible }) });
    }

    free {
        slots.do({ |slt, index| this.freeModule(index) });
        controls.do({ |ctrl| ctrl.resetValue });          // confirm this works
    }
}

NS_ChannelStripMatrix : NS_ChannelStripBase {
    var <inGroup, <inSynth;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_matrixStripIn,{
                var sig = 4.collect({ |i|
                    var inBus = NamedControl.kr(("inBus" ++ i).asSymbol,-1);

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
        var numChans = NSFW.numChans(group.server);
        ^super.new(stripId, group, numChans, 6).addInputSynth
    }

    createSendCtrls {
        var nsServer = NSFW.servers[stripGroup.server.name];

        nsServer.outMixer.do({ |outStrip, i|
            controls.add(
                NS_Control(outStrip.stripId, ControlSpec(0,1,'lin',1), 0)
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

    addInputSynth {
        var nsServer = NSFW.servers[stripGroup.server.name];

        inGroup = Group(stripGroup,\addToHead);
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
                    { sourcePage == 18 and: {sourceStrip < nsServer.options.inChannels} }{
                        var source = nsServer.inputs[sourceStrip];
                        inSynth.set(inBus, source.stripBus);  // this is post fader, is it what we want?
                    }
                    // if sourcePage == integer, it must be a matrixStrip
                    { sourcePage < 10 }{ 
                        var thisPage  = stripId.first.digit;
                        var thisStrip = stripId.last.digit;

                        var stripBool = sourceStrip != thisStrip;
                        var pageBool  = case
                        { sourcePage < thisPage}{ true }
                        { sourcePage == thisPage and: {sourceStrip < thisStrip} }{ true }
                        { false };

                        if(stripBool and: pageBool,{
                            var source = nsServer.strips[sourcePage][sourceStrip];
                            inSynth.set(inBus, source.stripBus);  // this is post fader, is it what we want?
                        },{
                            fork{
                                // could add color change for emphasis
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
                .addAction(\synth,{ |c|
                    inSynth.set(amp, c.value.dbamp);
                })
            )
        });
    }

    gateCheck {
        var modules = slots.reject({ |i| i == nil });
        var gateSum = modules.collect({ |mod| mod.gateBool.binaryValue }).sum;
        gateSum.postln;
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

        action.value
    }
}

NS_ChannelStripOut : NS_ChannelStripBase {

    *new { |stripId, group|
        var numChans = NSFW.numChans(group.server);
        ^super.new(stripId, group, numChans, 4)
    }

    createSendCtrls {
        var nsServer = NSFW.servers[stripGroup.server.name];
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
                        //"% sending to channels: %".format(stripId, outChanString).postln
                    },{
                        this.removeSend(outBus);
                        //"% no longer sending to %".format(stripId, outChanString).postln
                    })
                }, false)
            )
        })
    }

    gateCheck { |bool| /* this needs to be empty */}

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

NS_ChannelStripIn : NS_ChannelStripBase {
    var <inGroup, <inSynth;
    var <responder;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_inputMono,{
                var sig = In.ar(\inBus.kr());
                //var sig = SinOsc.ar(240) * -18.dbamp;
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
                var mute = 1 - \mute.kr(0,0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0,0.01));
                SendPeakRMS.ar(sig.sum * numChans.reciprocal.sqrt, cmdName: '/peakRMS');

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add;
        }
    }

    *new { |stripId, group|
        var numChans = NSFW.numChans(group.server);
        ^super.new(stripId, group, numChans, 3)
        .addInputSynth
        .replaceFader
    }

    addInputSynth {
        var nsServer = NSFW.servers[stripGroup.server.name];
        var index    = stripId.last.digit;
        var inBus    = nsServer.server.inputBus.subBus(index);

        inGroup = Group(stripGroup,\addToHead);
        inSynth = Synth(\ns_inputMono, [\inBus, inBus, \outBus, stripBus], inGroup);
    }

    replaceFader {
        fader.free;
        fader = Synth(\ns_inStripFader,[\bus, stripBus], faderGroup);

    }

    addResponder { |levelMeter|
        responder = OSCFunc({ |msg|
            var peak = msg[3];
            var rms = msg[4];

            { levelMeter.value_(peak, rms) }.defer;

        },'/peakRMS', stripGroup.server.addr, nil, [fader.nodeID])
    }

    freeResponder {
        responder.free;
        responder = nil;
    }

    createSendCtrls {
        var nsServer = NSFW.servers[stripGroup.server.name];

        nsServer.outMixer.do({ |outStrip, i|
            controls.add(
                NS_Control(outStrip.stripId, ControlSpec(0,1,'lin',1), 0)
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

    gateCheck {}

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
