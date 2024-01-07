NS_RingMod : NS_Module {

  *initClass {
    StartUp.add{
      SynthDef(\needsAName,{
        var sig = In.ar(\inBus.kr, 1);
        sig = sig * SinOsc.ar(\modFreq.kr(0.5).lincurve(0,1,1,3500,4));
        Out.ar(\outBus.kr, sig!2 )
      }).add
    }
  }

  init {
    // fun stuff goes here
    // make gui controls (with unique IDs for every instance)
    // initate buffers, busses, what else?

    this.makeCtrlBus(1); // numChans argument

    synths = [
  //    Synth(\needsAName,[\inBus, inBus,\outBus,outBus],group).map(
  //      \modFreq,ctlBus.subBus(0)
  //    )
    ];
    
    this.routeInsAndOuts;

  }
}
