NS_GrainDelay : NS_SynthModule {
    classvar <isSource = false;

    /* SynthDef based on the similar SynthDef by PlaymodesStudio */
    *initClass {
        ServerBoot.add{
            SynthDef(\ns_grainDelay, {
                var numChans    = NSFW.numChans;
                var sig         = In.ar(\bus.kr, numChans).sum * numChans.reciprocal.sqrt;
                var sampRate    = SampleRate.ir;

                var circularBuf = LocalBuf(sampRate * 3, 1).clear;
                var bufFrames   = BufFrames.kr(circularBuf) - 1;
                var writePos    = Phasor.ar(DC.ar(0), 1, 0, bufFrames);
                var rec         = BufWr.ar(sig, circularBuf, writePos);
                
                // maybe there could be a repeater thing with a Latch.ar() and a TDelay on the writePos?
                var readPos     = Wrap.ar(writePos - (\dTime.kr(0.1) * sampRate), 0, bufFrames);
                var grainDur    = \grainDur.kr(0.25);

                var trig        = Impulse.ar(\tFreq.kr(4));
                var pan         = Demand.ar(trig,0,Dwhite(-1,1));
                var durJit      = Demand.ar(trig,0,Dwhite(1,1.5));
                var posJit      = Demand.ar(trig,0,Dwhite(0,grainDur)) * sampRate;

                sig = GrainBuf.ar(
                    numChans,
                    trig,
                    grainDur * durJit, 
                    circularBuf <! rec, 
                    \rate.kr(1),
                    (readPos - posJit) / bufFrames,
                    pan: pan
                );

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(4);
        this.makeWindow("GrainDelay", Rect(0,0,300,270));

        synths.add( Synth(\ns_grainDelay,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("grainDur",ControlSpec(0.01,1,\exp),"tFreq",ControlSpec(2,80,\exp),{ |xy| 
                synths[0].set(\grainDur,xy.x, \tFreq, xy.y);
            },[0.25,4]).round_([0.01,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("dTime",ControlSpec(0.1,1.5,\exp),"rate",ControlSpec(0.5,2,\exp),{ |xy| 
                synths[0].set( \dTime, xy.x,\rate,xy.y );
            },[0.1,1]).round_([0.1,0.1])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);
         
        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz')
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
            VLayout(
                HLayout(
                    VLayout( controls[0], assignButtons[0] ),
                    VLayout( controls[1], assignButtons[1] ),
                ),
                HLayout( controls[2], assignButtons[2], controls[3], assignButtons[3] ),
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
        ],randCol:true).oscString("Grain Delay")
    }
}
