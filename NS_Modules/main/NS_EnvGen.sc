NS_EnvGen : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_EnvGen,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans);

                var ramp = SelectX.kr(\which.kr(0),[0,LFSaw.kr(),LFTri.kr()]);
                var trig = Impulse.kr(\trigFreq.kr(0) + ramp);
                var env = Env([0,1,0],[\atk.kr(0.01),\rls.kr(0.49)],\curve.kr(0)).ar(0,trig,\tScale.kr(1));
                
                sig = sig * env;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(4);

        this.makeWindow("EnvGen", Rect(0,0,300,300));

        synths.add( Synth(\ns_ringMod,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("freq",ControlSpec(1,3500,\exp),"modFreq",ControlSpec(1,3500,\exp),{ |xy| 
                synths[0].set(\freq,xy.x, \modFreq, xy.y);
            },[40,4]).round_([1,1])
        );
        assignButtons[0] = NS_AssignButton().setAction(this, 0, \xy);


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
        assignButtons[3] = NS_AssignButton().maxWidth_(60).setAction(this,3,\button);

        win.layout_(
            HLayout()
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[],randCol:true).oscString("EnvGen")
    }
}
