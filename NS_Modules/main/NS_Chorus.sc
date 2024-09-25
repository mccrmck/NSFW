NS_Chorus : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_chorus,{
                var numChans = NSFW.numOutChans;
                var in = In.ar(\bus.kr, numChans);

                var sig = in + LocalIn.ar(numChans);
                var depth = \depth.kr(0.5).lag(0.5) * 0.01;
                8.do{ sig = DelayC.ar(sig,0.03, LFDNoise3.kr(\rate.kr(0.2).lag(0.5)).range(0.018 - depth,0.018 + depth) ) };

                LocalOut.ar(sig.rotate * \feedB.kr(0.5).lag(0.1));
                //sig =  sig + in;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(4);
        this.makeWindow("Chorus", Rect(0,0,270,210));

        synths.add( Synth(\ns_chorus,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("rate",ControlSpec(0.01,20,\exp),"depth",ControlSpec(0,1,\lin),{ |xy| 
                synths[0].set(\rate,xy.x, \depth, xy.y);
            },[0.5,0.5]).round_([0.01,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("feedB",ControlSpec(0,0.9,\lin),{ |f| synths[0].set(\feedB, f.value) },'horz',initVal:0.5)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

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
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                VLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2], controls[3], assignButtons[3])
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader(horizontal: true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])   
        ],randCol: true).oscString("Chorus")
    }
}
