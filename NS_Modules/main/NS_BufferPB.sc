NS_BufferPB : NS_SynthModule{
    classvar <isSource = true;
    var currentBuffer, buffers, bufferPaths;
    var startPosBus, durBus, rateBus, mixBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_bufferPBmono,{
                var numChans = NSFW.numChans;
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var start    = \start.kr(0) * frames;
                var end      = \end.kr(1) * frames;
                var rate     = BufRateScale.kr(bufnum) * \rate.kr(1);
                var pos      = Phasor.ar(DC.ar(0),rate,start,end);
                var sig      = BufRd.ar(1,bufnum,pos);
                var gate     = pos > (end - (SampleRate.ir * 0.02 * rate));
                
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0,gate + \trig.tr);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add
        }
    }

    init {
        this.initModuleArrays(9);
        this.makeWindow("BufferPB", Rect(0,0,270,330));
        synths = Array.newClear(1);

        currentBuffer = 0;
        buffers = Array.newClear(4);
        bufferPaths = Array.newClear(4);

        startPosBus = Bus.control(modGroup.server,1).set(0);
        durBus = Bus.control(modGroup.server,1).set(1);
        rateBus = Bus.control(modGroup.server,1).set(1);
        mixBus = Bus.control(modGroup.server,1).set(1);

        controls.add(
            NS_XY("startPos",ControlSpec(0,0.99,\lin),"dur",ControlSpec(1,0.01,\exp),{ |xy|
                startPosBus.set(xy.x);
                durBus.set(xy.x + ((1 - xy.x) * xy.y))
            },[0,1]).round_([0.01,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("rate",ControlSpec(0.25,4,\exp),{ |f| rateBus.set(f.value) },'horz',initVal:1)
        );
        assignButtons[1] = NS_AssignButton(this,1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| mixBus.set(f.value) },'horz',initVal:1)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                if(val == 0,{
                    synths[0].set(\gate,0);
                    synths[0] = nil;
                },{
                    synths.put(0, 
                        Synth(\ns_bufferPBmono,[
                            \bufnum, buffers[currentBuffer],
                            \start,  startPosBus.asMap,
                            \end,   durBus.asMap,
                            \rate,  rateBus.asMap,
                            \mix,   mixBus.asMap,
                            \bus,   bus
                        ],modGroup)
                    )
                });
                strip.inSynthGate_(val);
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        controls.add(
            NS_Switch((0..3),{ |switch|
                currentBuffer = switch.value;
                if(synths[0].notNil,{
                    fork{
                        synths[0].set(\trig,1);
                        0.02.wait;
                        synths[0].set(\bufnum,buffers[currentBuffer])
                    }
                })
            }).maxWidth_(45)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \switch).maxWidth_(45);

        4.do({ |bufIndex|
            controls.add(
                DragSink()
                .background_(Color.white)
                .align_(\center)
                .string_("drag file path here")
                .canReceiveDragHandler_({ View.currentDrag.isKindOf(String) })
                .receiveDragHandler_({ |sink|
                    bufferPaths[bufIndex] = View.currentDrag;
                    sink.object_(PathName(bufferPaths[bufIndex]).fileNameWithoutExtension);
                    sink.align_(\left)

                    fork {
                        if(buffers[bufIndex].notNil,{ buffers[bufIndex].free });
                        buffers[bufIndex] = Buffer.readChannel(modGroup.server,bufferPaths[bufIndex],channels:[0]);
                        modGroup.server.sync;
                    }
                })
            );
        });

        win.layout_(
            VLayout(
                controls[0], 
                assignButtons[0],
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout(
                    controls[4],
                    VLayout( controls[5], controls[6], controls[7], controls[8],),
                ),
                HLayout( assignButtons[4], controls[3], assignButtons[3] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        buffers.do(_.free);
        startPosBus.free; 
        durBus.free;
        rateBus.free; 
        mixBus.free;
    }

    saveExtra { |saveArray|
        var moduleArray = List.newClear(0);
        moduleArray.add( bufferPaths );
        saveArray.add( moduleArray );
        ^saveArray
    }

    loadExtra { |loadArray|
        bufferPaths = loadArray[0];
        bufferPaths.do({ |path,index|
            if(path.notNil,{
                {
                    controls[index + 5].object_(PathName(path).fileNameWithoutExtension);
                    buffers[index] = Buffer.readChannel(modGroup.server,path,channels:[0]);
                    modGroup.server.sync;

                    // free synth, add a new one

                }.fork(AppClock)
            })
        })
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_XY(height: "50%",snap:true),
            OSC_Switch(mode: 'slide',numPads:4),
            OSC_Fader(horizontal: true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("BufferPB")
    }
}
