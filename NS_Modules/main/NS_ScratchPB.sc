NS_ScratchPB : NS_SynthModule {
    var busses, buffer;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);

        synths = Array.newClear(2);

        // if I put the args in In.kr, I can pass 'busses.asPairs' to the Synth instance
        busses = (
            freq:    Bus.control(server, 1).set(4),
            mul:     Bus.control(server, 1).set(0.5),
            modFreq: Bus.control(server, 1).set(1),
            modMul:  Bus.control(server, 1).set(1),
            mix:     Bus.control(server, 1).set(1),
        );
        
        buffer = Buffer.alloc(server, server.sampleRate * 2, numChans);

        nsServer.addSynthDef(
            ("ns_scratchPB" ++ numChans).asSymbol,
            {
                var bufnum  = \bufnum.kr;
                var frames  = BufFrames.kr(bufnum) - 1;
                var modMul  = \modMul.kr(1);
                var freq    = \freq.kr(4) * 
                LFDNoise1.kr(\modFreq.kr(1)).linexp(-1, 1, modMul.reciprocal, modMul);
                var scratch = LFDNoise0.ar(freq, \mul.kr(0.5));
                var pos     = Phasor.ar(
                    DC.ar(0),
                    BufRateScale.kr(bufnum) * (scratch + 1) * scratch.sign,
                    0,
                    frames
                );

                var sig = BufRd.ar(numChans, bufnum, pos);
                sig = HPF.ar(sig,20).tanh;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }
        );

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_scratchPBRec" ++ numChans).asSymbol,
            {
                var sig    = In.ar(\bus.kr, numChans);
                var bufnum = \bufnum.kr;
                var pos    = Phasor.ar(DC.ar(0), \rec.kr(1), 0, BufFrames.kr(bufnum));
                var rec    = BufWr.ar(sig, bufnum, pos);
                NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
            },
            [\bus, strip.stripBus, \bufnum, buffer],
            { |synth|
                synths.add(synth);
        
                controls[0] = NS_Control(\freq,ControlSpec(0.1,36,1.5),4)
                .addAction(\synth,{ |c| busses['freq'].set( c.value ) });

                controls[1] = NS_Control(\mul,ControlSpec(0.01,1,\lin),0.5)
                .addAction(\synth,{ |c| busses['mul'].set( c.value ) });

                controls[2] = NS_Control(\modFreq,ControlSpec(0.1,10,\exp),1)
                .addAction(\synth,{ |c| busses['modFreq'].set( c.value ) });

                controls[3] = NS_Control(\modMul,ControlSpec(1,4,\lin),1)
                .addAction(\synth,{ |c| busses['modMul'].set( c.value ) });

                controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| busses['mix'].set( c.value ) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    var val = c.value;
                    this.gateBool_(val);
                    synths[0].set(\rec, 1 - val);

                    if(val == 0,{
                        synths[1].set(\gate,0);
                        synths[1] = nil
                    },{
                        synths.put(1,
                            Synth(("ns_scratchPB" ++ numChans).asSymbol,[
                                \bufnum,  buffer,
                                \freq,    busses['freq'].asMap,
                                \mul,     busses['mul'].asMap,
                                \modFreq, busses['modFreq'].asMap,
                                \modMul,  busses['modMul'].asMap,
                                \mix,     busses['mix'].asMap,
                                \bus,     strip.stripBus
                            ], modGroup, \addToTail)
                        )
                    });
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("ScratchPB", Rect(0,0,240,150));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    freeExtra {
        buffer.free;
        busses.do(_.free)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height: "20%")
            ], width: "20%")
        ], columns: 3, randCol: true).oscString("ScratchPB")
    }
}
