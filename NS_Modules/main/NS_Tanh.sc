NS_Tanh : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_tanh,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);

                sig = BHiShelf.ar(sig,\hiFreq.kr(8000),1,\hiDb.kr(0));
                sig = BLowShelf.ar(sig,\loFreq.kr(8000),1,\loDb.kr(0));
                sig = (sig * \gain.kr(1)).tanh;
                sig = BLowShelf.ar(sig,\loFreq.kr(8000),1,\loDb.kr(0).neg);
                sig = BHiShelf.ar(sig,\hiFreq.kr(8000),1,\hiDb.kr(0).neg);

                sig = LeakDC.ar(sig);
                sig = sig * \trim.kr(1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("Tanh", Rect(0,0,300,240));

        synths.add( Synth(\ns_tanh,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("loFreq",ControlSpec(20,1250,\exp),"loDb",\boostcut, { |xy| 
                synths[0].set(\loFreq,xy.x, \loDb,xy.y)
            },[180,0]).round_([1,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("hiFreq",ControlSpec(1250,12000,\exp),"hiDb",\boostcut, { |xy| 
                synths[0].set(\hiFreq,xy.x, \hiDb,xy.y)
            },[8000,0]).round_([1,0.1])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        controls.add(
            NS_Fader("gain",ControlSpec(0,32,\db),{ |f| synths[0].set(\gain,f.value.dbamp) },'horz',0)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("trim",\db,{ |f| synths[0].set(\trim,f.value.dbamp) },'horz',0)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)  
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })
        );
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( 
                    VLayout( controls[0], assignButtons[0] ),
                    VLayout( controls[1], assignButtons[1] ),
                ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3] ),
                HLayout( controls[4], assignButtons[4], controls[5], assignButtons[5] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            
        ],randCol: true).oscString("Gain")
    }
}
