NS_MultiChannelTest : NS_SynthModule {
    classvar <isSource = true;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_multiChannelTest,{
                var numChans = NSFW.numOutChans;
                var sig = PinkNoise.ar();
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                sig = NS_Pan(sig, numChans, LFSaw.kr(0.2),1);
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            }).add
        }
    }

    init {
        this.initModuleArrays(1);

        this.makeWindow("MultiChannelTest", Rect(0,0,60,60));

        synths.add(Synth(\ns_multiChannelTest,[\bus,bus],modGroup));
        bus.postln;

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
        assignButtons[0] = NS_AssignButton().maxWidth_(45).setAction(this,0,\button);

        win.layout_(
            VLayout( controls[0], assignButtons[0] )
        );

        win.layout.spacing_(2).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Button()
        ],randCol: true).oscString("MCTest")
    }
}
