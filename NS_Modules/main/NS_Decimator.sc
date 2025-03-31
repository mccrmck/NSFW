NS_Decimator : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_decimator,{
                var sig = In.ar(\bus.kr, numChans);

                sig = Decimator.ar(sig,\sRate.kr(48000),\bits.kr(10));
                sig = LeakDC.ar(sig);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(4);
        this.makeWindow("Decimator", Rect(0,0,240,90));

        synths.add( Synth(\ns_decimator, [\bus, bus], modGroup) );

        controls[0] = NS_Control(\sRate,ControlSpec(80, modGroup.server.sampleRate,\exp),modGroup.server.sampleRate)
        .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\bits,ControlSpec(1,10,\lin),10)
        .addAction(\synth,{ |c| synths[0].set(\bits, c.value) });
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
            OSC_XY(width: "85%"),
            OSC_Panel([OSC_Fader(false, false), OSC_Button(height: "20%")])
        ], columns: 2, randCol: true).oscString("Decimator")
    }
}
