NS_BufferPB : NS_SynthModule{
    var buffers;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(10);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_bufferPBmono" ++ numChans).asSymbol,
            {
                var bufnum = \bufnum.kr;
                var frames = BufFrames.kr(bufnum) - 1;
                var start  = \start.kr(0) * frames;
                var rate   = BufRateScale.kr(bufnum) * \rate.kr(1);
                var end    = (start + (\dur.kr(1) * frames * rate)).clip(0, frames);
                var pos    = Phasor.ar(DC.ar(0) + \trig.tr, rate, start, end, start);
                var sig    = BufRd.ar(1, bufnum, pos);
                var gate   = pos > (end - (SampleRate.ir * 0.02 * rate));
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0, gate + \trig.tr);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                buffers     = Array.newClear(4);

                controls[0] = NS_Control(\whichBuf, ControlSpec(0,3,\lin,1),0)
                .addAction(\synth, { |c| 
                    fork{
                        synths[0].set(\trig, 1);
                        0.02.wait;
                        synths[0].set(\bufnum, buffers[c.value])
                    }
                });

                4.do({ |ctlIndex|
                    controls[ctlIndex + 1] = NS_Control("buffer" ++ ctlIndex, \string, "")
                    .addAction(\synth,{ |c|
                        buffers[ctlIndex].free;
                        buffers[ctlIndex] = Buffer.readChannel(
                            server,
                            c.value,
                            channels: [0]
                        );
                    }, false)
                });

                controls[5] = NS_Control(\start, ControlSpec(0,0.99,\lin),0)
                .addAction(\synth, { |c| synths[0].set(\start, c.value) });

                controls[6] = NS_Control(\remainDur, ControlSpec(0.01,1,\exp),1)
                .addAction(\synth, { |c| synths[0].set(\dur, c.value) });

                controls[7] = NS_Control(\rate, ControlSpec(0.25,4,\exp),1)
                .addAction(\synth, { |c| synths[0].set(\rate, c.value) });

                controls[8] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[9] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value); 
                    // do we always restart the buffer?
                    synths[0].set(\trig, 1, \thru, c.value)
                });
                
                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {

        this.makeWindow("BufferPB", Rect(0,0,230,240));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[5]),
                NS_ControlFader(controls[6]),
                NS_ControlFader(controls[7]),
                NS_ControlFader(controls[8]),
                HLayout( 
                    NS_ControlSwitch(controls[0], (0..3), 1).fixedWidth_(30),
                    VLayout( 
                        *4.collect({ |i| NS_ControlSink(controls[i + 1]) })
                    )
                ),
                NS_ControlButton(controls[9], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra { buffers.do(_.free) }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Switch(4, 4),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([
                OSC_Fader(false), 
                OSC_Button(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("BufferPB")
    }
}
