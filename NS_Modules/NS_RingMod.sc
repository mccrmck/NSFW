NS_RingMod : NS_SynthModule {
  classvar <isSource = false;

  *initClass {
    StartUp.add{
      SynthDef(\ns_RingMod,{
        var sig = In.ar(\bus.kr, 2).sum * -3.dbamp;
        var freq = \freq.kr(40);
        var modFreq = \modFreq.kr(40);
        var modGain = \modGain.kr(1);
        sig = sig * SinOsc.ar(freq + SinOsc.ar(modFreq,mul:modGain) );

        sig = sig.tanh;
        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
        
        XOut.ar(\bus.kr,\mix.kr(1), sig!2 )
      }).add
    }
  }

  init {
    this.initModuleArrays(3);

    this.makeWindow("RingMod", Rect(700,700,300,250));

    synths.add(Synth(\ns_RingMod,[\bus,bus],modGroup));

    controls.add(
      NS_XY(win,"freq",ControlSpec(1,3500,\exp),"modFreq",ControlSpec(1,3500,\exp),{ |xy| 
        synths[0].set(\freq,xy.x, \modFreq, xy.y);
      },[40,4]).round_([1,1])
    );
    assignButtons[0] = NS_AssignButton().setAction(this, 0, \xy);

    controls.add(
      NS_Fader(win,"modGain",ControlSpec(0,3500,\amp),{ |f| synths[0].set(\modGain, f.value) }).round_(1).maxWidth_(60)
    );
    assignButtons[1] = NS_AssignButton().maxWidth_(60).setAction(this, 1, \fader);

    controls.add(
      NS_Fader(win,"mix",ControlSpec(0,1,\amp),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(60)
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this, 2, \fader);

    win.layout_(
      GridLayout.columns(
        [ controls[0], assignButtons[0] ],
        [ controls[1], assignButtons[1] ],
        [ controls[2], assignButtons[2] ],
      )
    );

    win.layout.spacing_(4).margins_(4)
  }

  makeOSCFragment { |name|
    OSC_Fragment(true,[
      OSC_XY(snap:true),
      OSC_Fader("25%",snap:true),
      OSC_Fader("25%")
    ]).write(name)
  }
}
