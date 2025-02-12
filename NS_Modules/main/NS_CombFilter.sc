NS_CombFilter : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_combFilter,{
                var numChans = NSFW.numChans;
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
        this.initModuleArrays(4);
        this.makeWindow("Comb Filter", Rect(0,0,210,90));

        synths.add( Synth(\ns_combFilter,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\freq,ControlSpec(20,1200,\exp),250)
        .addAction(\synth,{ |c| synths[0].set(\delayTime, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\decay,ControlSpec(0.1,3,\exp),0.5)
        .addAction(\synth,{ |c| synths[0].set(\delayTime, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[2] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[3] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]).round_(1), assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]), assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]), assignButtons[2] ),
                HLayout( NS_ControlButton(controls[3],["▶","bypass"]), assignButtons[3] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(),
            OSC_Panel([ OSC_Fader(false), OSC_Button(height:"20%")], width: "15%")
        ], columns: 2,randCol: true).oscString("CombFilter")
    }
}
