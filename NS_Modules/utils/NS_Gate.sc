NS_Gate : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_gate,{
                var sig = In.ar(\bus.kr, numChans);
                var thresh = \thresh.kr(-36);
                var sliceDur = SampleRate.ir * 0.01;
                var gate = FluidAmpGate.ar(sig,10,10,thresh,thresh-5,sliceDur,sliceDur,sliceDur,sliceDur);

                gate = LagUD.ar(gate,\atk.kr(0.01),\rls.kr(0.01));
                sig = sig * gate;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            }).add
        }
    }

    init {
        this.initModuleArrays(4);
        this.makeWindow("Gate", Rect(0,0,255,90));

        synths.add(Synth(\ns_gate,[\bus,bus],modGroup));

        controls[0] = NS_Control(\thresh,\db.asSpec, -36)
        .addAction(\synth,{ |c| synths[0].set(\thresh, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\atk,ControlSpec(0,0.1,\lin),0.01)
        .addAction(\synth,{ |c| synths[0].set(\atk, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\rls,ControlSpec(0,0.1,\lin),0.1)
        .addAction(\synth,{ |c| synths[0].set(\rls, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls[3] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_( c.value ); synths[0].set(\thru, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]).round_(0.1)    , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]).round_(0.001)  , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]).round_(0.001)  , assignButtons[2] ),
                HLayout( NS_ControlButton(controls[3],["▶","bypass"]), assignButtons[3] )
            )
        );

        win.layout.spacing_(2).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Button()
        ], randCol: true).oscString("Gate")
    }
}
