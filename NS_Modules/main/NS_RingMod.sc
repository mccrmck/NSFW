NS_RingMod : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_ringMod,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);
                var freq = \freq.kr(40).lag(0.05);
                var modFreq = \modFreq.kr(40);
                var modGain = \modGain.kr(1);
                sig = sig * SinOsc.ar(freq + SinOsc.ar(modFreq,mul:modGain) );

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(4);

        this.makeWindow("RingMod", Rect(0,0,300,250));

        synths.add( Synth(\ns_ringMod,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("freq",ControlSpec(1,3500,\exp),"modFreq",ControlSpec(1,3500,\exp),{ |xy| 
                synths[0].set(\freq,xy.x, \modFreq, xy.y);
            },[40,4]).round_([1,1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("modGain",ControlSpec(0,3500,\amp),{ |f| synths[0].set(\modGain, f.value) }).round_(1).maxWidth_(60)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(60);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(60)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(60);

        controls.add(
            Button()
            .maxWidth_(60)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(60);

        win.layout_(
            HLayout(
                VLayout( controls[0], assignButtons[0] ),
                VLayout( controls[1], assignButtons[1] ),
                VLayout( controls[2], assignButtons[2], controls[3], assignButtons[3] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader("15%",snap:true),
            OSC_Panel("15%",horizontal:false,widgetArray: [
              OSC_Fader(),
              OSC_Button(height:"20%")
          ])
        ],randCol:true).oscString("RingMod")
    }
}
