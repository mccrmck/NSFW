NS_VarDelay : NS_SynthModule {
  classvar <isSource = true;
  var buffer;

  *initClass {
      ServerBoot.add{
          SynthDef(\ns_varDelay,{
              var numChans = NSFW.numOutChans;
              var sig = In.ar(\bus.kr,numChans);
              var buffer = \buffer.kr(0 ! numChans);
              var clip = \clip.kr(1);

              var tap = DelTapWr.ar(buffer,sig + LocalIn.ar(numChans));

              sig = DelTapRd.ar(buffer,tap,\dTime.kr(0.2,0.05) + SinOsc.ar(\sinFreq.kr(0.05) * ({ 0.9.rrand(1) } ! numChans)).range(-0.02,0),2); 
              sig = Clip.ar(sig,clip.neg,clip);

              LocalOut.ar(sig.rotate(1) * \feedB.kr(0.95));

              sig = LeakDC.ar(sig);
              sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

              NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
          }).add
      }
  }

  init {
    this.initModuleArrays(5);

    this.makeWindow("VarDelay",Rect(0,0,320,240));

    buffer = { Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 1) } ! NSFW.numOutChans;
    synths.add( Synth(\ns_varDelay,[\buffer, buffer, \bus, bus],modGroup));

    controls.add(
      NS_XY("dTime",ControlSpec(0.01,1,\lin),"clip",ControlSpec(0.01,1,\lin),{ |xy| 
        synths[0].set(\dTime,xy.x, \clip, xy.y);
      },[0.2,1]).round_([0.01,0.01])
    );
    assignButtons[0] = NS_AssignButton().setAction(this, 0, \xy);

    controls.add(
      NS_Fader("sinHz",ControlSpec(0.01,40,\exp),{ |f| synths[0].set(\sinFreq, f.value) },initVal:0.05).maxWidth_(45)
    );
    assignButtons[1] = NS_AssignButton().maxWidth_(45).setAction(this, 1, \fader);

    controls.add(
      NS_Fader("feedB",ControlSpec(0.8,1.05,\amp),{ |f| synths[0].set(\feedB, f.value) },initVal:0.95).maxWidth_(45)
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(45).setAction(this, 2, \fader);

    controls.add(
      NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:0).maxWidth_(45)
    );
    assignButtons[3] = NS_AssignButton().maxWidth_(45).setAction(this, 3, \fader);

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
    assignButtons[4] = NS_AssignButton().maxWidth_(45).setAction(this,4,\button);

    win.layout_(
      HLayout(
        VLayout( controls[0], assignButtons[0] ),
        VLayout( controls[1], assignButtons[1] ),
        VLayout( controls[2], assignButtons[2] ),
        VLayout( controls[3], assignButtons[3], controls[4], assignButtons[4] ),
      )
    );

    win.layout.spacing_(4).margins_(4);
  }

  freeExtra {
    buffer.free; 
  }

  *oscFragment {       
      ^OSC_Panel(horizontal:false, widgetArray:[
          OSC_XY(height: "50%",snap:true),
          OSC_Fader(horizontal:true, snap:true),
          OSC_Fader(horizontal:true, snap:true),
          OSC_Panel(widgetArray: [
              OSC_Fader(horizontal:true),
              OSC_Button(width:"20%")
          ])
      ],randCol: true).oscString("VarDelay")
  }
}
