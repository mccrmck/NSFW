NS_MonoSumSend : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_monoSumSend,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var sum = sig.sum * numChans.reciprocal;
                var coFreq = \coFreq.kr(80,0.1);

                sum = SelectX.ar(\which.kr(0),[sum, LPF.ar(LPF.ar(sum,coFreq),coFreq) ]);
                
                sum = NS_Envs(sum, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                Out.ar(\sendBus.kr,sum * \sendAmp.kr(0) * \mute.kr(0));
                ReplaceOut.ar(\bus.kr, sig)
            }).add
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("MonoSumSend", Rect(0,0,270,90));

        synths.add( Synth(\ns_subSend,[\bus,bus],modGroup) );

        controls.add(
            NS_Fader("lpf",ControlSpec(20,120,\exp),{ |f| synths[0].set(\coFreq,f.value)},'horz',initVal:80).round_(1)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("amp",ControlSpec(0,1,\lin),{ |f| synths[0].set(\sendAmp, f.value) },'horz',initVal:0)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\mute, val)
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["LPF",Color.black,Color.white],["noFilt",Color.white,Color.black]])
            .action_({ |but| 
                synths[0].set(\which, but.value)
            });
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        controls.add(
            PopUpMenu()
            .maxWidth_(45)
            .items_( (0..(NSFW.numOutBusses-1)) )
            .action_({ |m|
                synths[0].set(\sendBus, m.value)
            })
        );

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2], controls[3], assignButtons[3], StaticText().align_(\center).string_("out:").minWidth_(45), controls[4], ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false, widgetArray:[
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Button()
        ],randCol: true).oscString("MonoSumSend")
    }
}
