NS_PitchShift : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_pitchShift,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);
                sig = PitchShift.ar(sig,0.05,\ratio.kr(1),\pitchDev.kr(0),0.05);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(3);
        this.makeWindow("PitchShift", Rect(0,0,240,210));

        synths.add( Synth(\ns_pitchShift,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("ratio",ControlSpec(0.25,2,\exp),"pitchDev",ControlSpec(0,0.5,\lin),{ |xy| 
                synths[0].set(\ratio,xy.x, \timeVar, xy.y);
            },[1,0]).round_([0.01,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(60);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
        strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(60);

        win.layout_(
            VLayout(
                VLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Panel("15%",horizontal:false,widgetArray: [
              OSC_Fader(),
              OSC_Button(height:"20%")
          ])
        ],randCol: true).oscString("PitchShift")
    }
}
