NS_AmpMod : NS_Module {
  classvar <isSource = false;

  *initClass {
    StartUp.add{
      SynthDef(\ns_AmpMod,{
        var sig = In.ar(\bus.kr, 2).sum * -3.dbamp;
        var freq = \freq.kr(4);
        var pulse = LFPulse.ar(freq,width: \width.kr(0.5) );
        sig = sig * LagUD.ar(pulse,\lagUp.kr(0.01),\lagDown.kr(0.01));

        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
        
        XOut.ar(\bus.kr,\mix.kr(1), sig!2 )
      }).add
    }
  }

  init {

    this.initModuleArrays(3);

    this.makeWindow("AmpMod", Rect(700,450,360,250));

    synths.add(Synth(\ns_AmpMod,[\bus,bus],modGroup));

    controls.add(
      NS_XY(win,"freq",ControlSpec(1,3500,\exp),"width",ControlSpec(0.01,0.99,\lin),{ |xy| 
        synths[0].set(\freq,xy.x, \width, xy.y);
      },[4,0.5]).round_([1,0.1])
    );
    assignButtons[0] = NS_AssignButton().setAction(this, 0, \xy );

    controls.add(
      NS_XY(win,"lagUp",ControlSpec(0.001,0.1,\exp),"lagDown",ControlSpec(0.001,0.1,\exp),{ |xy| 
        synths[0].set(\lagUp,xy.x, \lagDown, xy.y);
      },[0.01,0.01]).round_([0.01,0.01])
    );
    assignButtons[1] = NS_AssignButton().setAction(this, 1, \xy );

    controls.add(
      NS_Fader(win,"mix",ControlSpec(0,1,\amp),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(60)
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this, 2, \fader );

    win.layout_(
      GridLayout.columns(
        [controls[0],assignButtons[0]],
        [controls[1],assignButtons[1]],
        [controls[2],assignButtons[2]]
      )
    );

    win.layout.spacing_(4).margins_(4)
  }

  makeOSCFragment { |name|

  }

}
