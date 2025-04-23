NS_BufferPB : NS_SynthModule{
    classvar <isSource = true;
    var dragSinks;
    var currentBuffer, buffers, bufferPaths;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_bufferPBmono" ++ numChans).asSymbol,
            {
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum) - 1;
                var start    = \start.kr(0) * frames;
                var end      = start + ((frames - start) * \dur.kr(1));
                //var end      = \end.kr(1) * frames;
                var rate     = BufRateScale.kr(bufnum) * \rate.kr(1);
                var pos      = Phasor.ar(DC.ar(0) + \trig.tr, rate, start, end, start);
                var sig      = BufRd.ar(1,bufnum,pos);
                var gate     = pos > (end - (SampleRate.ir * 0.02 * rate));
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0, gate + \trig.tr);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        currentBuffer = 0;
        buffers = Array.newClear(4);
        bufferPaths = Array.newClear(4);

        controls[0] = NS_Control(\whichBuf, ControlSpec(0,3,\lin,1),0)
        .addAction(\synth, { |c| 
            currentBuffer = c.value;
            fork{
                synths[0].set(\trig,1);
                0.02.wait;
                synths[0].set(\bufnum, buffers[currentBuffer])
            }
        });

        controls[1] = NS_Control(\start, ControlSpec(0,0.99,\lin),0)
        .addAction(\synth, { |c| synths[0].set(\start, c.value) });

        controls[2] = NS_Control(\dur, ControlSpec(0.01,1,\exp),1)
        .addAction(\synth, { |c| synths[0].set(\dur, c.value) }); // old math: \end.kr = start + ((1-start) * dur)

        controls[3] = NS_Control(\rate, ControlSpec(0.25,4,\exp),1)
        .addAction(\synth, { |c| synths[0].set(\rate, c.value) });

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

        dragSinks = 4.collect({ |bufIndex|
            DragSink()
            .background_(Color.white)
            .align_(\center)
            .string_("drag file path here")
            .canReceiveDragHandler_({ View.currentDrag.isKindOf(String) })
            .receiveDragHandler_({ |sink|
                bufferPaths[bufIndex] = View.currentDrag;
                sink.object_(PathName(bufferPaths[bufIndex]).fileNameWithoutExtension);

                fork {
                    if(buffers[bufIndex].notNil,{ buffers[bufIndex].free });
                    buffers[bufIndex] = Buffer.readChannel(server, bufferPaths[bufIndex], channels:[0]);
                    server.sync;
                }
            })
        });

        this.makeWindow("BufferPB", Rect(0,0,230,240));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                HLayout( NS_ControlSwitch(controls[0], (0..3), 1).minWidth_(30), VLayout( *dragSinks ) ),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra { buffers.do(_.free) }

    saveExtra { |saveArray|
        var moduleArray = List.newClear(0);
        moduleArray.add( bufferPaths );
        saveArray.add( moduleArray );
        ^saveArray
    }

    loadExtra { |loadArray|
        var server  = modGroup.server;
        var cond    = CondVar();
        bufferPaths = loadArray[0];
        {
            bufferPaths.do({ |path,index|
                if(path.notNil,{
                    dragSinks[index].object_( PathName(path).fileNameWithoutExtension );
                    buffers[index] = Buffer.readChannel(server, path, channels:[0], action: { cond.signalOne });
                    cond.wait { buffers[index].numFrames != 0 };
                    server.sync;
                    "buffer: % loaded".format(buffers[index].bufnum).postln;
                    controls[0].value_(index)
                })
            })
        }.fork( AppClock )
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Switch(4, 4),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([
                OSC_Fader(false), 
                OSC_Button(width:"20%")
            ], columns: 2)
        ], randCol: true).oscString("BufferPB")
    }
}
