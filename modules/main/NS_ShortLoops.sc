NS_ShortLoops : NS_SynthModule {
    var buffers, samps, phasorBus, phasorStart, phasorEnd;

    buildSynthModule {

        buffers     = Array.newClear(numChans);
        samps       = nsServer.options.sampleRate * 6;
        phasorBus   = Bus.control(nsServer.server, 1).set(0);
        phasorStart = 0;
        phasorEnd   = samps;

        numChans.do({ |index|
            buffers[index] = Buffer.alloc(modGroup.server, samps);
        });

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_shortLoops" ++ numChans).asSymbol,
            {
                var sig, in  = In.ar(\bus.kr, numChans);
                var bufnum   = \bufnum.kr(0 ! numChans);
                var frames   = BufFrames.kr(bufnum);

                var recHead  = Phasor.ar(DC.ar(0), \rec.kr(0), 0, frames);
                var rec      = numChans.collect { |i| 
                    BufWr.ar(in[i], bufnum[i], recHead)
                };

                var trigLoop = \tLoop.tr(1);
                var plyStart = \playStart.kr(0) + \deviation.kr(0 ! numChans);
                var plyEnd   = \playEnd.kr(48000) + \offset.kr(0);
                var rate     = \rate.kr(1);
                var plyHead  = Phasor.ar(
                    TDelay.ar(T2A.ar(trigLoop), 0.02),
                    rate,
                    plyStart, 
                    plyEnd, 
                    plyStart
                ).wrap(0, frames);

                var duckTime = SampleRate.ir * 0.02 * rate;
                var duck     = plyHead > (plyEnd.wrap(0, frames) - duckTime);
                duck         = duck + (plyHead > (frames - duckTime));

                Out.kr(\phasorBus.kr, A2K.kr(recHead));

                sig = numChans.collect { |i| 
                    BufRd.ar(1, bufnum[i], plyHead[i])
                } * \mute.kr(0);
                sig = sig * Env([1, 0, 1], [0.02, 0.02]).ar(0, duck + trigLoop);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0));
                sig = (in * \drySig.kr(0)) + sig;
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) ) 
            },
            [\bus, modBus, \bufnum, buffers, \phasorBus, phasorBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\rate, ControlSpec(0.25, 2, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

                    NS_ControlFloat(\dev, ControlSpec(0, 0.5, \lin), 0)
                    .addAction(\synth, { |c| 
                        var dev = { c.value.rand } ! numChans;
                        var delta = (phasorEnd - phasorStart).wrap(0, samps);
                        dev = delta * dev;
                        synths[0].set(\tLoop, 1, \deviation, dev) 
                    }),

                    NS_ControlInt(\recLoop, 0, 1,0)
                    .addAction(\synth, { |c| 
                        if(c.value == 1) {
                            synths[0].set(\rec, 1);
                            phasorStart = phasorBus.getSynchronous;
                        } {
                            var offset = 0;
                            phasorEnd = phasorBus.getSynchronous;
                            if((phasorEnd - phasorStart).isNegative) { offset = samps };
                            synths[0].set(
                                \rec,       0, 
                                \tLoop,     1, 
                                \playStart, phasorStart,
                                \playEnd,   phasorEnd,
                                \offset,    offset,
                                \mute,      1
                            )
                        }
                    }),

                    NS_ControlFloat(\amp, \db, -18)
                    .addAction(\synth,{ |c| synths[0].set(\amp, c.value.dbamp) }),

                    NS_ControlInt(\drySig, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\drySig, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    }),
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['dev']),
            NS_ControlButton(controlDict['recLoop'], ["rec", "loop"]),
            NS_ControlFader(controlDict['amp']),
            NS_ControlButton(controlDict['drySig'], ["unmute thru", "mute thru"]),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    freeExtra {
        buffers.do(_.free);
        phasorBus.free;
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageButton().mode_("push").height_("40%"),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false), 
                OpenStageButton().width_("20%")
            ]).columns_(2)
        ]).randCol.label_("ShortLoops")
    }
}
