NS_Benjolin : NS_SynthModule {
  classvar <isSource = true;

  /* SynthDef based on the work of Alejandro Olarte, who was inspired by Rob Hordijk's Benjolin */
  *initClass {
    StartUp.add{
      SynthDef(\ns_benjolin,{
        var sh0, sh1, sh2, sh3, sh4, sh5, sh6, sh7, sh8=1, sig;

        var sr = SampleDur.ir;
        var local = LocalIn.ar(2,0);
        var rungler = local[0];
        var buf = local[1];

        var loop = \loop.kr(0);
        var freq1 = \freq1.kr(250);
        var freq2 = \freq2.kr(4);
        var rungler1 = \rungler1.kr(0.5);
        var rungler2 = \rungler2.kr(0.5);

        var runglerFilt = \runglerFilt.kr(0.5);
        var filtFreq = \filtFreq.kr(40);
        var rq = \rq.kr(0.5);
        var gain = \gain.kr(1);
        var tri1 = LFTri.ar((rungler*rungler1)+freq1);
        var tri2 = LFTri.ar((rungler*rungler2)+freq2);
        var osc1 = PulseDPW.ar((rungler*rungler1)+freq1);
        var osc2 = PulseDPW.ar((rungler*rungler2)+freq2);

        var pwm = BinaryOpUGen('>', (tri1 + tri2),(0)); // pwm = tri1 > tri2;

        osc1 = ( (buf * loop) + (osc1 * (loop * -1 + 1)) );
        sh0 = BinaryOpUGen('>', osc1, 0.5);
        sh0 = BinaryOpUGen('==', (sh8 > sh0), (sh8 < sh0));
        sh0 = (sh0 * -1) + 1;

        sh1 = DelayN.ar(Latch.ar(sh0,osc2),0.01,sr);
        sh2 = DelayN.ar(Latch.ar(sh1,osc2),0.01,sr*2);
        sh3 = DelayN.ar(Latch.ar(sh2,osc2),0.01,sr*3);
        sh4 = DelayN.ar(Latch.ar(sh3,osc2),0.01,sr*4);
        sh5 = DelayN.ar(Latch.ar(sh4,osc2),0.01,sr*5);
        sh6 = DelayN.ar(Latch.ar(sh5,osc2),0.01,sr*6);
        sh7 = DelayN.ar(Latch.ar(sh6,osc2),0.01,sr*7);
        sh8 = DelayN.ar(Latch.ar(sh7,osc2),0.01,sr*8);

        //rungler = ((sh6/8)+(sh7/4)+(sh8/2)); //original circuit
        //rungler = ((sh5/16)+(sh6/8)+(sh7/4)+(sh8/2));

        rungler = ((sh1/2.pow(8)) + (sh2/2.pow(7)) + (sh3/2.pow(6)) + (sh4/2.pow(5)) + (sh5/2.pow(4)) + (sh6/2.pow(3)) + (sh7/2.pow(2)) + (sh8/2.pow(1)));

        buf     = rungler;
        rungler = (rungler * \scale.kr(1).linlin(0,1,0,127));
        rungler = rungler.midicps;

        LocalOut.ar([rungler,buf]);

        sig = SelectX.ar(\whichSig.kr(5), [ tri1, tri2, osc1, osc2, pwm, sh0 ]);
        
        sig = LeakDC.ar(sig);

        sig = SelectX.ar(\whichFilt.kr(0), [
          RLPF.ar(sig, (rungler*runglerFilt)+filtFreq, rq, gain),
          BMoog.ar(sig,(rungler*runglerFilt)+filtFreq, 1 - rq, 0, gain),
          RHPF.ar(sig, (rungler*runglerFilt)+filtFreq, rq, gain),
          SVF.ar( sig, (rungler*runglerFilt)+filtFreq, 1 - rq,1,0,0,0,0,gain),
          DFM1.ar(sig, (rungler*runglerFilt)+filtFreq, 1 - rq ,gain,1)
        ]);

        sig = sig.tanh * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

        NS_XOut( \bus.kr, sig!2, \mix.kr(1), \thru.kr(0) )
      }).add
    }
  }

  init {
    this.initModuleArrays(10);

    this.makeWindow("Benjolin",Rect(0,0,400,450));

    synths.add( Synth(\ns_benjolin,[\bus,bus],modGroup) );

    controls.add(
      NS_XY("freq1",ControlSpec(20,20000,\exp),"freq2",ControlSpec(0.1,14000,\exp),{ |xy| 
        synths[0].set(\freq1,xy.x, \freq2, xy.y);
      },[40,4]).round_([1,0.1])
    );
    assignButtons[0] = NS_AssignButton().setAction(this, 0, \xy);

    controls.add(
      NS_XY("filtFreq",ControlSpec(20,20000,\exp),"rq",ControlSpec(1,0.01,\exp),{ |xy| 
        synths[0].set(\filtFreq,xy.x, \rq, xy.y);
      },[250,0.5]).round_([1,0.01])
    );
    assignButtons[1] = NS_AssignButton().setAction(this, 1, \xy);

    controls.add(
      NS_XY("rungler1",ControlSpec(0,1,\lin),"rungler2",ControlSpec(0,1,\lin),{ |xy| 
        synths[0].set(\rungler1,xy.x, \rungler2, xy.y);
      },[0.5,0.5]).round_([0.01,0.01])
    );
    assignButtons[2] = NS_AssignButton().setAction(this, 2, \xy);

    controls.add(
      NS_XY("runglerFilt",ControlSpec(0,1,\lin),"gain",ControlSpec(0.dbamp,9.dbamp,\exp),{ |xy| 
        synths[0].set(\runglerFilt,xy.x, \gain, xy.y);
      },[0.5,0.dbamp]).round_([0.01,0.01])
    );
    assignButtons[3] = NS_AssignButton().setAction(this, 3, \xy);

    controls.add(
      NS_Switch([ "tri1", "tri2", "osc1", "osc2", "pwm", "sh0" ],{ |switch| 
        synths[0].set(\whichSig, switch.value)
      }).value_(5).maxWidth_(45)
    );
    assignButtons[4] = NS_AssignButton().maxWidth_(45).setAction(this, 4, \switch);

    controls.add(
      NS_Switch([ "rlpf", "moog", "rhpf", "svf", "dfm1" ],{ |switch| 
        synths[0].set(\whichFilt, switch.value)
      }).maxWidth_(45)
    );
    assignButtons[5] = NS_AssignButton().maxWidth_(45).setAction(this, 5, \switch);

    controls.add(
      NS_Fader("loop",ControlSpec(0,1,\lin),{ |f| synths[0].set(\loop, f.value) },initVal:0).maxWidth_(45)
    );
    assignButtons[6] = NS_AssignButton().maxWidth_(45).setAction(this, 6, \fader);

    controls.add(
      NS_Fader("scale",ControlSpec(0,1,\lin),{ |f| synths[0].set(\scale, f.value) },initVal:1).maxWidth_(45)
    );
    assignButtons[7] = NS_AssignButton().maxWidth_(45).setAction(this, 7, \fader);

    controls.add(
      NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(45)
    );
    assignButtons[8] = NS_AssignButton().maxWidth_(45).setAction(this, 8, \fader);

    controls.add(
      Button()
      .maxWidth_(45)
      .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
      .action_({ |but|
        var val = but.value;
        strip.inSynthGate_(val);
        synths[0].set(\thru, val)
      })
    );
    assignButtons[9] = NS_AssignButton().maxWidth_(45).setAction(this,9,\button);

    win.layout_(
      HLayout(
        VLayout( controls[0], assignButtons[0], controls[1], assignButtons[1] ),
        VLayout( controls[2], assignButtons[2], controls[3], assignButtons[3] ),
        VLayout( controls[5], assignButtons[5], controls[6], assignButtons[6], controls[7], assignButtons[7] ),
        VLayout( controls[4], assignButtons[4], controls[8], assignButtons[8], controls[9], assignButtons[9] ),
      )
    );

    win.layout.spacing_(4).margins_(4)
  }

  *oscFragment {
      ^OSC_Panel(horizontal: false, widgetArray:[
          OSC_Panel(height:"50%",widgetArray:[
              OSC_XY(snap:true),
              OSC_XY(snap:true),
              OSC_Switch(width: "15%", horizontal: false, mode: 'slide', numPads: 5),
              OSC_Switch(width: "15%", horizontal: false, mode: 'slide', numPads: 6),
          ]),
          OSC_Panel(height:"50%",widgetArray:[
              OSC_XY(snap:true),
              OSC_XY(snap:true),
              OSC_Fader(width: "15%",),
              OSC_Fader(width: "15%",),
          ])
      ],randCol:true).oscString("Benjolin")
  }
}
