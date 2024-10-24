NS_SumSplay : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_sumSplay,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                Out.ar(\sendBus.kr(),Splay.ar(sig,1, \sendAmp.kr(0)) * \mute.kr(0) );
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add
        }
    }

    init {
        this.initModuleArrays(3);
        this.makeWindow("SumSplay", Rect(0,0,195,60));
        
        synths.add( Synth(\ns_sumSplay,[\bus, bus],modGroup) );

        controls.add(
            PopUpMenu()
            .maxWidth_(45)
            .items_( (0..(NSFW.numOutBusses-1)) )
            .action_({ |m|
                synths[0].set(\sendBus, m.value)
            })
        );
        assignButtons[0] = NS_AssignButton(this, 0, \switch).maxWidth_(45);

        controls.add(
            NS_Fader("amp",\db,{ |f| synths[0].set(\sendAmp,f.value.dbamp)},'horz').round_(1)
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

        win.layout_(
            VLayout(
                HLayout(
                    controls[0], assignButtons[0],
                    controls[2], assignButtons[2]
                ),
                HLayout( controls[1], assignButtons[1] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false, widgetArray:[
            OSC_Fader(),
            OSC_Button(height:"20%")
        ],randCol: true).oscString("SumSplay")
    }
}
