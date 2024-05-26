NS_Input : NS_Module {
  classvar <isSource = true;
  var <rms, localResponder;

  *initClass {
    StartUp.add{
      SynthDef(\ns_monoInput,{
        var inBus = \inBus.kr;
        var sig = SoundIn.ar(inBus);

        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(0));

        SendPeakRMS.ar(sig,10,3,'/inAmp',inBus);

        Out.ar(\outBus.kr, sig!2 )
      }).add;

      SynthDef(\ns_stereoInput,{
        var inBus = \inBus.kr;
        var sig = SoundIn.ar([inBus,inBus + 1]);

        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(0));

        SendPeakRMS.ar(sig.sum * -3.dbamp,10,3,'/inAmp',inBus);

        Out.ar(\outBus.kr, sig )
      }).add;
    }
  }

  *new { |group, bus, inChans = 0|
    ^super.newCopyArgs(group, bus).init(inChans.asArray)
  }

  init { |inChans|
   this.initModuleArrays(1);

    this.makeWindow("Input",Rect(250,250,75,240));

    switch(inChans.size,
      1, { synths.add( Synth(\ns_monoInput,[\inBus,inChans[0],\amp,0,\outBus,bus],modGroup) ) },
      2, { synths.add( Synth(\ns_stereoInput,[\inBus,inChans[0],\amp,0,\outBus,bus],modGroup) ) },
      { "only mono and stereo inputs implemented at the moment".error }
  );

  controls.add(
      NS_Fader(win,nil,\amp,{ |f| synths[0].set(\amp, f.value) }).round_(0.1)
  );
  assignButtons[0] = NS_AssignButton();

  rms = LevelIndicator().minWidth_(15).style_(\led).stepWidth_(2).drawsPeak_(true).warning_(0.9).critical_(1.0);

  localResponder.free;
  localResponder = OSCFunc({ |msg|
      if(msg[2] == inChans[0],{
          { 
              rms.value = msg[4].ampdb.linlin(-80, 0, 0, 1);
              rms.peakLevel = msg[3].ampdb.linlin(-80, 0, 0, 1,\min)
          }.defer
      });
  }, '/inAmp', argTemplate: [synths[0].nodeID]);

  win.layout_(
      VLayout(
          HLayout(
              controls[0],
              rms,
          ),
          assignButtons[0]
      )
  );

  win.layout.spacing_(4).margins_(4)

  }

  makeOSCFragment {}
}
