NS_SamplePB : NS_SynthModule{
    classvar <isSource = true;
    var dragSink;
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
        this.initModuleArrays(4);
        this.makeWindow("SamplePB", Rect(0,0,210,240));

        rateBus  = Bus.control(modGroup.server,1).set(1);
        ampBus   = Bus.control(modGroup.server,1).set(1);
        bufArray = Array.newClear(16);

        controls[0] = NS_Control(\which,ControlSpec(0,15,\lin,1),0)
        .addAction(\synth,{ |c| 
            Synth(\ns_samplePBmono,[
                \bufnum, bufArray[ c.value ],
                \rate,rateBus.getSynchronous,
                \amp,ampBus.asMap,
                \bus,bus
            ],modGroup,\addToHead)
        });
        assignButtons[0] = NS_AssignButton(this, 0, \switch);

        controls[1] = NS_Control(\rate,ControlSpec(0.5,2,\exp),1)
        .addAction(\synth,{ |c| rateBus.set( c.value ) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\amp,\db,1)
        .addAction(\synth,{ |c| ampBus.set( c.value.dbamp ) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(30);

        dragSink = DragSink()
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
        });

        win.layout_(
            VLayout(
                NS_ControlSwitch(controls[0],""!16,4),
                assignButtons[0],
                dragSink,
                HLayout( NS_ControlFader(controls[1])                 , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]).round_(1)       , assignButtons[2] ),
                HLayout( NS_ControlButton(controls[3], ["▶","bypass"]), assignButtons[3] ),    
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
            dragSink.object_(PathName(bufferPath).folderName);

            {
                PathName(bufferPath).entries.wrapExtend(16).do({ |entry, index|
                    bufArray[index] = Buffer.readChannel(modGroup.server,entry.fullPath,channels: [0]);
                });
                modGroup.server.sync;
            }.fork(AppClock)
        })
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Switch(16, 4, 'tap', height: "50%"),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2),
        ],randCol:true).oscString("SamplePB")
    }
}
