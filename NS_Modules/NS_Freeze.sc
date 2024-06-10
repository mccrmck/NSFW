NS_Freeze : NS_SynthModule {
  classvar <isSource = false;
  var trigGroup, synthGroup;
  var buffer, localResponder;
  var mixBus;

*initClass {
    StartUp.add{
      SynthDef(\ns_freezeTrig,{
        var sig = In.ar(\bus.kr,2);
        var trig = FluidOnsetSlice.ar(sig.sum * -3.dbamp,9,\thresh.kr(1));
        trig = Select.ar(\which.kr(0),[trig, Impulse.ar(\trigFreq.kr(0)), Dust.ar(\trigFreq.kr(0))]);
        trig = trig * (1 - \trigMute.kr(1));
        trig = trig + \trig.kr(0);

        SendTrig.ar(trig,0,1);

        ReplaceOut.ar(\bus.kr,sig)
      }).add;

      SynthDef(\ns_freeze,{
        var sig = In.ar(\bus.kr, 2).sum * -3.dbamp;

        sig = FFT(LocalBuf(1024),sig);
        sig = PV_Freeze(sig,1);

        sig = IFFT(sig);
        
        sig = sig * Env.asr(0.5,1,0.02).ar(2,\gate.kr(1));
        sig = sig * Env.asr(0,1,0).kr(1,\pauseGate.kr(1));

        sig = Pan2.ar(sig,Rand(-1.0,1.0),\amp.kr(1));
        
        XOut.ar(\bus.kr,\mix.kr(1), sig )
      }).add
    }
  }

  init {
    this.initModuleArrays(8);

    this.makeWindow("Freeze",Rect(30,600,240,360));

    trigGroup  = Group(modGroup);
    synthGroup = Group(trigGroup,\addAfter);

    synths = List.newClear(2);

    synths.put(0,Synth(\ns_freezeTrig,[\bus,bus],trigGroup));

    //buffers = [128,1024,4096].collect({ |i| Buffer.alloc(modGroup.server, i) ) });
  
    //mixBus = Bus.control(modGroup.server,1);

    localResponder.free;
    localResponder = OSCFunc({ |msg|
      if(synths[1].notNil,{ synths[1].set(\gate,0) });
      synths.put(1, Synth(\ns_freeze,[\bus,bus,\mix,mixBus.asMap],synthGroup) );

    },'/tr',argTemplate: [synths[0].nodeID]);


    controls.add(
      NS_Switch(win,["impulse","dust","onsets"],{ |switch| synths[0].set(\which,switch.value) })
    );
    assignButtons[0] = NS_AssignButton().maxWidth_(60).addAction(this,0,\switch);

    controls.add(
      NS_Switch(win,["128","1024","4096"],{ |switch|   /**********************/       })
    );
    assignButtons[1] = NS_AssignButton().maxWidth_(60).addAction(this,1,\switch);

    controls.add(
      NS_Fader(win,"trigFreq",ControlSpec(0,12,\lin),{ |f| synths[0].set(\trigFreq, f.value) },initVal:0).maxWidth_(60);
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(60).addAction(this,2,\fader);

    controls.add(
      NS_Fader(win,"thresh",ControlSpec(0,1,\amp),{ |f| synths[0].set(\thresh, f.value) },initVal:1).maxWidth_(60);
    );
    assignButtons[3] = NS_AssignButton().maxWidth_(60).addAction(this,3,\fader);

    controls.add(
      NS_Fader(win,"mix",ControlSpec(0,1,\amp),{ |f| mixBus.set(f.value) },initVal:1).maxWidth_(60)
    );
    assignButtons[4] = NS_AssignButton().maxWidth_(60).addAction(this,4,\fader);

    controls.add(
      Button()
      .minHeight_(45)
      .maxWidth_(60)
      .states_([["mute\ntrig"],["▶",Color.white,Color.black]])
    );
    assignButtons[5] = NS_AssignButton().maxWidth_(60).addAction(this,5,\button);

    controls.add(
      Button()
      .minHeight_(45)
      .maxWidth_(60)
      .states_([["lock",Color.black],["unlock",Color.white,Color.black]])
    );
    assignButtons[6] = NS_AssignButton().maxWidth_(60).addAction(this,6,\button);

    controls.add(
      Button()
      .minHeight_(45)
      .maxWidth_(60)
      .states_([["trig"]])
    );
    assignButtons[7] = NS_AssignButton().maxWidth_(60).addAction(this,7,\button);


    win.view.layout_(
      HLayout(
        VLayout(
          controls[0],assignButtons[0],
          controls[1],assignButtons[1],
        ),
        GridLayout.rows(
          [ controls[2],      controls[3],      controls[4] ],
          [ assignButtons[2], assignButtons[3], assignButtons[4] ],
          [ controls[5],      controls[6],      controls[7] ],
          [ assignButtons[5], assignButtons[6], assignButtons[7] ]
        )
      )
    );

    controls[0].layout.spacing_(4);
    controls[0].buttonsMinHeight_(45);
    controls[1].layout.spacing_(4);
    controls[1].buttonsMinHeight_(45);

    win.layout.spacing_(4).margins_(4);
    win.view.maxWidth_(255).maxHeight_(350);
  }

  freeExtra {
    trigGroup.free;
    synthGroup.free;
    //buffers.do({ |b| b.free })
  }

  

  makeOSCFragment { |name| }

}
