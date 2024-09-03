NS_CombFilter : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_combFilter,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);
                sig = CombC.ar(sig, 0.2, \delayTime.kr(250).reciprocal.lag,\decayTime.kr(0.5));
                sig = sig + PinkNoise.ar(0.0001);
                sig = LeakDC.ar(sig.tanh);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(3);
        this.makeWindow("Comb Filter", Rect(0,0,240,210));

        synths.add( Synth(\ns_combFilter,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("freq",ControlSpec(20,1200,\exp),"decay",ControlSpec(0.1,3,\exp),{ |xy| 
                synths[0].set(\delayTime,xy.x, \decayTime, xy.y);
            },[250,0.5]).round_([1,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(60);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
        strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(60);

        win.layout_(
            VLayout(
                VLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Panel("15%",horizontal:false,widgetArray: [
              OSC_Fader(),
              OSC_Button(height:"20%")
          ])
        ],randCol: true).oscString("CombFilter")
    }
}
