NS_EnvGen : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_envGen,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);
                var rFreq = \rFreq.kr(0.25);
                var rMult = \rMult.kr(2);

                var ramp = Select.kr(\which.kr(0),[
                    0,
                    LFSaw.kr(rFreq).range(0,rMult),
                    LFTri.kr(rFreq).range(0,rMult)
                ]);
                var tFreq = \tFreq.kr(0);
                var trig = Impulse.kr(tFreq + ramp);
                var tScale = (tFreq + ramp).reciprocal / 2;
                var env = \env.kr(Env.perc(0.01,0.49,1,-4).asArray);
                env = EnvGen.ar(env,trig,timeScale: tScale);

                sig = sig * env;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(6);

        this.makeWindow("EnvGen", Rect(0,0,240,300));

        synths.add( Synth(\ns_envGen,[\bus,bus],modGroup) );

        controls.add(
            NS_Fader("tFreq",ControlSpec(0,10,'lin'),{ |f| synths[0].set(\tFreq,f.value) },'horz',0)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Switch(["impulse","saw","tri"],{ |switch| synths[0].set(\which,switch.value) },'horz')
        );
        assignButtons[1] = NS_AssignButton(this, 1, \switch).maxWidth_(45);

        controls.add(
            NS_XY("rFreq",ControlSpec(0.1,5,\exp),"rMult",ControlSpec(1,20,\exp),{ |xy| 
                synths[0].set(\rFreq,xy.x, \rMult, xy.y);
            },[0.25,1]).round_([0.1,0.1])
        );
        assignButtons[2] = NS_AssignButton(this, 2, \xy);

        controls.add(
            NS_Switch(["perc","welch","revPerc"],{ |switch|
                var val = switch.value;
                var env;
                case
                { val == 0 }{ env = Env.perc(0.01,0.49,1,4.neg).asArray }
                { val == 1 }{ env = Env([0,1,0],[0.25,0.25],'wel').asArray }
                { val == 2 }{ env = Env.perc(0.49,0.01,1,8).asArray };

                synths[0].set(\env,env) 
            },'horz')
        );
        assignButtons[3] = NS_AssignButton(this, 3, \switch).maxWidth_(45);

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
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[3], assignButtons[3] ),
                HLayout(
                    VLayout( controls[2], assignButtons[2]),
                    VLayout( controls[4], assignButtons[4], controls[5], assignButtons[5] )
                )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(horizontal: true),
            OSC_Switch(numPads: 3),
            OSC_Switch(numPads: 3),
            OSC_XY(height: "40%",snap: true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal:true),
                OSC_Button(width:"20%")
            ])     
        ],randCol:true).oscString("EnvGen")
    }
}
