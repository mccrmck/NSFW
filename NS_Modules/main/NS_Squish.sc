NS_Squish : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);
                
            SynthDef(\ns_squish,{
                var sig = In.ar(\bus.kr, numChans);
                var amp = Amplitude.ar(sig, \atk.kr(0.01), \rls.kr(0.1)).max(-100.dbamp).ampdb;
                amp = ((amp - \thresh.kr(-12)).max(0) * (\ratio.kr(4).reciprocal - 1)).lag(\knee.kr(0)).dbamp;

                sig = sig * amp * \muGain.kr(0).dbamp;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            }).add
        }
    }

    init {
        this.initModuleArrays(8);
        this.makeWindow("Squish", Rect(0,0,240,210));

        synths.add(Synth(\ns_squish,[\bus,bus],modGroup));

        controls[0] = NS_Control(\thresh,\db,-12)
        .addAction(\synth,{ |c| synths[0].set(\thresh, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\ratio,ControlSpec(1,20,\lin),4)
        .addAction(\synth,{ |c| synths[0].set(\ratio, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \knob);

        controls[2] = NS_Control(\atk,ControlSpec(0.001,0.1,\lin),0.001)
        .addAction(\synth,{ |c| synths[0].set(\atk, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \knob);

        controls[3] = NS_Control(\rls,ControlSpec(0.001,0.3,\lin),0.001)
        .addAction(\synth,{ |c| synths[0].set(\rls, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \knob);

        controls[4] = NS_Control(\knee,ControlSpec(0,0.5,\lin),0.1)
        .addAction(\synth,{ |c| synths[0].set(\knee, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \knob);

        controls[5] = NS_Control(\mUp,ControlSpec(0,20,\db),0)
        .addAction(\synth,{ |c| synths[0].set(\muGain, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(30);

        controls[6] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(30);

        controls[7] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| /*strip.inSynthGate_(c.value);*/ synths[0].set(\thru, c.value) });
        assignButtons[7] = NS_AssignButton(this, 7, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0], 0.1),             assignButtons[0] ),
                HLayout( *4.collect{ |i| NS_ControlKnob( controls[i+1] ) } ),
                HLayout( *4.collect{ |i| assignButtons[i+1] } ),
                HLayout( NS_ControlFader(controls[5], 0.1),             assignButtons[5] ),
                HLayout( NS_ControlFader(controls[6]),                  assignButtons[6] ),
                HLayout( NS_ControlButton(controls[7], ["▶","bypass"]), assignButtons[7] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Panel({OSC_Knob()}!4, columns: 4),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2),
        ], randCol: true).oscString("Squish")
    }
}
