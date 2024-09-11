NS_Test : NS_SynthModule {
    classvar <isSource = true;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_test,{
                var numChans = NSFW.numOutChans;
                var freq = LFDNoise3.kr(1).range(80,8000);
                var sig = Select.ar(\which.kr(0),[SinOsc.ar(freq,mul: AmpCompA.kr(freq,80)),PinkNoise.ar()]);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(3);
        this.makeWindow("Test", Rect(0,0,240,60));

        synths.add(Synth(\ns_test,[\bus,bus],modGroup));

        controls.add( 
            NS_Switch(["sine","pink"],{ |switch| synths[0].set(\which,switch.value ) },'horz').maxHeight_(30)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \switch).maxWidth_(45);

        controls.add(
            NS_Fader("amp",\amp,{ |f| synths[0].set(\amp, f.value) },'horz').maxHeight_(30)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

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
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout(
                    HLayout( controls[0], assignButtons[0] ),
                    HLayout( controls[1], assignButtons[1] ),
                ),
                VLayout( controls[2], assignButtons[2] )
            )
        );

        win.layout.spacing_(2).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Switch(numPads:2),
            OSC_Fader(horizontal:true),
            OSC_Button()
        ],randCol: true).oscString("Test")
    }
}
