NS_Squish : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_squish,{
                var numChans = NSFW.numOutChans;
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

        this.makeWindow("Squish", Rect(0,0,300,210));

        synths.add(Synth(\ns_squish,[\bus,bus],modGroup));

        controls.add(
            NS_Fader("thresh",\db,{ |f| synths[0].set(\thresh, f.value) },'horz',initVal:-12)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("ratio",ControlSpec(0,20),{ |f| synths[0].set(\ratio, f.value) },'horz',initVal:4)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("atk",ControlSpec(0.001,0.1,\lin),{ |f| synths[0].set(\atk, f.value) },'horz',initVal:0.01).round_(0.001)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("rls",ControlSpec(0.001,0.3,\lin),{ |f| synths[0].set(\rls, f.value) },'horz',initVal:0.1).round_(0.001)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("knee",ControlSpec(0,0.5,\lin),{ |f| synths[0].set(\knee, f.value) },'horz',initVal:0.1)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mUp",ControlSpec(0,20,\db),{ |f| synths[0].set(\muGain, f.value) },'horz',initVal:0)
        );
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)
        );
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(45);

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
        assignButtons[7] = NS_AssignButton(this, 7, \button).maxWidth_(45);

        win.layout_(
            VLayout(
               HLayout( controls[0], assignButtons[0] ),
               HLayout( controls[1], assignButtons[1] ),
               HLayout( controls[2], assignButtons[2] ),
               HLayout( controls[3], assignButtons[3] ),
               HLayout( controls[4], assignButtons[4] ),
               HLayout( controls[5], assignButtons[5] ),
               HLayout( controls[6], assignButtons[6], controls[7], assignButtons[7]),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width: "20%")
            ]),
        ],randCol:true).oscString("Squish")
    }
}
