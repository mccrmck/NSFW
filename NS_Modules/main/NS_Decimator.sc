NS_Decimator : NS_SynthModule {
  classvar <isSource = false;

  *initClass {
      ServerBoot.add{
          SynthDef(\ns_decimator,{
              var numChans = NSFW.numOutChans;
              var sig = In.ar(\bus.kr, numChans);

              sig = Decimator.ar(sig,\sRate.kr(48000),\bits.kr(10));
              sig = LeakDC.ar(sig);
              sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

              NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
          }).add
      }
  }

  init {
    this.initModuleArrays(3);
    strip.inSynthGate_(1);
    this.makeWindow("Decimator", Rect(0,0,240,210));

    synths.add( Synth(\ns_decimator,[\bus,bus],modGroup) );

    controls.add(
      NS_XY("sRate",ControlSpec(80, modGroup.server.sampleRate,\exp),"bits",ControlSpec(1,10,\lin),{ |xy| 
        synths[0].set(\sRate,xy.x, \bits, xy.y);
      },[modGroup.server.sampleRate,10]).round_([1,0.1])
    );
    assignButtons[0] = NS_AssignButton(this, 0, \xy);

    controls.add(
      NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)
    );
    assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(60);

    controls.add(
      Button()
      .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
      .action_({ |but|
        var val = but.value;
        synths[0].set(\thru, val)
      })
    );
    assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(60);

    win.layout_(
      VLayout(
        VLayout( controls[0], assignButtons[0] ),
        HLayout( controls[1], assignButtons[1] ),
        HLayout( controls[2], assignButtons[2] )
    )
);

win.layout.spacing_(4).margins_(4)
  }

  *oscFragment {       
      ^OSC_Panel(horizontal: false, widgetArray:[
          OSC_Button(),
          OSC_XY(height: "70%", snap:true),
          OSC_Fader(height: "15%", horizontal:true)
      ],randCol: true).oscString("Decimator")
  }
}
