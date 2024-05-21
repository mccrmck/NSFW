NS_RingMod : NS_Module {

  *initClass {
    StartUp.add{
      SynthDef(\ns_RingMod,{
        var sig = In.ar(\inBus.kr, 1);
        var modFreq = \modFreq.kr(0.5).lincurve(0,1,4,3500,4);
        var modModFreq = \modModFreq.kr(0.5).lincurve(0,1,0,10,4);
        sig = sig * SinOsc.ar(modFreq + SinOsc.ar(modModFreq).range(0,100));
        sig = sig * Env.asr(0.01,1,0.01).ar(2,\gate.kr(1));
        sig = sig * Env.asr(0,1,0).kr(1,\pauseGate.kr(1));

        Out.ar(\outBus.kr, sig!2 * \amp.kr(1) )
      }).add
    }
  }

  init {
    // fun stuff goes here
    // make gui controls (with unique IDs for every instance)
    // initate buffers, busses, what else?


    synths = [
      //    Synth(\needsAName,[\inBus, inBus,\outBus,outBus],group).map(
      //      \modFreq,ctlBus.subBus(0)
      //    )
    ];

    controls = [
      NS_XY("modFreq",ControlSpec(4,3500,\exp),"modMod",ControlSpec(0,10,4),{ |xy| synths[0].set(\modFreq,xy.x, \modModFreq, xy.y) }).round_([1,0.01]),
      NS_Fader("amp",\amp,{ |f| synths[0].set(\amp, f.value) })
    ];

    
    //this.routeInsAndOuts;

  }

  makeWindow { |name|
    win = Window(name.asString,Rect(700,700,250,250),false);
    win.drawFunc = {
      var start = [win.view.bounds.leftTop,win.view.bounds.rightTop].choose;
      var stop  = [win.view.bounds.leftBottom,win.view.bounds.rightBottom].choose;
      
      Pen.addRect(win.view.bounds);
      Pen.fillAxialGradient(start,stop, Color.rand, Color.rand);
    };
    win.layout_(
      HLayout( *controls )
    );
    win.front
  }

  makeOSCFragment { |name|
    OSC_Fragment(true,[
      OSC_XY(snap:true),
      OSC_Fader("25%")
    ]).write(name)
  }
}
