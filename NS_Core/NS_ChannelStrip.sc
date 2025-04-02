NS_ChannelStripBase : NS_ControlModule {
    var <pageIndex, <stripIndex;
    var <stripGroup, <inGroup, <slotGroups, <faderGroup;
    var <slots;
    var <stripBus;
    var fader, sends;
    var <>paused = false;

    var <view;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_stripIn,{
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                sig = sig * \thru.kr(0);
                ReplaceOut.ar(\outBus.kr,sig);
            }).add;

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

    *new { |pageIndex, stripIndex, inGroup, numModules = 6|
        ^super.new.init(pageIndex, stripIndex, inGroup, numModules)
    }

    // how do I save routing?!? Do it in the RoutingView class
    addSend { |outBus|

        sends.put(outBus.index,
            Synth(
                \ns_stripSend,
                [\inBus, stripBus, \outBus, outBus],
                faderGroup,
                \addToTail
            )
        );
    }

    removeSend { |outBus|
        var key = outBus.index;
        sends[key].set(\gate, 0);
        sends.removeAt(key);

    }

    moduleArray { ^slots.collect({ |slt| slt.module }) }


    // move this to NS_SynthModule!!
    inSynthGate_ { this.subClassResponsibility(thisMethod) } // this needs an overhaul

    // I don't like calling controls by their indexes...
    amp  { ^controls[0].normValue }
    amp_ { |amp| controls[0].normValue_(amp) }

    toggleAllVisible {
        this.moduleArray.do({ |mod| if( mod.notNil,{ mod.toggleVisible }) });
    }

    free {
       // this.setInSynthGate(0);
        slots.do({ |slt| slt.free });
        // reset buttons state, or?
        this.amp_(0)
    }

    pause {
       // inSynth.set(\pauseGate, 0);
        this.moduleArray.do({ |mod|
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
        this.moduleArray.do({ |mod| 
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
    var <inSynth;
    var <slotGate;

    init { |pageIn, stripIn, group, numModules|
        var allSlots;
        this.initControlArrays(3);
        pageIndex  = pageIn;
        stripIndex = stripIn;

        stripGroup = Group(group,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        allSlots   = Group(stripGroup,\addToTail);
        slotGroups = numModules.collect({ |i| Group(allSlots,\addToTail) });
        faderGroup = Group(stripGroup,\addToTail);
        
        slots      = numModules.collect({ |i| NS_ModuleSlot(this, i) });
        slotGate   = Array.fill(numModules, { false });
        stripBus   = Bus.audio(group.server, NSFW.numChans(group.server));

        inSynth    = Synth(\ns_stripIn, [\inBus,stripBus,\outBus,stripBus], inGroup);
        fader      = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);
        sends      = IdentityDictionary();
        
        controls[0] = NS_Control(\amp,\db)
        .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) });
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(30);

        controls[1] = NS_Control(\visible,ControlSpec(0,0,'lin',1))
        .addAction(\synth, { |c| this.toggleAllVisible }, false);
        assignButtons[1] = NS_AssignButton(this,1,\button).maxWidth_(30);

        controls[2] = NS_Control(\mute,ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false);
        assignButtons[2] = NS_AssignButton(this,2,\button).maxWidth_(30);

        view = NS_ChannelStripMatrixView(this)
    }

    //saveExtra { |saveArray|
    //    var stripArray = List.newClear(0);
    //    var inSinkArray = if(inSink.module.notNil,{ inSink.save }); 
    // this.moduleArray.collect(_.save) (if module.notNil)
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

    init { |pageIn, stripIn, group, numModules|
        var allSlots;
        this.initControlArrays(3);
        pageIndex  = pageIn;
        stripIndex = stripIn;


        stripGroup = Group(group,\addToTail);
        allSlots   = Group(stripGroup,\addToTail);
        slotGroups = numModules.collect({ |i| Group(allSlots,\addToTail) });
        faderGroup = Group(stripGroup,\addToTail);
        
        slots      = numModules.collect({ |i| NS_ModuleSlot(this, i) });
        stripBus   = Bus.audio(group.server, NSFW.numChans(group.server));

        fader      = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);
        sends      = IdentityDictionary();
        
        controls[0] = NS_Control(\amp,\db)
        .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) });
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(30);

        controls[1] = NS_Control(\visible,ControlSpec(0,0,'lin',1))
        .addAction(\synth, { |c| this.toggleAllVisible }, false);
        assignButtons[1] = NS_AssignButton(this,1,\button).maxWidth_(30);

        controls[2] = NS_Control(\mute,ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| fader.set(\mute, c.value) }, false);
        assignButtons[2] = NS_AssignButton(this,2,\button).maxWidth_(30);

        view = NS_ChannelStripMatrixView(this)
    }
    
    saveExtra {}

    loadExtra {}
}

//NS_ChannelStripIn : NS_ChannelStripBase {}
