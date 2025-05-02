NS_ChannelStripBase : NS_ControlModule {
    var <stripId, <numChans;
    var <stripGroup, <slotGroups, <faderGroup;
    var <slots;
    var <stripBus;
    var fader, sends, <sendCtrls;
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

        fader      = Synth(\ns_stripFader,[\bus, stripBus],faderGroup);
        sends      = IdentityDictionary();
        sendCtrls  = IdentityDictionary();

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

        this.createSendCtrls(group);
    }

    createSendCtrls { this.subclassResponsibility(thisMethod) }

    // needs a target arg? would be nice to add sends before/after other groups/synths
    addSend { |targetBus, addAction = \addAfter| 
        sends.put(
            targetBus.index.asSymbol,
            Synth(
                \ns_stripSend,
                [\inBus, stripBus, \outBus, targetBus],
                fader, addAction
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

    // I don't like calling controls by their indexes...
    amp  { ^controls[0].normValue }
    amp_ { |amp| controls[0].normValue_(amp) }

    toggleAllVisible {
        slots.do({ |mod| if( mod.notNil,{ mod.toggleVisible }) });
    }

    free {
        slots.do({ |slt, index| this.freeModule(index) });

        // this could almost certainly be better
        sendCtrls.collect({ |c| c.asArray }).asArray.flat.do({ |v|  v.value_(0) });

        this.amp_(0)
    }
}

NS_ChannelStripMatrix : NS_ChannelStripBase {
    var <inGroup, <inSynth;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_stripIn,{
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                sig = sig * \thru.kr(0);
                ReplaceOut.ar(\outBus.kr,sig);
            }).add;
        }
    }

    *new { |stripId, group|
        var numChans = NSFW.numChans(group.server);
        ^super.new(stripId, group, numChans, 6).addInputSynth
    }

    createSendCtrls { |group|
        var nsServer  = NSFW.servers[group.server.name];
        var numPages  = NS_MatrixServer.numPages;
        var numStrips = NS_MatrixServer.numStrips;

        var stripSendsArray = (numPages * numStrips).collect({ |index|
            var pageIndex   = (index / 4).floor.asInteger;
            var stripIndex  = index % 4;
            
            NS_Control("send%:%".format(pageIndex,stripIndex), ControlSpec(0, 1, 'lin', 1), 0)
            .addAction("%:%".format(pageIndex,stripIndex),{ |c|
                var strip = nsServer.strips[pageIndex][stripIndex];
                if(c.value == 1,{
                    this.addSend(strip.stripBus);
                    //  "% sending to: %".format(stripId, "%:%".format(pageIndex,stripIndex)).postln
                },{
                    this.removeSend(strip.stripBus);
                    //  "% no longer sending to: %".format(stripId, "%:%".format(pageIndex,stripIndex)).postln
                })
            }, false)
        });

        var outSendsArray = nsServer.outMixer.collect({ |outStrip, outIndex|
            NS_Control("send%".format(outStrip.stripId), ControlSpec(0, 1, 'lin', 1), 0)
            .addAction(outStrip.stripId,{ |c|
                if(c.value == 1,{
                    this.addSend(outStrip.stripBus);
                    //"% sending to: %".format(stripId, outStrip.stripId).postln
                },{
                    this.removeSend(outStrip.stripBus);
                    //"% no longer sending to: %".format(stripId, outStrip.stripId).postln
                })
            }, false)
        });

        sendCtrls.put(\stripSends, stripSendsArray);
        sendCtrls.put(\outSends,   outSendsArray)
    }

    addInputSynth {
        inGroup = Group(stripGroup,\addToHead);
        inSynth = Synth(\ns_stripIn, [\inBus,stripBus,\outBus,stripBus], inGroup);
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
            if(mod.notNil,{ mod.unpause })
        });
        fader.set(\pauseGate, 1); fader.run(true);
        sends.do({ |snd| snd.set(\pauseGate, 1); snd.run(true) });
        stripGroup.run(true);
        this.paused = false;
    }

    saveExtra { |saveArray|
        var stripArray     = List.newClear(0);
        var moduleArray    = slots.collect({ |slt| slt !? { slt.save } });
        var stripSendArray = sendCtrls['stripSends'].collect({ |ctrl| ctrl.value });
        var outSendArray   = sendCtrls['outSends'].collect({ |ctrl| ctrl.value });

        stripArray.add( moduleArray );
        stripArray.add( stripSendArray );
        stripArray.add( outSendArray );

        ^saveArray.add( stripArray );
    }

    loadExtra { |loadArray, cond, action|

        loadArray[0].do({ |slotArray, slotIndex|
            slotArray !? {
                slots[slotIndex].load(slotArray, cond, { cond.signalOne });
                cond.wait { slots[slotIndex].loaded }
            }
        });

        loadArray[1].do({ |ctrlVal, index|
            sendCtrls['stripSends'][index].value_(ctrlVal);
        });

        loadArray[2].do({ |ctrlVal, index|
            sendCtrls['outSends'][index].value_(ctrlVal);
        });
        
        action.value
    }
}

NS_ChannelStripOut : NS_ChannelStripBase {

    *new { |stripId, group|
        var numChans = NSFW.numChans(group.server);
        ^super.new(stripId, group, numChans, 4)
    }

    createSendCtrls { |group|
        var nsServer = NSFW.servers[group.server.name];
        var numChans = nsServer.options.numChans;
        var outChans = nsServer.options.outChannels;

        var possibleOuts = if(outChans == numChans, {
            [[0, numChans - 1]]
        },{
            (outChans - (numChans - 1)).collect({ |startChan|
                [startChan, startChan + (numChans - 1)]
            })
        });

        var hwSendsArray = possibleOuts.collect({ |chanPair|
            var outBus   = nsServer.server.outputBus.subBus(chanPair[0]);
            var outChannelString = "%-%".format(chanPair[0], chanPair[1]);

            NS_Control(outChannelString, ControlSpec(0, 1, 'lin', 1), 0)
            .addAction(outChannelString.asSymbol,{ |c|
                if(c.value == 1,{
                    this.addSend(outBus);
                    //"% sending to channels: %".format(stripId, outChannelString).postln
                },{
                    this.removeSend(outBus);
                    //"% no longer sending to %".format(stripId, outChannelString).postln
                })
            }, false)
        });

        sendCtrls.put(\hardwareSends, hwSendsArray);
    }

    gateCheck { |bool| /* this needs to be empty */}

    saveExtra { |saveArray|
        var stripArray  = List.newClear(0);
        var moduleArray = slots.collect({ |slt| slt !? { slt.save } });
        var sendArray   = sendCtrls['hardwareSends'].collect({ |ctrl| ctrl.value });

        stripArray.add( moduleArray );
        stripArray.add( sendArray );

        ^saveArray.add( stripArray );
    }

    loadExtra { |loadArray, cond, action|

        loadArray[0].do({ |slotArray, slotIndex|
            slotArray !? {
                slots[slotIndex].load(slotArray, cond, { cond.signalOne });
                cond.wait { slots[slotIndex].loaded }
            }
        });

        loadArray[1].do({ |ctrlVal, index|
            sendCtrls['hardwareSends'][index].value_(ctrlVal);
        });

        action.value;
    }
}

NS_ChannelStripIn : NS_ChannelStripBase {

    *initClass {
        ServerBoot.add{ |server|

           // SynthDef(\ns_inputMono,{
           //     var sig = SoundIn.ar(\inBus.kr());
           //     sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),1);
           //     Out.ar(\outBus.kr, sig)
           // }).add;

           // SynthDef(\ns_inputStereo,{
           //     var inBus = \inBus.kr();
           //     var sig = SoundIn.ar([inBus,inBus + 1]).sum * -3.dbamp;
           //     sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
           //     Out.ar(\outBus.kr, sig)
           // }).add;

           // SynthDef(\ns_inStripFader,{
           //     var sig = In.ar(\bus.kr, 1);
           //     var mute = 1 - \mute.kr(0,0.01); 
           //     sig = ReplaceBadValues.ar(sig);
           //     sig = sig * mute;
           //     sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));

           //     ReplaceOut.ar(\bus.kr, sig)
           // }).add;

            // expansion happens here? Or in the fader?
            //  SynthDef(\ns_stripSend,{
            //      var sig = In.ar(\inBus.kr, 1);
            //      sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
            //      Out.ar(\outBus.kr,sig);
            //  }).add
        }
    }

    *new { |stripId, group|
        ^super.new(stripId, group, 1, 3)
    }

    createSendCtrls { |group| }

    gateCheck {}

    saveExtra {}
    loadExtra { |loadArray, cond, action| 

        action.value
    }
   
}
