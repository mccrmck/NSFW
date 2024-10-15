NS_Gate : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_gate,{
                var numChans = NSFW.numChans;
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
        this.makeWindow("Gate", Rect(0,0,240,120));

        synths.add(Synth(\ns_gate,[\bus,bus],modGroup));

        controls.add(
            NS_Fader("thresh",\db,{ |f| synths[0].set(\thresh, f.value) },'horz',initVal:-36)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("atk",ControlSpec(0,0.01,\lin),{ |f| synths[0].set(\atk, f.value) },'horz',initVal:0)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("rls",ControlSpec(0,0.01,\lin),{ |f| synths[0].set(\rls, f.value) },'horz',initVal:0)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        win.layout_(
            VLayout(
               HLayout( controls[0], assignButtons[0] ),
               HLayout( controls[1], assignButtons[1] ),
               HLayout( controls[2], assignButtons[2] ),
               HLayout( controls[3], assignButtons[3] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Button()
        ],randCol:true).oscString("Gate")
    }
}
