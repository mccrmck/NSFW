NS_AmpMod : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_ampMod,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var freq = \freq.kr(4);
                var pulse = LFPulse.ar(freq, width: \width.kr(0.5), add: \offset.kr(0)).clip(0,1);
                pulse = Lag.ar(pulse,1/freq * \lag.kr(0).lag(0.01));
                sig = (sig * pulse);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("AmpMod", Rect(0,0,210,150));

        synths.add(Synth(\ns_ampMod,[\bus,bus],modGroup));

        controls[0] = NS_Control(\freq,ControlSpec(1,10000,\exp),4)
        .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\width,ControlSpec(0.01,0.99,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\width, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\lag,ControlSpec(0,1,\lin),0)
        .addAction(\synth,{ |c| synths[0].set(\lag, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\offset,ControlSpec(0,0.999,\lin),0)
        .addAction(\synth,{ |c| synths[0].set(\offset, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        win.layout_(
            VLayout(
               HLayout( NS_ControlFader(controls[0]).round_(1), assignButtons[0] ),
               HLayout( NS_ControlFader(controls[1])          , assignButtons[1] ),
               HLayout( NS_ControlFader(controls[2])          , assignButtons[2] ),
               HLayout( NS_ControlFader(controls[3])          , assignButtons[3] ),
               HLayout( NS_ControlFader(controls[4])          , assignButtons[4] ),
               HLayout( NS_ControlButton(controls[5],["▶","bypass"]), assignButtons[5] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(),
            OSC_XY(),
            OSC_Panel([OSC_Fader(false), OSC_Button(height:"20%")], width: "15%")
        ], columns: 3, randCol: true).oscString("AmpMod")
    }
}
