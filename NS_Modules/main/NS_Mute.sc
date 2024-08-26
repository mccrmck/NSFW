NS_Mute : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_mute,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);

                var mute = Lag.kr((1 - \mute.kr(0)),\lag.kr(0.02));
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add
        }
    }

    init {
        this.initModuleArrays(2);
        this.makeWindow("Mute", Rect(0,0,200,60));
        strip.inSynthGate_(1);

        synths.add( Synth(\ns_mute,[\bus,bus],modGroup) );

        controls.add(
            NS_Fader("lag",ControlSpec(0.01,10,'exp'),{ |f| synths[0].set(\lag,f.value)},'horz',0.02 )
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["mute",Color.black,Color.white],["▶",Color.white,Color.black]])
            .action_({ |but|
                synths[0].set(\mute, but.value)
            })
        );
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        strip.inSynthGate_(0)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Button(),
            OSC_Fader(horizontal: true, height: "20%")
        ],randCol: true).oscString("Mute")
    }
}
