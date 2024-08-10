NS_AmpMod : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_ampMod,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);
                var freq = \freq.kr(4);
                var pulse = LFPulse.ar(freq,width: \width.kr(0.5) );
                sig = sig * LagUD.ar(pulse,\lagUp.kr(0.01),\lagDown.kr(0.01));

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            }).add
        }
    }

    init {
        this.initModuleArrays(4);

        this.makeWindow("AmpMod", Rect(0,0,360,250));

        synths.add(Synth(\ns_ampMod,[\bus,bus],modGroup));

        controls.add(
            NS_XY("freq",ControlSpec(1,3500,\exp),"width",ControlSpec(0.01,0.99,\lin),{ |xy| 
                synths[0].set(\freq,xy.x, \width, xy.y);
            },[4,0.5]).round_([1,0.01])
        );
        assignButtons[0] = NS_AssignButton().setAction(this, 0, \xy );

        controls.add(
            NS_XY("lagUp",ControlSpec(0.001,0.1,\exp),"lagDown",ControlSpec(0.001,0.1,\exp),{ |xy| 
                synths[0].set(\lagUp,xy.x, \lagDown, xy.y);
            },[0.01,0.01]).round_([0.001,0.001])
        );
        assignButtons[1] = NS_AssignButton().setAction(this, 1, \xy );

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(60)
        );
        assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this, 2, \fader );

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
        assignButtons[3] = NS_AssignButton().maxWidth_(60).setAction(this,3,\button);

        win.layout_(
            HLayout(
               VLayout( controls[0], assignButtons[0] ),
               VLayout( controls[1], assignButtons[1] ),
               VLayout( controls[2], assignButtons[2], controls[3], assignButtons[3]),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_XY(snap:true),
            OSC_Fader("15%")
        ],randCol:true).oscString("AmpMod")
    }
}
