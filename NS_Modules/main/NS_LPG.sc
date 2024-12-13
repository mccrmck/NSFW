NS_LPG : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_lpg,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var amp = Amplitude.ar(sig.sum * -3.dbamp * \gainOffset.kr(1),\atk.kr(0.1),\rls.kr(0.1));
                var rq = \rq.kr(0.707);

                sig = Select.ar(\which.kr(0),[
                    BLowPass.ar(sig,amp.linexp(0,1,20,20000),rq),
                    BHiPass.ar(sig,amp.linexp(0,1,20,20000),rq),
                    BLowPass.ar(sig,amp.linexp(0,1,20000,20),rq),
                    BHiPass.ar(sig,amp.linexp(0,1,20000,20),rq),
                ]);

                sig = LeakDC.ar(sig.tanh);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("LPG", Rect(0,0,300,250));

        synths.add( Synth(\ns_lpg,[\bus,bus],modGroup) );

        controls.add(
            NS_Fader("trim",ControlSpec(-12,12,\db),{ |f| synths[0].set(\gainOffset,f.value.dbamp) },'horz',initVal: 0)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_XY("atk",ControlSpec(0.001,0.1,\lin),"rls",ControlSpec(0.001,0.1,\lin),{ |xy| 
                synths[0].set(\atk,xy.x, \rls, xy.y);
            },[0.1,0.1]).round_([0.001,0.001])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        controls.add(
            NS_Switch(["LPG","HPG","ILPG","IHPG"],{ |switch| synths[0].set(\which,switch.value) },'horz')
        );
        assignButtons[2] = NS_AssignButton(this, 2, \switch).maxWidth_(45);

        controls.add(
            NS_Fader("rq",ControlSpec(0.01, 1, \exp),{ |f| synths[0].set(\rq, f.value) },'horz',initVal: 0.707)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

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
                VLayout( 
                    controls[1], assignButtons[1], 
                    HLayout( controls[0], assignButtons[0] ),
                    HLayout( controls[2], assignButtons[2] ),
                    HLayout( controls[3], assignButtons[3] )
                ),
                VLayout( controls[4], assignButtons[4], controls[5], assignButtons[5] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {
        ^OSC_Panel(horizontal:false, widgetArray:[
            OSC_Panel(height: "50%", widgetArray:[
                OSC_XY(width: "75%", snap:true),
                OSC_Switch(columns: 1, mode: 'slide', numPads: 4),
            ]),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Panel( widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("LPG")
    }
}

