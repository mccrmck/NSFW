NS_Mute : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_mute,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);

                var lag = \lag.kr(0.02);
                var mute = Env.asr(lag,1,lag,\lin).ar(0,1 - \mute.kr(0));
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add
        }
    }

    init {
        this.initModuleArrays(2);
        this.makeWindow("Mute", Rect(0,0,210,60));
        strip.inSynthGate_(1);

        synths.add( Synth(\ns_mute,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\lag,ControlSpec(0.01,10,\exp),0.02)
        .addAction(\synth,{ |c| synths[0].set(\lag, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\mute, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| synths[0].set(\mute, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])               , assignButtons[0] ),
                HLayout( NS_ControlButton(controls[1], ["mute","▶"]), assignButtons[1] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        strip.inSynthGate_(0)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Button(),
            OSC_Fader(height: "20%")
        ],randCol: true).oscString("Mute")
    }
}
