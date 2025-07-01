NS_Freeze : NS_SynthModule {
    classvar <isSource = false;
    var trigGroup, synthGroup;
    var bufferArray, bufIndex, localResponder;
    var sendBus, mixBus;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);

        trigGroup  = Group(modGroup);
        synthGroup = Group(trigGroup, \addAfter);

        synths  = List.newClear(2);
        sendBus = Bus.audio(server, 1);
        mixBus  = Bus.control(server, 1).set(0.5);

        nsServer.addSynthDef(
            ("ns_freeze" ++ numChans).asSymbol,
            {
                var sig = In.ar(\inBus.kr, 1);

                sig = FFT(\bufnum.kr, sig);
                sig = PV_Freeze(sig, 1);
                sig = IFFT(sig);

                sig = sig * Env.asr(0.5,1,0.02).ar(2, \gate.kr(1) + Impulse.kr(0));
                sig = sig * Env.asr(0,1,0).kr(1, \pauseGate.kr(1));

                sig = NS_Pan(sig, numChans, Rand(-0.8,0.8), numChans/4);

                Out.ar(\outBus.kr, sig * \amp.kr(1) * \mix.kr(0.5) )
            }
        );

        nsServer.addSynthDefCreateSynth(
            trigGroup,
            ("ns_freezeTrig" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr,numChans);
                var sum = sig.sum * numChans.reciprocal.sqrt;
                var trig = FluidOnsetSlice.ar(sum, 9, \thresh.kr(1));
                var tFreq = \trigFreq.kr(0);
                trig = Select.ar(\which.kr(0),[trig, Impulse.ar(tFreq), Dust.ar(tFreq)]);
                trig = trig * \trigMute.kr(0);
                trig = trig + \trig.tr(0);

                SendTrig.ar(trig,0,1);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                Out.ar(\sendBus.kr, sum);

                ReplaceOut.ar(\bus.kr,sig * (1 - \mix.kr(0.5)) )
            },
            [\bus, strip.stripBus, \sendBus, sendBus, \mix, mixBus.asMap],
            { |synth| 
                synths.put(0, synth);

                // this should maybe be in the finishing action of \ns_freeze?
                // it relies on \ns_freezeTrig to have a nodeID, so maybe not...

                localResponder.free;
                localResponder = OSCFunc({ |msg|
                    if(synths[1].notNil,{ synths[1].set(\gate,0) });
                    synths.put(1, 
                        Synth(("ns_freeze" ++ numChans).asSymbol,[
                            \inBus,  sendBus,
                            \bufnum, bufferArray[bufIndex],
                            \mix,    mixBus.asMap,
                            \outBus, strip.stripBus
                        ], synthGroup) 
                    );

                }, '/tr', argTemplate: [synths[0].nodeID]);

                bufIndex = 0;
                bufferArray = [128,1024,2048].collect({ |frames| Buffer.alloc(server, frames) });

                controls[0] = NS_Control(\whichTrig, ControlSpec(0,2,\lin,1),0)
                .addAction(\synth,{ |c| synths[0].set(\which,c.value) });

                controls[1] = NS_Control(\fftSize, ControlSpec(0,2,\lin,1),0)
                .addAction(\synth,{ |c| bufIndex = c.value });

                controls[2] = NS_Control(\tFreq,ControlSpec(0,4,\lin),0)
                .addAction(\synth,{ |c| synths[0].set(\trigFreq, c.value) });

                controls[3] = NS_Control(\thresh,\db,0)
                .addAction(\synth,{ |c| synths[0].set(\thresh, c.value.dbamp) });

                controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),0.5)
                .addAction(\synth,{ |c| mixBus.set(c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,2,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    switch(c.value.asInteger,  // this is a problem!
                        0,{ 
                            synths[0].set(\trigMute,0); 
                            synths[1].set(\gate, 0); 
                            synths[1] = nil
                        },
                        1,{ 
                            synths[0].set(\trigMute,1)
                        },
                        2,{ 
                            synths[0].set(\trigMute,0);
                            synths[0].set(\trig, 1)
                        }
                    );
                    this.gateBool_(c.value > 0)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("Freeze",Rect(0,0,240,180));

        win.layout_(
            VLayout(
                NS_ControlSwitch(controls[0], ["onsets", "impulse", "dust"], 3),
                NS_ControlSwitch(controls[1], ["128", "1024", "2048"], 3),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3], 1),
                NS_ControlFader(controls[4]),
                NS_ControlSwitch(controls[5], ["free", "▶", "trig"], 3),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra {
        trigGroup.free;
        synthGroup.free;
        bufferArray.do(_.free);
        sendBus.free;
        mixBus.free;
        localResponder.free
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Panel([OSC_Switch(3, 3), OSC_Switch(3, 3)], columns: 2),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Switch(3, 3)
        ], randCol: true).oscString("Freeze")
    }
}
