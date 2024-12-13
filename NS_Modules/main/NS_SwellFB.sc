NS_SwellFB : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_swellFB,{
                var numChans = NSFW.numChans;
                var in = In.ar(\bus.kr, numChans);
                var coef = \coef.kr(1);
                var thresh = \thresh.kr(0.5);
                var sig = in.sum * numChans.reciprocal.sqrt * Env.sine(\dur.kr(0.1)).ar(gate: \trig.tr);
                var pan = Demand.kr(\trig.tr(),0,Dwhite(-1.0,1.0));
                sig = sig + LocalIn.ar(1);
                sig = DelayC.ar(sig,0.1, \delay.kr(0.03));
                sig = sig * (1 -Trig.ar(Amplitude.ar(sig) > thresh,0.1)).lag(0.01);
                LocalOut.ar(sig * coef);
                sig = LeakDC.ar(HPF.ar(sig,80));
                //sig = ReplaceBadValues.ar(sig,0,);
                                
                sig = NS_Pan(sig, numChans,pan,numChans/4);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
                sig = (in * \muteThru.kr(1)) + sig;
                ReplaceOut.ar( \bus.kr, sig );
            }).add
        }
    }

    init {
        this.initModuleArrays(7);
        this.makeWindow("SwellFB", Rect(0,0,300,270));

        synths.add( Synth(\ns_swellFB,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("delay",ControlSpec(1000.reciprocal,0.1,\exp),"dur",ControlSpec(0.01,0.1,\exp),{ |xy| 
                synths[0].set(\delay,xy.x, \dur, xy.y);
            },[0.03,0.1]).round_([0.001,0.001])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("coef",ControlSpec(0.95,1.5,\lin),{ |f| synths[0].set(\coef, f.value) },'horz',1).round_(0.01)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("thresh",ControlSpec(-24,-3,\db),{ |f| synths[0].set(\thresh, f.value.dbamp) },'horz',-6).round_(1)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["trig",Color.black,Color.white],["trig",Color.white,Color.black]])
            .action_({ |but|
                if(but.value == 1,{
                    synths[0].set(\trig,1)
                })
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        controls.add(
            Button()
            .states_([["mute thru",Color.black,Color.white],["unmute thru",Color.white,Color.black]])
            .action_({ |but|
                synths[0].set(\muteThru,1 - but.value)
            })
        );
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(45);

        controls.add(
            NS_Fader("amp",\db,{ |f| synths[0].set(\amp, f.value.dbamp) },'horz').round_(1)
        );
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
            })
        );
        assignButtons[6] = NS_AssignButton(this, 6, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                controls[0], assignButtons[0],
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3], controls[4], assignButtons[4] ),
                HLayout( controls[5], assignButtons[5], controls[6], assignButtons[6] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader(height: "15%",horizontal: true, snap:true),
            OSC_Panel(height:"15%",widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("SwellFB")
    }
}
