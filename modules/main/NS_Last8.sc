NS_Last8 : NS_SynthModule {
    var buffer, busses;

    buildSynthModule {

        synths = Array.newClear(2);

        busses = (
            mixBus:  Bus.control(nsServer.server, 1).set(1),
            rateBus: Bus.control(nsServer.server, 1).set(1),
            posBus:  Bus.control(nsServer.server, 1).set(0)
        );

        buffer = Buffer.alloc(nsServer.server, nsServer.options.sampleRate * 8, numChans);

        nsServer.addSynthDef(
            ("ns_last8Play" ++ numChans).asSymbol,
            {
                var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var trig     = \trig.tr;
                var rate     = BufRateScale.kr(bufnum) * \rate.kr(1);
                var pos      = Phasor.ar(
                    TDelay.ar(T2A.ar(trig), 0.02), rate, 0, 
                    frames, \startPos.kr(0) * frames
                );
                var duckTime = SampleRate.ir * 0.02 * rate;
                var duck     = pos > (frames - duckTime);

                sig = BufRd.ar(numChans, bufnum, pos);
                sig = sig * Env([1, 0, 1], [0.02, 0.02]).ar(0, duck + trig);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }
        );

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_last8Rec" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr,numChans);
                var rec = RecordBuf.ar(sig,\bufnum.kr, run: \rec.kr(1));
                NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
            },
            [\bus, modBus, \bufnum, buffer],
            { |synth| 
                synths.put(0, synth);

                controlDict.addAll(
                    NS_ControlFloat(\rate, ControlSpec(0.5, 2, \exp), 1)
                    .addAction(\synth, { |c| busses['rateBus'].set( c.value ) }),

                    NS_ControlFloat(\pos, ControlSpec(0, 1), 0)
                    .addAction(\synth, { |c| busses['posBus'].set( c.value ) }),

                    NS_ControlInt(\trig, 0, 1, 0)
                    .addAction(\synth, { |c| synths[1].set(\trig, c.value ) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| busses['mixBus'].set( c.value ) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        var val = c.value;
                        if(val == 0,) {
                            synths[1].set(\gate,0); // needs an if(pause) condition
                            synths[1] = nil
                        } {
                            synths[1] = Synth(("ns_last8Play" ++ numChans).asSymbol, [
                                \bufnum,   buffer,
                                \rate,     busses['rateBus'].asMap,
                                \startPos, busses['posBus'].asMap,
                                \mix,      busses['mixBus'].asMap,
                                \bus,      modBus 
                            ], modGroup, \addToTail)
                        };
                        this.gateBool_(val);
                        synths[0].set(\rec, 1 - val);
                    }),
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout( 
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['pos']),
            NS_ControlButton(controlDict['trig'], "trig" ! 2),
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
            OpenStageFader(),
            OpenStageFader(),
            OpenStageButton('push'),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("Last8")
    }
}
