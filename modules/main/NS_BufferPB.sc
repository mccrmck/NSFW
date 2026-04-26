NS_BufferPB : NS_SynthModule{
    // add numBufs as variable for the do loops, etc.
    var buffers;

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_bufferPBmono" ++ numChans).asSymbol,
            {
                var bufnum = \bufnum.kr;
                var frames = BufFrames.kr(bufnum) - 1;
                var start  = \start.kr(0) * frames;
                var rate   = BufRateScale.kr(bufnum) * \rate.kr(1);
                var end    = (start + (\dur.kr(1) * frames)).clip(0, frames);
                var pos    = Phasor.ar(DC.ar(0) + \trig.tr, rate, start, end, start);
                var sig    = BufRd.ar(1, bufnum, pos);
                var gate   = pos > (end - (SampleRate.ir * 0.02 * rate));
                sig = sig * Env([1, 0, 1], [0.02, 0.02]).ar(0, gate + \trig.tr);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                buffers = Array.newClear(4);

                4.do { |index|
                    controlDict.add(
                        NS_ControlString("buffer" ++ index, "")
                        .addAction(\synth, { |c|
                            buffers[index].free;

                            // should get rid of this error:
                            // File '' could not be opened: System error : No such file or directory
                            if(c.value.size > 0,{
                                buffers[index] = Buffer.readChannel(
                                    nsServer.server, c.value, channels: [0], 
                                    action: { |buf|
                                        synths[0].set(\bufnum, buf, \trig, 1)
                                    }
                                );
                            })
                        }, false)
                    )
                };

                controlDict.addAll(
                    NS_ControlInt(\whichBuf, 0, 3,0)
                    .addAction(\synth, { |c| 
                        fork{
                            synths[0].set(\trig, 1);
                            0.02.wait;
                            buffers[c.value] !? { synths[0].set(\bufnum, buffers[c.value]) }
                        }
                    }),

                    NS_ControlFloat(\start, ControlSpec(0, 0.99), 0)
                    .addAction(\synth, { |c| synths[0].set(\start, c.value) }),

                    NS_ControlFloat(\remainDur, ControlSpec(0.01, 1, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\dur, c.value) }),

                    NS_ControlFloat(\rate, ControlSpec(0.25, 4, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value); 
                        // do we always restart the buffer?
                        synths[0].set(\trig, 1, \thru, c.value)
                    }),
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['start']),
            NS_ControlFader(controlDict['remainDur']),
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['mix']),
            HLayout( 
                NS_ControlSwitch(controlDict['whichBuf'], (0..3), 1).maxWidth_(15),
                VLayout( 
                    *4.collect({ |i|
                        NS_ControlSink(controlDict[("buffer" ++ i).asSymbol])
                    })
                )
            ),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    freeExtra { buffers.do(_.free) }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageSwitch(4, 4),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("BufferPB")
    }
}
