NS_SamplePB : NS_SynthModule{
    classvar <isSource = true;
    var rateBus, ampBus;
    var bufArray, bufferPath;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_samplePBmono,{
                var numChans = NSFW.numChans;
                var bufnum   = \bufnum.kr;
                var sig = PlayBuf.ar(1,bufnum,BufRateScale.kr(bufnum) * \rate.kr(1),doneAction:2);

                // should I add an envelop with BufDur? This is lazy...

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                sig = NS_Pan(sig,numChans,Rand(-0.8,0.8),numChans/4);

                // should I add a mix control here? 
                Out.ar(\bus.kr,sig);
                //NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )

            }).add
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("SamplePB", Rect(0,0,270,240));

        rateBus  = Bus.control(modGroup.server,1).set(1);
        ampBus   = Bus.control(modGroup.server,1).set(1);
        bufArray = Array.newClear(16);

        controls.add(
            NS_Switch((0..15),{ |switch| 
                var val = switch.value;
                Synth(\ns_samplePBmono,[
                    \bufnum, bufArray[val],
                    \rate,rateBus.getSynchronous,
                    \amp,ampBus.asMap,
                    \bus,bus
                ],modGroup,\addToHead)
            },4).buttonsMaxHeight_(50).maxHeight_(150)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \switch);

        controls.add(
            NS_Fader("rate",ControlSpec(0.5,2,\exp),{ |f| rateBus.set(f.value) },'horz',initVal:1)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("amp",\db,{ |f| ampBus.set(f.value.dbamp) },'horz',initVal:1)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        controls.add(
            DragSink()
            .background_(Color.white)
            .align_(\center)
            .string_("drag sample folder here")
            .canReceiveDragHandler_({ View.currentDrag.isKindOf(String) })
            .receiveDragHandler_({ |sink|
                bufferPath = View.currentDrag;
                sink.object_(PathName(bufferPath).folderName);
                bufArray.do(_.free);
                {
                    PathName(bufferPath).entries.wrapExtend(16).do({ |entry, index|
                        bufArray[index] = Buffer.readChannel(modGroup.server,entry.fullPath,channels: [0]);
                    });
                    modGroup.server.sync;
                }.fork(AppClock)
            })
        );

        win.layout_(
            VLayout(
                controls[0],
                assignButtons[0],
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3] ),
                controls[4]
            )
        );
        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        bufArray.do(_.free)
    }

    saveExtra { |saveArray|
        var moduleArray = List.newClear(0);
        moduleArray.add( bufferPath );
        saveArray.add( moduleArray );
        ^saveArray
    }

    loadExtra { |loadArray|
        if(loadArray[0].notNil,{
            bufferPath = loadArray[0];
            controls[19].object_(PathName(bufferPath).folderName);

            {
                PathName(bufferPath).entries.wrapExtend(16).do({ |entry, index|
                    bufArray[index] = Buffer.readChannel(modGroup.server,entry.fullPath,channels: [0]);
                });
                modGroup.server.sync;
            }.fork(AppClock)
        })
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 4 ),
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 4 ),
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 4 ),
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 4 ),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true)
        ],randCol:true).oscString("SamplePB")
    }
}
