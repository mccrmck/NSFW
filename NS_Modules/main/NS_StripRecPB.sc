NS_StripRecPB : NS_SynthModule {
  classvar <isSource = true;
  var buffer;

  *initClass {
    StartUp.add{
      SynthDef(\ns_stripRec,{
        var sig = In.ar(\bus.kr, 2);
        var bufnum = \bufnum.kr;
        sig = RecordBuf.ar(sig,bufnum,0,1,\overdub.kr(0));

        NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
      }).add;

      SynthDef(\ns_stripPB,{
        var bufnum = \bufnum.kr;
        var frames = BufFrames.kr(bufnum);
        var trig = T2A.ar(\trig.tr);
        var rate = \rate.kr(1) * \direction.kr(1);
        var pos = Phasor.ar(trig,BufRateScale.kr(bufnum) * rate,\start.kr(0) * frames, \end.kr(1) * frames, \start.kr(0) * frames);
        var sig = BufRd.ar(2,bufnum,pos);
        
        sig = sig.tanh;
        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

        XOut.ar(\bus.kr,\mix.kr(1) * \thru.kr(0), sig )

      }).add
    }
  }

  init {
    this.initModuleArrays(6);

    this.makeWindow("StripPB", Rect(0,0,300,250));

    fork {
      buffer = Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 8, 2);
      modGroup.server.sync;
      synths.add( Synth(\ns_stripRec,[\bus,bus,\bufnum,buffer],strip.faderGroup,\addToHead) );
      0.1.wait;
      synths.add( Synth(\ns_stripPB,[\bus,bus,\bufnum,buffer],modGroup) );
    };

    controls.add(
      NS_XY("start",ControlSpec(0,1,'lin'),"end",ControlSpec(0,1,'lin'),{ |xy|
        var start = xy.x;
        var end = xy.y;

        if(start > end,{
          synths[1].set(\direction,-1) // is this right? or do start and end stay in their respective spots?
        },{
          synths[1].set(\direction,1)
        });

        synths[1].set(\start,start,\end,end)

      },[0,1]).round_([0.01,0.01])
    );
    assignButtons[0] = NS_AssignButton().setAction(this, 0, \xy);

    controls.add(
      NS_Fader("rate",ControlSpec(1/4,4,\exp),{ |f| synths[1].set(\rate, f.value) },initVal: 1).round_(0.001).maxWidth_(60)
    );
    assignButtons[1] = NS_AssignButton().maxWidth_(60).setAction(this, 1, \fader);

    controls.add(
      Button()
      .maxWidth_(60)
      .states_([["trig",Color.black,Color.white]])
      .action_({ |but|
        synths[1].set(\trig,1)
      })
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this,2,\button);

    controls.add(
      Button()
      .maxWidth_(60)
      .states_([["overdub",Color.black,Color.white],["overwrite",Color.white,Color.black]])
      .action_({ |but|
        synths[0].set(\overdub,but.value)
      })
    );
    assignButtons[3] = NS_AssignButton().maxWidth_(60).setAction(this,3,\button);

    controls.add(
      NS_Fader("mix",ControlSpec(0,1,\amp),{ |f| synths[1].set(\mix, f.value) },initVal:1).maxWidth_(60)
    );
    assignButtons[4] = NS_AssignButton().maxWidth_(60).setAction(this, 4, \fader);

    controls.add(
      Button()
      .maxWidth_(60)
      .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
      .action_({ |but|
        var val = but.value;
        strip.inSynthGate_(val);
        synths[1].set(\thru, val)
      })
    );
    assignButtons[5] = NS_AssignButton().maxWidth_(60).setAction(this,5,\button);

    win.layout_(
      HLayout(
        VLayout( controls[0], assignButtons[0] ),
        VLayout( controls[1], assignButtons[1], controls[2], assignButtons[2], controls[3], assignButtons[3] ),
        VLayout( controls[4], assignButtons[4], controls[5], assignButtons[5] )
      )
    );

    win.layout.spacing_(4).margins_(4)
  }

  makeOSCFragment { |name|
    OSC_ModuleFragment(true,[
      OSC_XY(snap:true),
      OSC_Fader("15%",snap:true),
      OSC_Fader("15%")
    ]).write(name)
  }

}
