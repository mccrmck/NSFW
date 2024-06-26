NS_Decimator : NS_SynthModule {
  classvar <isSource = false;

  *initClass {
    StartUp.add{
      SynthDef(\ns_decimator,{
        var sig = In.ar(\bus.kr, 2);

        sig = Decimator.ar(sig,\sRate.kr(48000),\bits.kr(10));
        sig = LeakDC.ar(sig);
        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

        XOut.ar(\bus.kr,\mix.kr(1) * \thru.kr(0), sig )
      }).add
    }
  }

  init {
    this.initModuleArrays(3);

    this.makeWindow("Decimator", Rect(0,0,240,210));

    synths.add( Synth(\ns_decimator,[\bus,bus],modGroup) );

    controls.add(
      NS_XY("sRate",ControlSpec(80, modGroup.server.sampleRate,\exp),"bits",ControlSpec(1,10,\lin),{ |xy| 
        synths[0].set(\sRate,xy.x, \bits, xy.y);
      },[modGroup.server.sampleRate,10]).round_([1,0.1])
    );
    assignButtons[0] = NS_AssignButton().setAction(this, 0, \xy);

    controls.add(
      NS_Fader("mix",ControlSpec(0,1,\amp),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)
    );
    assignButtons[1] = NS_AssignButton().maxWidth_(60).setAction(this, 1, \fader);

    controls.add(
      Button()
      .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
      .action_({ |but|
        var val = but.value;
        strip.inSynthGate_(val);
        synths[0].set(\thru, val)
      })
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this,2,\button);

    win.layout_(
      VLayout(
        VLayout( controls[0], assignButtons[0] ),
        HLayout( controls[1], assignButtons[1] ),
        HLayout( controls[2], assignButtons[2] )
      )
    );

    win.layout.spacing_(4).margins_(4)
  }

  makeOSCFragment { |name|
    OSC_ModuleFragment(true,[

    ]).write(name)
  }
}
