NS_MultiChannelTest : NS_SynthModule {
    classvar <isSource = true;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_multiChannelTest,{
                var numChans = NSFW.numOutChans;
                var sig = PinkNoise.ar();
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                sig = NS_Pan(sig, numChans, LFSaw.kr(\rate.kr(0.5)),1);
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            }).add
        }
    }

    init {
        this.initModuleArrays(2);
        this.makeWindow("MultiChannelTest", Rect(0,0,210,60));

        synths.add(Synth(\ns_multiChannelTest,[\bus,bus],modGroup));

        controls.add(
            NS_Fader("rate",ControlSpec(0.2,2,'lin'),{ |f| synths[0].set(\rate, f.value) },'horz',0.5)
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
                HLayout( controls[1], assignButtons[1] )
            )
        );

        win.layout.spacing_(2).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Fader(horizontal: true),
            OSC_Button()
        ],randCol: true).oscString("MCTest")
    }
}
