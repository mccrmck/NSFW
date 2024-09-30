NS_Tanh : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_tanh,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);


                sig = BHiShelf.ar(sig,\preHiFreq.kr(8000),1,\preHidB.kr(0));
                sig = BLowShelf.ar(sig,\preLoFreq.kr(200),1,\preLodB.kr(0));
                sig = (sig * \gain.kr(1)).tanh;
                sig = BLowShelf.ar(sig,\postLoFreq.kr(200),1,\postLodB.kr(0));
                sig = BHiShelf.ar(sig,\postHiFreq.kr(8000),1,\postHidB.kr(0));

                sig = LeakDC.ar(sig);
                sig = sig * \trim.kr(1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(8);
        this.makeWindow("Tanh", Rect(0,0,330,390));

        synths.add( Synth(\ns_tanh,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("preLoFreq",ControlSpec(20,2000,\exp),"preLodB",\boostcut, { |xy|
                synths[0].set(\preLoFreq, xy.x, \preLodB, xy.y)
            },[200,0] ).round_([1,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("preHiFreq",ControlSpec(2000,10000,\exp),"preHidB",\boostcut, { |xy|
                synths[0].set(\preHiFreq, xy.x, \preHidB, xy.y)
            },[8000,0] ).round_([1,0.1])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        controls.add(
            NS_XY("postLoFreq",ControlSpec(20,2000,\exp),"postLodB",\boostcut, { |xy|
                synths[0].set(\postLoFreq, xy.x, \postLodB, xy.y)
            },[200,0] ).round_([1,0.1])
        );
        assignButtons[2] = NS_AssignButton(this, 2, \xy);

        controls.add(
            NS_XY("postHiFreq",ControlSpec(2000,10000,\exp),"postHidB",\boostcut, { |xy|
                synths[0].set(\postHiFreq, xy.x, \postHidB, xy.y)
            },[8000,0] ).round_([1,0.1])
        );
        assignButtons[3] = NS_AssignButton(this, 3, \xy);

        controls.add(
            NS_Fader("gain",ControlSpec(0,32,\db),{ |f| synths[0].set(\gain,f.value.dbamp) },'horz',0)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("trim",\db,{ |f| synths[0].set(\trim,f.value.dbamp) },'horz',0)
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
                HLayout( 
                    VLayout( controls[0], assignButtons[0], controls[2], assignButtons[2] ),
                    VLayout( controls[1], assignButtons[1], controls[3], assignButtons[3] ),
                ),
                HLayout( controls[4], assignButtons[4] ),
                HLayout( controls[5], assignButtons[5] ),
                HLayout( controls[6], assignButtons[6], controls[7], assignButtons[7] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Panel(widgetArray: [
                OSC_XY(),
                OSC_XY()
            ]),
            OSC_Panel(widgetArray: [
                OSC_XY(),
                OSC_XY()
            ]),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width: "20%")
            ])
        ],randCol: true).oscString("Tanh")
    }
}
