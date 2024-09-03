NS_BufferPB : NS_SynthModule{
    classvar <isSource = true;
    var buffer, bufferPath;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_bufferPBmono,{
                var numChans = NSFW.numOutChans;
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var start    = \start.kr(0) * frames;
                var end      = \end.kr(1) * frames;
                var rate     = BufRateScale.kr(bufnum) * \rate.kr(1);
                var pos      = Phasor.ar(DC.ar(0),rate,start,end);
                var sig      = BufRd.ar(1,bufnum,pos);
                var gate     = pos > (end - (SampleRate.ir * 0.02 * rate));
                
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0,gate);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("BufferPB", Rect(0,0,300,250));
        
        controls.add(
            NS_XY("startPos",ControlSpec(0,0.99,\lin),"dur",ControlSpec(1,0.01,\exp),{ |xy|
                synths[0].set(\start, xy.x, \end, xy.x + ((1 - xy.x) * xy.y) )
            },[0,1]).round_([0.01,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
          DragSink()
          .background_(Color.white)
          .align_(\center)
          .string_("drag file path here")
          .canReceiveDragHandler_({ View.currentDrag.isKindOf(String) })
          .receiveDragHandler_({ |sink|
              bufferPath = View.currentDrag;
              sink.object_(PathName(bufferPath).fileNameWithoutExtension);

              fork {
                  if(buffer.notNil,{ buffer.free });
                  buffer = Buffer.readChannel(modGroup.server,bufferPath,channels:[0]);
                  modGroup.server.sync;
                  if(synths[0].notNil,{
                      synths[0].set(\bufnum,buffer)
                  },{
                      synths.add( Synth(\ns_bufferPBmono,[\bus,bus,\bufnum,buffer],modGroup) );
                  });
              }
          })
      );

      controls.add(
            NS_Fader("rate",ControlSpec(0.25,4,\exp),{ |f| synths[0].set(\rate, f.value) },initVal:1).maxWidth_(45)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(45)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })
        );
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout( controls[0], assignButtons[0], controls[1] ),
                VLayout( controls[2], assignButtons[2] ),
                VLayout( controls[3], assignButtons[3], controls[4], assignButtons[4] )
            )
        );
        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        if(buffer.notNil,{ buffer.free })
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
            controls[1].object_(PathName(bufferPath).fileNameWithoutExtension);

            {
                if(buffer.notNil,{ buffer.free }); // get rid of this line?
                buffer = Buffer.readChannel(modGroup.server,bufferPath,channels:[0]);
                modGroup.server.sync;
                synths.add( Synth(\ns_bufferPBmono,[\bus,bus,\bufnum,buffer, \thru, controls[4].value ],modGroup) )
            }.fork(AppClock)
        })
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader("15%"),
            OSC_Panel("15%",horizontal:false,widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
          ])
        ],randCol:true).oscString("BufferPB")
    }
}
