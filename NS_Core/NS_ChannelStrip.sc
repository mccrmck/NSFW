NS_ChannelStripBase : NS_ControlModule {
    var <stripId, <numChans;
    var <stripGroup, <inGroup, <slotGroups, <faderGroup;
    var <slots;
    var <stripBus;
    var fader, sends, <sendCtrls;
    var <>paused = false;

    var <view;

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
        this.initControlArrays(3);
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
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(30);

        controls[1] = NS_Control(\visible,ControlSpec(0,0,'lin',1))
        .addAction(\synth, { |c| this.toggleAllVisible }, false);
        assignButtons[1] = NS_AssignButton(this,1,\button).maxWidth_(30);

        controls[2] = NS_Control(\mute,ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false);
        assignButtons[2] = NS_AssignButton(this,2,\button).maxWidth_(30);
    
        this.createSendCtrls(group);
    }

    createSendCtrls { this.subclassResponsibility(thisMethod) }

    addSend { |targetBus|
        sends.put(
            targetBus.index.asSymbol,
            Synth(
                \ns_stripSend,
                [\inBus, stripBus, \outBus, targetBus],
                faderGroup, \addToTail
            )
        );
    }

    removeSend { |targetBus|
        var key = targetBus.index.asSymbol;
        sends[key].set(\gate, 0);
        sends.removeAt(key);
    }

    addModule { |className, slotIndex|
        slots[slotIndex] = className.new(this, slotIndex);
    }

    freeModule { |slotIndex|
        slots[slotIndex].free;
        slots[slotIndex] = nil;
    }



    // move this to NS_SynthModule!!
    inSynthGate_ { this.subclassResponsibility(thisMethod) } // this needs an overhaul

    // I don't like calling controls by their indexes...
    amp  { ^controls[0].normValue }
    amp_ { |amp| controls[0].normValue_(amp) }

    toggleAllVisible {
        slots.do({ |mod| if( mod.notNil,{ mod.toggleVisible }) });
    }

    free {
       // this.setInSynthGate(0);
        slots.do({ |slt, index| this.freeModule(index) });

        // free all sends/reset sendButtons
        // reset buttons state, or?
        this.amp_(0)
    }

    pause {
       // inSynth.set(\pauseGate, 0);
        slots.do({ |mod|
            if(mod.notNil,{ mod.pause })
        });
        fader.set(\pauseGate, 0);
        sends.do({ |snd| snd.set(\pauseGate, 0) });
        stripGroup.run(false);
        this.paused = true;
    }

    unpause {
     //   inSynth.set(\pauseGate, 1);
     //   inSynth.run(true);
      //  if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.unpause });
        slots.do({ |mod| 
            if(mod.notNil,{ mod.unpause })
        });
        fader.set(\pauseGate, 1);
        fader.run(true);
        sends.do({ |snd| snd.set(\pauseGate, 1); snd.run(true) });
        stripGroup.run(true);
        this.paused = false;
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
        var numChans = NSFW.servers[group.server.name].options.numChans;
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
                    this.addSend(strip.stripBus)
                },{
                    this.removeSend(strip.stripBus)
                })
            },false)
        });

        var outSendsArray = nsServer.outMixer.collect({ |outStrip, outIndex|
            NS_Control("send%".format(outStrip.stripId), ControlSpec(0, 1, 'lin', 1), 0)
            .addAction(outStrip.stripId,{ |c|
                if(c.value == 1,{
                    this.addSend(outStrip.stripBus)
                },{
                    this.removeSend(outStrip.stripBus)
                })
            },false)
        });

        sendCtrls.put(\stripSends, stripSendsArray);
        sendCtrls.put(\outSends,   outSendsArray)
    }

    addInputSynth {
        inGroup = Group(stripGroup,\addToHead);
        inSynth = Synth(\ns_stripIn, [\inBus,stripBus,\outBus,stripBus], inGroup);
    }

    inSynthGate_ {}

    //saveExtra { |saveArray|
    //    var stripArray = List.newClear(0);
    //    var inSinkArray = if(inSink.module.notNil,{ inSink.save }); 
    //    slots.collect(_.save) (if module.notNil)
    //    var sinkArray = moduleSinks.collect({ |sink|   
    //        if(sink.module.notNil,{ sink.save })
    //    });
    //    stripArray.add( inSinkArray );
    //    stripArray.add( sinkArray );
    //    stripArray.add( inSynthGate );

    //    saveArray.add(stripArray);

    //    ^saveArray
    //}

    //loadExtra { |loadArray|
    //    var cond = CondVar();
    //    var count = 0;

    //    {
    //        if(loadArray[0].notNil,{ inSink.load( loadArray[0] ) });

    //        loadArray[1].do({ |sinkArray, index|
    //            if(sinkArray.notNil,{
    //                moduleSinks[index].load(sinkArray, slotGroups[index])
    //            });
    //            count = count + 1
    //        });
    //        cond.wait( count == loadArray[1].size );

    //        this.setInSynthGate( loadArray[2] );
    //        inSynth.set( \thru, inSynthGate.sign );
    //        // 0.5.wait;
    //        // this.toggleAllVisible
    //    }.fork(AppClock)
    //}
}

NS_ChannelStripOut : NS_ChannelStripBase {


    *new { |stripId, group|
        var numChans = NSFW.servers[group.server.name].options.numChans;
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
                    "% sending to channels: %".format(
                        stripId, outChannelString
                    ).postln
                },{
                    this.removeSend(outBus);
                    "% no longer sending to %".format(
                        stripId, outChannelString
                    ).postln
                })
            }, false)
        });

        sendCtrls.put(\hardwareSends, hwSendsArray);
    }

    saveExtra {}

    loadExtra {}
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
   
}
