NS_RingMod : NS_Module {

  *initClass {
    StartUp.add{
      SynthDef(\ns_RingMod,{
        var sig = In.ar(\inBus.kr, 1);
        var modFreq = \modFreq.kr(40);
        var modGain = \modGain.kr(1);
        sig = sig * SinOsc.ar(modFreq, mul: modGain );

        sig = sig * Env.asr(0.01,1,0.01).ar(2,\gate.kr(1));
        sig = sig * Env.asr(0,1,0).kr(1,\pauseGate.kr(1));
        sig = sig!2 * \amp.kr(1) * \mute.kr(1);

        XOut.ar(\outBus.kr,\mix.kr(1), sig )
      }).add
    }
  }

  init {
    var name = this.class.asString.split($_)[1];
    // fun stuff goes here
    // initate buffers, busses, what else?

    this.initArrays(2);

    this.makeWindow(name, Rect(700,700,250,250));

    controls.add(
      NS_XY(win,"modFreq",ControlSpec(4,3500,\exp),"modGain",ControlSpec(1,4,\amp),{ |xy| 
        synths[0].set(\modFreq,xy.x, \modModFreq, xy.y);
        xy.value.postln;
      },[40,1]).round_([1,0.01])
    );
    assignButtons[0] = NS_AssignButton();

    controls.add(
      NS_Fader(win,"mix",\amp,{ |f| synths[0].set(\amp, f.value) }).maxWidth_(60)
    );
    assignButtons[1] = NS_AssignButton().maxWidth_(60);

    win.layout_(
      HLayout(
        VLayout(
          controls[0],assignButtons[0]
        ),
        VLayout(
          controls[1],assignButtons[1]
        ),
      )
    );
   
    win.layout.spacing_(4).margins_(4!4)

    //this.routeInsAndOuts;

  }

  makeOSCFragment { |name|
    OSC_Fragment(true,[
      OSC_XY(snap:true),
      OSC_Fader("25%")
    ]).write(name)
  }
}
