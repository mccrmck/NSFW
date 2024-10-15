NS_ChannelStrip : NS_SynthModule {
    classvar numSlots = 5;
    var <>pageIndex, <>stripIndex;
    var <stripBus;
    var stripGroup, <inGroup, slots, <slotGroups, <faderGroup;
    var <inSink, <inSynth, <fader;
    var <inSynthGate = 0;
    var <moduleSinks, <view;
    var <>paused = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_stripIn,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                sig = sig * \thru.kr(0);
                ReplaceOut.ar(\outBus.kr,sig);
            }).add;

            SynthDef(\ns_stripFader,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0,0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));

                ReplaceOut.ar(\bus.kr, sig)
            }).add;

            SynthDef(\ns_stripSend,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                Out.ar(\outBus.kr,sig);
            }).add
        }
    }

    *new { |group, outBus, pgIndex, strIndex| 
        ^super.new(group, outBus).pageIndex_(pgIndex).stripIndex_(strIndex)
    }

    init {
        this.initModuleArrays(6);
        synths     = Array.newClear(4);

        stripBus   = Bus.audio(modGroup.server,NSFW.numChans);

        stripGroup = Group(modGroup,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        slots      = Group(stripGroup,\addToTail);
        slotGroups = numSlots.collect({ |i| Group(slots,\addToTail) });
        faderGroup = Group(stripGroup,\addToTail);

        inSynth = Synth(\ns_stripIn,[\inBus,stripBus,\outBus,stripBus],inGroup);
        fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);

        this.makeView;
    }

    makeView {
        inSink = NS_InModuleSink(this);
        moduleSinks = slotGroups.collect({ |slotGroup, slotIndex| 
            NS_ModuleSink(this, slotIndex)
        });

        controls.add(
            NS_Fader(nil,\amp,{ |f| fader.set(\amp, f.value) },'horz').maxHeight_(45)
        );
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                fader.set(\mute,but.value)
            })
        );
        assignButtons[1] = NS_AssignButton(this,1,\button);

        4.do({ |outChannel|
            controls.add(
                Button()
                .states_([[outChannel,Color.white,Color.black],[outChannel, Color.cyan, Color.black]])
                .action_({ |but|
                    var outSend = synths[outChannel];
                    if(but.value == 0,{
                        if(outSend.notNil,{ outSend.set(\gate,0) });
                        synths.put(outChannel, nil);
                    },{
                        var mixerBus = NS_ServerHub.servers[modGroup.server.name].outMixerBusses;
                        synths.put(outChannel, Synth(\ns_stripSend,[\inBus,stripBus,\outBus,mixerBus[outChannel]],faderGroup,\addToTail) )
                    })
                })
            )
        });

        view = View().layout_(
            VLayout(
                VLayout( *([inSink] ++ moduleSinks) ),
                HLayout( controls[0], assignButtons[0] ),
                HLayout( 
                    Button()
                    .states_([["S", Color.black, Color.yellow]])
                    .action_({ |but|
                        moduleSinks.do({ |sink| 
                            if( sink.module.notNil,{ sink.module.toggleVisible });
                        });
                        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.toggleVisible })
                    }),
                    controls[1],
                    assignButtons[1]
                ),
                HLayout( controls[2], controls[3], controls[4], controls[5] )
            )
        );

        controls[2].valueAction_(1);
        view.layout.spacing_(0).margins_(0);
    }

    asView { ^view }

    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }

    moduleStrings { ^this.moduleArray.collect({ |mod| mod.class.asString.split($_)[1] }) }

    free {
        inSink.free;
        synths.do(_.free);
        synths = Array.newClear(4);
        this.setInSynthGate(0);
        moduleSinks.do({ |sink| sink.free });
        this.amp_(0)
    }

    amp  { this.fader.get(\amp,{ |a| a.postln }) }
    amp_ { |amp| this.fader.set(\amp, amp) }

    toggleMute {
        this.fader.get(\mute,{ |muted|
            this.fader.set(\mute,1 - muted)
        })
    }

    setInSynthGate { |val| inSynthGate = val }

    inSynthGate_ { |val|
        inSynthGate = inSynthGate + val.linlin(0,1,-1,1);
        inSynthGate.postln;

        // these two lines need to be reassessed...
        inSynthGate = inSynthGate.max(0);
        inSynth.set( \thru, inSynthGate.sign )
    }

    saveExtra { |saveArray|
        var stripArray = List.newClear(0);
        var inSinkArray = if(inSink.module.notNil,{ inSink.save }); 
        var sinkArray = moduleSinks.collect({ |sink|
            if(sink.module.notNil,{
                sink.save
            })
        });
        stripArray.add( inSinkArray );
        stripArray.add( sinkArray );
        stripArray.add( inSynthGate );

        saveArray.add(stripArray);

        ^saveArray
    }

    loadExtra { |loadArray|
        var cond = CondVar();
        var count = 0;

        {
            if(loadArray[0].notNil,{ inSink.load( loadArray[0] ) });

            loadArray[1].do({ |sinkArray, index|
                if(sinkArray.notNil,{
                    moduleSinks[index].load(sinkArray, slotGroups[index])
                });
                count = count + 1
            });
            cond.wait( count == loadArray[1].size );

            this.setInSynthGate( loadArray[2] );
            inSynth.set( \thru, inSynthGate.sign )
        }.fork(AppClock)
    }

    pause {
        inSynth.set(\pauseGate, 0);
        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.pause });
        this.moduleArray.do({ |mod|
            if(mod.notNil,{ mod.pause })
        });
        fader.set(\pauseGate, 0);
        synths.do({ |snd|
            if(snd.notNil,{ snd.set(\pauseGate, 0) })
        });
        stripGroup.run(false);
        this.paused = true;
    }

    unpause {
        inSynth.set(\pauseGate, 1);
        inSynth.run(true);
        if(inSink.module.isInteger.not and: {inSink.module.notNil},{ inSink.module.unpause });
        this.moduleArray.do({ |mod| 
            if(mod.notNil,{ mod.unpause })
        });
        fader.set(\pauseGate, 1);
        fader.run(true);
        synths.do({ |snd|
            if(snd.notNil,{ snd.set(\pauseGate, 1); snd.run(true) })
        });
        stripGroup.run(true);
        this.paused = false;
    }
}

NS_OutChannelStrip : NS_SynthModule {
    classvar numSlots = 4;
    var <pageIndex = -1, <>stripIndex;
    var <stripBus;
    var stripGroup, <inGroup, slots, <slotGroups, <faderGroup;
    var <inSynth, <fader;
    var <inSynthGate = 0;
    var <moduleSinks, <view;
    var <label, <send;

    *new { |group, outIndex| 
        ^super.new(group, outIndex).stripIndex_(outIndex)
    }

    init {
        this.initModuleArrays(2);

        stripBus   = Bus.audio(modGroup.server,NSFW.numChans);

        stripGroup = Group(modGroup,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        slots      = Group(stripGroup,\addToTail);
        slotGroups = numSlots.collect({ |i| Group(slots,\addToTail) });
        faderGroup = Group(stripGroup,\addToTail);

        inSynth = Synth(\ns_stripIn,[\inBus,stripBus,\thru,1,\outBus,stripBus],inGroup);
        fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);
        synths.add( Synth(\ns_stripSend,[\inBus,stripBus,\outBus,0],faderGroup,\addToTail) );

        this.makeView;
    }

    makeView {

        moduleSinks = slotGroups.collect({ |slotGroup, slotIndex| 
            NS_ModuleSink(this, slotIndex)
        });

        label = StaticText().align_(\center).stringColor_(Color.white).string_("out: %".format(bus));

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                this.fader.set(\mute, but.value)
            })
        );
        assignButtons[0] = NS_AssignButton(this,0,\button).maxWidth_(45);

        controls.add(
            NS_Fader(nil,\db,{ |f| fader.set(\amp, f.value.dbamp) }).maxWidth_(45),
        );
        assignButtons[1] = NS_AssignButton(this,1,\fader).maxWidth_(45);

        view = View().layout_(
            VLayout(
                label,
                HLayout(
                    VLayout(
                        *(moduleSinks ++ [
                            HLayout(
                                PopUpMenu()
                                .items_( (0..((NSFW.numOutBusses - 1) - NSFW.numChans)) )
                                .value_(0)
                                .action_({ |menu|
                                    synths[0].set(\outBus, menu.value)
                                }),
                                Button()
                                .maxWidth_(45)
                                .states_([["S", Color.black, Color.yellow]])
                                .action_({ |but|
                                    moduleSinks.do({ |sink| 
                                        var mod = sink.module;
                                        if(mod.notNil,{ mod.toggleVisible });
                                    })
                                }),
                                controls[0],
                                assignButtons[0]
                            )]
                        )
                    ),
                    VLayout( controls[1], assignButtons[1] ),
                )
            ),
        );

        view.layout.spacing_(0).margins_(0);
    }

    asView { ^view }

    moduleArray { ^moduleSinks.collect({ |sink| sink.module }) }


    inSynthGate_ { |val| /* if this is not here, the language crashes... */ }

    free {
        moduleSinks.do({ |sink| sink.free });
        this.amp_(0)
    }

    amp  { this.fader.get(\amp,{ |a| a.postln }) }
    amp_ { |amp| this.fader.set(\amp, amp) }

    toggleMute {
        this.fader.get(\mute,{ |muted|
            this.fader.set(\mute,1 - muted)
        })
    }

    saveExtra { |saveArray|
        var sinkArray = moduleSinks.collect({ |sink|
            if(sink.module.notNil,{
                sink.save
            })
        });

        saveArray.add(sinkArray);

        ^saveArray
    }

    loadExtra { |loadArray|
        loadArray.do({ |sinkArray, index|
            if(sinkArray.notNil,{
                moduleSinks[index].load(sinkArray, slotGroups[index])
            })
        })
    }
}

NS_InChannelStrip : NS_SynthModule {   // this is not yet compatible when numServers > 1
    classvar numSlots = 3;
    var <stripBus, <eqBus;
    var localResponder;
    var stripGroup, <inGroup, <eqGroup, <faderGroup;
    var <inSynth, <fader;
    var <inSink, <rms, <eqWindow, <view;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_inputMono,{
                var sig = SoundIn.ar(\inBus.kr());

                var gateThresh = \gateThresh.kr(-72);
                var sliceDur = SampleRate.ir * 0.01;

                // hpf
                sig = HPF.ar(sig,\hpFreq.kr(40));

                // gate
                sig = sig * FluidAmpGate.ar(sig,10,10,gateThresh,gateThresh-5,sliceDur,sliceDur,sliceDur,sliceDur).lagud(0.01,0.1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),1);

                Out.ar(\outBus.kr, sig )
            }).add;

            SynthDef(\ns_inputStereo,{
                var inBus = \inBus.kr();
                var sig = SoundIn.ar([inBus,inBus + 1]).sum * -3.dbamp;

                var gateThresh = \gateThresh.kr(-72);
                var sliceDur = SampleRate.ir * 0.01;

                // hpf
                sig = HPF.ar(sig,\hpFreq.kr(40));

                // gate 
                sig = sig * FluidAmpGate.ar(sig,10,10,gateThresh,gateThresh-5,sliceDur,sliceDur,sliceDur,sliceDur).lag(0.01);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));

                Out.ar(\outBus.kr, sig )
            }).add;

            SynthDef(\ns_inFader,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr, 1) * \gain.kr(1);

                // compressor 
                var amp = Amplitude.ar(sig, \atk.kr(0.01), \rls.kr(0.1)).max(-100.dbamp).ampdb;
                amp = ((amp - \compThresh.kr(-12)).max(0) * (\ratio.kr(4).reciprocal - 1)).lag(\knee.kr(0.01)).dbamp;
                sig = sig * amp * \muGain.kr(0).dbamp;

                // mute
                sig = sig * (1 - \mute.kr(0,0.01));
                sig = ReplaceBadValues.ar(sig);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));
                SendPeakRMS.ar(sig,10,3,'/inSynth',0);

                Out.ar(\outBus.kr, sig ! numChans)
            }).add;

            SynthDef(\ns_bellEQ,{
                var sig = In.ar(\bus.kr,1);
                var gain = In.kr(\gain.kr(0),1);
                
                sig = MidEQ.ar(sig,\freq.kr(440),\rq.kr(1),gain);
                sig = NS_Envs(sig,\gate.kr(1),\pauseGate.kr(1),1);

                ReplaceOut.ar(\bus.kr,sig)
            }).add
        }
    }

    // this new, init, makeView function is fucking wack, please fix!
    *new { |group, inbusIndex| 
        ^super.new( group, inbusIndex )
    }

    init {
        this.initModuleArrays(15);
        synths     = Array.newClear(4);

        stripBus   = Bus.audio(modGroup.server,1);
        eqBus      = Bus.control(modGroup.server,30).setn(0!30);

        stripGroup = Group(modGroup,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        eqGroup    = Group(stripGroup,\addToTail);
        faderGroup = Group(stripGroup,\addToTail);

        inSynth    = Synth(\ns_inputMono,[\inBus,bus,\outBus,stripBus],inGroup);
        fader      = Synth(\ns_inFader,[\inBus,stripBus, \outBus,NS_ServerHub.servers[modGroup.server.name].inputBusses[bus]],faderGroup);

        localResponder.free;
        localResponder = OSCFunc({ |msg|

            if( msg[2] == 0,{
                { 
                    rms.value = msg[4].ampdb.linlin(-80, 0, 0, 1);
                    rms.peakLevel = msg[3].ampdb.linlin(-80, 0, 0, 1,\min)
                }.defer
            })
        }, '/inSynth', argTemplate: [fader.nodeID]);

        this.makeView
    }

    makeView {

        inSink = TextField()
        .maxWidth_(150)
        .align_(\center)
        .object_( bus.asString )
        .string_( bus.asString )
        .beginDragAction_({ bus.asInteger })
        .mouseDownAction_({ |v| v.beginDrag });

        controls.add(
            NS_Fader("hpf",ControlSpec(20,260),{ |f| inSynth.set(\hpFreq,f.value) },'horz',40)
            .round_(1).stringColor_(Color.white)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("gate",ControlSpec(-72,-32,\db),{ |f| inSynth.set(\gateThresh,f.value) },'horz',-72)
            .round_(1).stringColor_(Color.white)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("gain",\boostcut,{ |f| fader.set(\gain,f.value.dbamp) },'horz',0)
            .round_(0.1).stringColor_(Color.white)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add( 
            NS_Fader("comp",\db,{ |f| fader.set(\compThresh, f.value) },'horz',initVal: -12)
            .round_(0.1).stringColor_(Color.white)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \knob).maxWidth_(45);

        controls.add(
            NS_Knob("atk",ControlSpec(0.001,0.25),{ |k| fader.set(\atk, k.value) },false,0.01)
            .round_(0.01).maxWidth_(45).stringColor_(Color.white)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \knob).maxWidth_(45);

        controls.add(
            NS_Knob("rls",ControlSpec(0.001,0.25),{ |k| fader.set(\rls, k.value) },false,0.1)
            .round_(0.01).maxWidth_(45).stringColor_(Color.white)
        );
        assignButtons[5] = NS_AssignButton(this, 5, \knob).maxWidth_(45);

        controls.add(
            NS_Knob("ratio",ControlSpec(1,20,\lin),{ |k| fader.set(\ratio, k.value) },false,4)
            .round_(0.1).maxWidth_(45).stringColor_(Color.white)
        );
        assignButtons[6] = NS_AssignButton(this, 6, \knob).maxWidth_(45);

        controls.add(
            NS_Knob("knee",ControlSpec(0,1,\lin),{ |k| fader.set(\knee, k.value) },false,0.01)
            .round_(0.01).maxWidth_(45).stringColor_(Color.white)
        );
        assignButtons[7] = NS_AssignButton(this, 7, \knob).maxWidth_(45);

        controls.add(
            NS_Fader("trim",\boostcut,{ |k| fader.set(\muGain, k.value) },'horz')
            .round_(0.1).stringColor_(Color.white)
        );
        assignButtons[8] = NS_AssignButton(this, 8, \knob).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]])
            .action_({ |but|
                fader.set(\mute,but.value)
            })
        );
        assignButtons[9] = NS_AssignButton(this, 9, \button).maxWidth_(45);

        controls.add(
            NS_Fader("amp",\db,{ |f| fader.set(\amp, f.value.dbamp ) },'horz')
            .round_(0.1).stringColor_(Color.white),
        );
        assignButtons[10] = NS_AssignButton(this, 10, \fader).maxWidth_(45);

        4.do({ |outMixerChannel|
            controls.add(
                Button()
                .maxWidth_(45)
                .states_([[outMixerChannel,Color.white,Color.black],[outMixerChannel, Color.cyan, Color.black]])
                .action_({ |but|
                    var outSend = synths[outMixerChannel];
                    if(but.value == 0,{
                        if(outSend.notNil,{ outSend.set(\gate,0) });
                        synths.put(outMixerChannel, nil);
                    },{
                        var inputBus = NS_ServerHub.servers[modGroup.server.name].inputBusses[bus];
                        var mixerBus = NS_ServerHub.servers[modGroup.server.name].outMixerBusses[outMixerChannel];
                        synths.put(outMixerChannel, Synth(\ns_stripSend,[\inBus,inputBus,\outBus,mixerBus],faderGroup,\addToTail) )
                    })
                })            
            )
        });

        rms = LevelIndicator()
        .minWidth_(15).maxWidth_(20).style_(\led).numTicks_(11).numMajorTicks_(3)
        .stepWidth_(2).drawsPeak_(true).warning_(0.9).critical_(1.0);

        view = View()
        .background_( Color.white.alpha_(0.15) )
        .layout_(
            HLayout(
                VLayout(
                    HLayout(
                        inSink,
                        Button()
                        .maxWidth_(45)
                        .states_([["mono",Color.black, Color.white],["stereo", Color.white, Color.black]])
                        .action_({ |but| 
                            if( but.value == 0,{
                                inSynth.set(\gate,0);
                                inSynth = Synth(\ns_inputMono,[\inBus,bus,\outBus,stripBus],inGroup)
                            },{
                                inSynth.set(\gate,0);
                                inSynth = Synth(\ns_inputStereo,[\inBus,bus,\outBus,stripBus],inGroup)
                            })
                        })
                    ),
                    HLayout( controls[0], assignButtons[0] ),
                    HLayout( controls[1], assignButtons[1] ),
                    HLayout( controls[2], assignButtons[2] ),
                    View().background_(Color.black).minHeight_(4),
                    HLayout( controls[3], assignButtons[3] ),
                    HLayout( *controls[4..7] ),
                    HLayout( *assignButtons[4..7] ),
                    HLayout( controls[8], assignButtons[8] ),
                    View().background_(Color.black).minHeight_(4),
                    HLayout(  
                        Button()
                        .states_([["EQ", Color.green, Color.black],["EQ", Color.black, Color.green]])
                        .action_({ |but|
                            eqWindow.visible_(but.value.asBoolean)
                        }),
                        controls[9],
                        assignButtons[9]
                    ),
                    HLayout( controls[10], assignButtons[10] ),
                    HLayout( *controls[11..14] )
                ),
                rms
            )
        );

        this.makeEqWindow;
        view.layout.spacing_(2).margins_(2);
    }

    asView { ^view }

    makeEqWindow {
        var gradient = Color.rand;
        // calculate 1/3 octave bands & rqs
        var freqs   = 20 * 30.collect({ |i| 2 ** (i/3) });
        var twoToN  = 2 ** (1/3);
        var sqrt    = twoToN.sqrt;
        var lessOne = twoToN - 1;

        eqWindow = Window("EQ - inputBus " ++ bus.asString).userCanClose_(false);

        eqWindow.drawFunc = {
            Pen.addRect(eqWindow.view.bounds);
            Pen.fillAxialGradient(eqWindow.view.bounds.leftTop, eqWindow.view.bounds.rightBottom, Color.black, gradient);
        };

        eqWindow.layout_(
            GridLayout.rows(
                *freqs.collect({ |freq,index|
                    var label = freq.asInteger.asString;
                    
                    var band;
                    VLayout(
                        Button()
                        .minWidth_(30)
                        .states_([[label,Color.green,Color.black],[label,Color.red,Color.black]])
                        .action_({ |but|
                            if(but.value == 0,{
                                band.set(\gate,0)
                            },{
                                band = Synth(\ns_bellEQ,[\freq,freq,\gain,eqBus.subBus(index),\rq,lessOne/sqrt,\bus,stripBus],eqGroup,\addToTail)
                            })
                        }),
                        NS_Fader(nil,\db,{ |f| eqBus.subBus(index).set(f.value) },initVal:0).round_(1)
                    )
                }).clump(15)
            )
        );
    }

    free {

    }

    saveExtra { |saveArray|

        ^saveArray
    }

    loadExtra { |loadArray|

    }
}
