NS_HenonSine : NS_SynthModule {
    classvar <isSource = true;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_henonSine,{
                var numChans = NSFW.numChans;
                var freqRate = \fRate.kr(0.1);
                var noise = \noise.kr(0.5);
                var spread = \spread.kr(0.5);
                var freq = HenonL.ar(freqRate,noise,spread).clip2;
                var sig = SinOsc.ar(freq.linexp(-1,1,40,3500));
                sig = (sig * \gain.kr(1)).fold2;
                sig = sig * -18.dbamp;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(7);
        this.makeWindow("HenonSine", Rect(0,0,300,270));

        synths.add( Synth(\ns_henonSine,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("fRate",ControlSpec(0,250,4),"noise",ControlSpec(1.1,1.4,\lin),{ |xy| 
                synths[0].set(\fRate,xy.x, \noise, xy.y);
            },[0.1,0.1]).round_([0.1,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("gain",ControlSpec(1,8,\exp),"spread",ControlSpec(0,0.3,\lin),{ |xy| 
                synths[0].set( \gain, xy.x,\spread,xy.y );
            },[0.5,0.1]).round_([0.1,0.1])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);
         
        controls.add(
            NS_Fader("amp",\db,{ |f| synths[0].set(\amp, f.value.dbamp) }).maxWidth_(45).round_(1)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                synths[0].set(\thru,val);
                strip.inSynthGate_(val);
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout( controls[0], assignButtons[0] ),
                VLayout( controls[1], assignButtons[1] ),
                VLayout( controls[2], assignButtons[2], controls[3], assignButtons[3] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_XY(snap:true),
            OSC_Panel("15%",horizontal: false, widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
            ])
        ],randCol:true).oscString("HenonSine")
    }
}
