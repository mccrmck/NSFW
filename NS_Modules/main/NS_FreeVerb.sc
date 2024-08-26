NS_FreeVerb : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_freeVerb,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);
                sig = HPF.ar(sig,80) + PinkNoise.ar(0.0001);
                sig = BLowShelf.ar(sig,\preLoFreq.kr(200),1,\preLodB.kr(0));
                sig = BHiShelf.ar(sig,\preHiFreq.kr(8000),1,\preHidB.kr(0));
                sig = FreeVerb.ar(sig,1,\room.kr(1),\damp.kr(0.9));
                sig = BLowShelf.ar(sig,\postLoFreq.kr(200),1,\postLodB.kr(0));
                sig = BHiShelf.ar(sig,\postHiFreq.kr(8000),1,\postHidB.kr(0));
                
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("FreeVerb", Rect(0,0,360,300));

        synths.add( Synth(\ns_freeVerb,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("preLoFreq",ControlSpec(20,2500,\exp),"preLodB",\boostcut, { |xy|
                synths[0].set(\preLoFreq, xy.x, \preLodB, xy.y)
            },[200,0] ).round_([1,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("preHiFreq",ControlSpec(2500,10000,\exp),"preHidB",\boostcut, { |xy|
                synths[0].set(\preHiFreq, xy.x, \preHidB, xy.y)
            },[8000,0] ).round_([1,0.1])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        controls.add(
            NS_XY("postLoFreq",ControlSpec(20,2500,\exp),"postLodB",\boostcut, { |xy|
                synths[0].set(\postLoFreq, xy.x, \postLodB, xy.y)
            },[200,0] ).round_([1,0.1])
        );
        assignButtons[2] = NS_AssignButton(this, 2, \xy);

        controls.add(
            NS_XY("postHiFreq",ControlSpec(2500,10000,\exp),"postHidB",\boostcut, { |xy|
                synths[0].set(\postHiFreq, xy.x, \postHidB, xy.y)
            },[8000,0] ).round_([1,0.1])
        );
        assignButtons[3] = NS_AssignButton(this, 3, \xy);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(45)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

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
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout( controls[0], assignButtons[0], controls[1], assignButtons[1] ),
                VLayout( controls[2], assignButtons[2], controls[3], assignButtons[3] ),
                VLayout( controls[4], assignButtons[4], controls[5], assignButtons[5] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Panel(widgetArray:[
                OSC_XY(),
                OSC_XY()
            ]),
            OSC_Panel(widgetArray:[
                OSC_XY(),
                OSC_XY()
            ]),
            OSC_Panel(height: "20%",widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width: "20%")
            ]),
        ],randCol:true).oscString("FreeVerb")
    }
}
