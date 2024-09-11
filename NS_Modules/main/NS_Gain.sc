NS_Gain : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_gain,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);
                sig = sig * \gain.kr(1);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(2);
        this.makeWindow("Gain", Rect(0,0,200,60));

        synths.add( Synth(\ns_gain,[\bus,bus],modGroup) );

        controls.add(
            NS_Fader("trim",\boostcut,{ |f| synths[0].set(\gain,f.value.dbamp)},'horz',0 )
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
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

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(),
            OSC_Button(height: "20%"),
        ],randCol: true).oscString("Gain")
    }
}
