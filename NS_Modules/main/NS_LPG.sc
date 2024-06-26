NS_LPG : NS_SynthModule {
  classvar <isSource = false;

  *initClass {
    StartUp.add{
      SynthDef(\ns_lpg,{
        var sig = In.ar(\bus.kr, 2);
        var amp = Amplitude.ar(sig.sum * -3.dbamp * \gainOffset.kr(1),\atk.kr(0.1),\rls.kr(0.1));
        var rq = \rq.kr(0.707);

        sig = Select.ar(\which.kr(0),[
          BLowPass.ar(sig,amp.linexp(0,1,20,20000),rq),
          BLowPass.ar(sig,amp.linexp(0,1,20000,20),rq),
          BHiPass.ar(sig,amp.linexp(0,1,20,20000),rq),
          BHiPass.ar(sig,amp.linexp(0,1,20000,20),rq),
        ]);
        
        sig = LeakDC.ar(sig.tanh);
        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

        XOut.ar(\bus.kr,\mix.kr(1) * \thru.kr(0), sig )
      }).add
    }
  }

  init {
    this.initModuleArrays(6);

    this.makeWindow("LPG", Rect(0,0,300,250));

    synths.add( Synth(\ns_lpg,[\bus,bus],modGroup) );

    controls.add(
      NS_Fader("trim",ControlSpec(-9.dbamp,9.dbamp,\amp),{ |f| synths[0].set(\gainOffset,f.value) },'horz',initVal: 0.dbamp)
    );
    assignButtons[0] = NS_AssignButton().maxWidth_(60).setAction(this, 0, \fader);

    controls.add(
      NS_XY("atk",ControlSpec(0.001,0.1,\lin),"rls",ControlSpec(0.001,0.1,\lin),{ |xy| 
        synths[0].set(\atk,xy.x, \rls, xy.y);
      },[0.1,0.1]).round_([0.001,0.001])
    );
    assignButtons[1] = NS_AssignButton().setAction(this, 1, \xy);

    controls.add(
      NS_Switch(["LPG","ILPG","HPG","IHPG"],{ |switch| synths[0].set(\which,switch.value) },'horz')
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this, 2, \xy);

    controls.add(
      NS_Fader("rq",ControlSpec(0.01, 2.sqrt.reciprocal, \exp),{ |f| synths[0].set(\rq, f.value) },'horz',initVal: 0.707)
    );
    assignButtons[3] = NS_AssignButton().maxWidth_(60).setAction(this, 3, \fader);

    controls.add(
      NS_Fader("mix",ControlSpec(0,1,\amp),{ |f| synths[0].set(\mix, f.value) },initVal:1)
    );
    assignButtons[4] = NS_AssignButton().maxWidth_(60).setAction(this, 4, \fader);

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
    assignButtons[5] = NS_AssignButton().maxWidth_(60).setAction(this,5,\button);

    win.layout_(
      HLayout(
        //VLayout( controls[0], assignButtons[0] ),
        VLayout( 
          controls[1], assignButtons[1], 
          HLayout( controls[0], assignButtons[0] ),
          HLayout( controls[2], assignButtons[2] ),
          HLayout( controls[3], assignButtons[3] )
        ),
        VLayout( controls[4], assignButtons[4], controls[5], assignButtons[5] )
      )
    );

    win.layout.spacing_(4).margins_(4)
  }

  makeOSCFragment { |name|
    OSC_ModuleFragment(widgetArray:[
      OSC_Panel(horizontal:false,widgetArray:[
        OSC_XY(snap:true),
        OSC_Fader(height: "18%",horizontal:true),
        OSC_Switch(height: "18%",mode: 'slide'),
        OSC_Fader(height: "18%",horizontal:true),
      ]),
      OSC_Panel("15%",horizontal:false,widgetArray:[
        OSC_Fader(),
        OSC_Button(height:"25%")
      ])
    ]).write(name)

  }
}

