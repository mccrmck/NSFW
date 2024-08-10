NS_NHHall : NS_SynthModule {
  classvar <isSource = false;

  *initClass {
    ServerBoot.add{
      SynthDef(\ns_nhHall,{
          var numChans = NSFW.numOutChans;
        var sig = In.ar(\bus.kr,numChans);

        sig = NHHall.ar(sig, \verbTime.kr(1),\spread.kr(0.5),\loFreq.kr(120).lag(0.1),\loFreqMult.kr(0.5),\hiFreq.kr(10000).lag(0.1),\hiFreqMult.kr(0.5),\early.kr(0.5),\late.kr(0.5),\modRate.kr(0.1),\modDepth.kr(0.1));
        sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

        NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
      }).add;
    }
  }

  init {
    this.initModuleArrays(12);

    this.makeWindow("NHHall", Rect(0,0,480,210));

    synths.add( Synth(\ns_nhHall,[\bus,bus],modGroup));
   
    controls.add(
      NS_Fader("time",ControlSpec(0.01,10,\exp),{ |f| synths[0].set(\verbTime, f.value) },'vert',initVal: 1).round_(0.01)
    );
    assignButtons[0] = NS_AssignButton().maxWidth_(60).setAction(this, 0, \fader);

    controls.add(
      NS_Fader("spread",ControlSpec(0,1,\lin),{ |f| synths[0].set(\spread, f.value) },'vert',initVal: 0.5).round_(0.01)
    );
    assignButtons[1] = NS_AssignButton().maxWidth_(60).setAction(this, 1, \fader);

    controls.add(
      NS_Fader("loFreq",ControlSpec(20,20000,\exp),{ |f| synths[0].set(\loFreq, f.value) },'vert',initVal: 120).round_(1)
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this, 2, \fader);

    controls.add(
      NS_Fader("loMult",ControlSpec(0,1,\lin),{ |f| synths[0].set(\loFreqMult, f.value) },'vert',initVal: 0.5).round_(0.1)
    );
    assignButtons[3] = NS_AssignButton().maxWidth_(60).setAction(this, 3, \fader);

    controls.add(
      NS_Fader("hiFreq",ControlSpec(20,20000,\exp),{ |f| synths[0].set(\hiFreq, f.value) },'vert',initVal: 10000).round_(1)
    );
    assignButtons[4] = NS_AssignButton().maxWidth_(60).setAction(this, 4, \fader);

    controls.add(
      NS_Fader("hiMult",ControlSpec(0,1,\lin),{ |f| synths[0].set(\hiFreqMult, f.value) },'vert',initVal: 0.5).round_(0.1)
    );
    assignButtons[5] = NS_AssignButton().maxWidth_(60).setAction(this, 5, \fader);

    controls.add(
      NS_Fader("early",ControlSpec(0,1,\lin),{ |f| synths[0].set(\early, f.value) },'vert',initVal: 0.5).round_(0.01)
    );
    assignButtons[6] = NS_AssignButton().maxWidth_(60).setAction(this, 6, \fader);

    controls.add(
      NS_Fader("late",ControlSpec(0,1,\lin),{ |f| synths[0].set(\late, f.value) },'vert',initVal: 0.5).round_(0.01)
    );
    assignButtons[7] = NS_AssignButton().maxWidth_(60).setAction(this, 7, \fader); 

    controls.add(
      NS_Fader("mRate",ControlSpec(0,1,\lin),{ |f| synths[0].set(\modRate, f.value) },'vert',initVal: 0.1).round_(0.01)
    );
    assignButtons[8] = NS_AssignButton().maxWidth_(60).setAction(this, 8, \fader);

    controls.add(
      NS_Fader("depth",ControlSpec(0,1,\lin),{ |f| synths[0].set(\modDepth, f.value) },'vert',initVal: 0.1).round_(0.01)
    );
    assignButtons[9] = NS_AssignButton().maxWidth_(60).setAction(this, 9, \fader);

    controls.add(
      NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'vert',initVal:0)
    );
    assignButtons[10] = NS_AssignButton().maxWidth_(60).setAction(this, 10, \fader);

    controls.add(
      Button()
      .maxWidth_(60)
      .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
      .action_({ |but|
        var val = but.value;
        strip.inSynthGate_(val);
        synths[0].set(\thru, val)
      })
    );
    assignButtons[11] = NS_AssignButton().maxWidth_(60).setAction(this,11,\button);

    win.layout_(
      HLayout(
        VLayout( controls[0], assignButtons[0] ),
        VLayout( controls[1], assignButtons[1] ),
        VLayout( controls[2], assignButtons[2] ),
        VLayout( controls[3], assignButtons[3] ),
        VLayout( controls[4], assignButtons[4] ),
        VLayout( controls[5], assignButtons[5] ),
        VLayout( controls[6], assignButtons[6] ),
        VLayout( controls[7], assignButtons[7] ),
        VLayout( controls[8], assignButtons[8] ),
        VLayout( controls[9], assignButtons[9] ),
        VLayout( controls[10], assignButtons[10], controls[11], assignButtons[11] )
      )
    );

    win.layout.spacing_(4).margins_(4)
  }

  *oscFragment {       
        ^OSC_Panel(widgetArray: [
            OSC_MultiFader(width: "85%",numFaders:10),
             OSC_Panel(horizontal:false, widgetArray:[
                OSC_Fader(),
                OSC_Button(height: "20%" )
            ])
        ],randCol: true).oscString("NHHall")
    }
}
