NS_ScratchPB : NS_SynthModule {
    var busses, buffer;

    buildSynthModule {

        synths = Array.newClear(2);

        // if I put the args in In.kr, I can pass 'busses.asPairs' to the Synth instance
        busses = (
            freq:    Bus.control(nsServer.server, 1).set(4),
            mul:     Bus.control(nsServer.server, 1).set(0.5),
            modFreq: Bus.control(nsServer.server, 1).set(1),
            modMul:  Bus.control(nsServer.server, 1).set(1),
            mix:     Bus.control(nsServer.server, 1).set(1),
        );
        
        buffer = Buffer.alloc(nsServer, nsServer.options.sampleRate * 2, numChans);

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
                sig = HPF.ar(sig, 20).tanh;

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
            [\bus, modBus, \bufnum, buffer],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\freq, ControlSpec(0.1, 36, 1.5), 4)
                    .addAction(\synth, { |c| busses['freq'].set( c.value ) }),

                    NS_ControlFloat(\mul, ControlSpec(0.01, 1, \lin), 0.5)
                    .addAction(\synth, { |c| busses['mul'].set( c.value ) }),

                    NS_ControlFloat(\modFreq, ControlSpec(0.1, 10, \exp), 1)
                    .addAction(\synth, { |c| busses['modFreq'].set( c.value ) }),

                    NS_ControlFloat(\modMul, ControlSpec(1, 4), 1)
                    .addAction(\synth, { |c| busses['modMul'].set( c.value ) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| busses['mix'].set( c.value ) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        var val = c.value;
                        this.gateBool_(val);
                        synths[0].set(\rec, 1 - val);

                        if(val == 0) {
                            synths[1].set(\gate,0);
                            synths[1] = nil
                        } {
                            synths.put(1,
                                Synth(("ns_scratchPB" ++ numChans).asSymbol, [
                                    \bufnum,  buffer,
                                    \freq,    busses['freq'].asMap,
                                    \mul,     busses['mul'].asMap,
                                    \modFreq, busses['modFreq'].asMap,
                                    \modMul,  busses['modMul'].asMap,
                                    \mix,     busses['mix'].asMap,
                                    \bus,     modBus
                                ], modGroup, \addToTail)
                            )
                        }
                    }),
                );
        
                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['freq']),
            NS_ControlFader(controlDict['mul']),
            NS_ControlFader(controlDict['modFreq']),
            NS_ControlFader(controlDict['modMul']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
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
