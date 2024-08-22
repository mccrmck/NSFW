NS_GateGrains : NS_SynthModule {
    classvar <isSource = false;
    var buffer;

    // inspired by/adapted from the FluidAmpGate helpfile example
    *initClass {
        ServerBoot.add{
            SynthDef(\ns_gateGrains,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr,numChans).sum * numChans.reciprocal;
                var thresh = \thresh.kr(-18);
                var width = \width.kr(0.5);
                var bufnum = \bufnum.kr;
                var sliceDur = SampleRate.ir * 0.01;
                var gate = FluidAmpGate.ar(sig,10,10,thresh,thresh-5,sliceDur,sliceDur,sliceDur,sliceDur);
                var phase = Phasor.ar(DC.ar(0),1 * gate,0,BufFrames.kr(bufnum));
                var trig = Impulse.ar(\tFreq.kr(8)) * (1-gate);
                var pan = Demand.ar(trig,0,Dwhite(width.neg,width));

                BufWr.ar(sig,bufnum,phase);

                // i could get fancy and add gain compensation based on overlap? must test...
                sig = GrainBuf.ar(numChans,trig,\grainDur.kr(0.1),bufnum,\rate.kr(1),\pos.kr(0).lag(0.01),4,pan);
                sig = sig.tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(6);

        this.makeWindow("GateGrains", Rect(0,0,300,250));

        fork {
            buffer = Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 2);
            modGroup.server.sync;
            synths.add( Synth(\ns_gateGrains,[\bufnum,buffer,\bus,bus],modGroup) );
        };

        controls.add(
            NS_XY("grainDur",ControlSpec(0.01,1,\exp),"tFreq",ControlSpec(4,80,\exp),{ |xy| 
                synths[0].set(\grainDur,xy.x, \tFreq, xy.y);
            },[0.1,8]).round_([0.01,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("pos",ControlSpec(0,1,\lin),"rate",ControlSpec(0.25,2,\exp),{ |xy| 
                synths[0].set(\pos,xy.x, \rate, xy.y);
            },[0,1]).round_([0.01,0.01])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        controls.add(
            NS_Fader("thresh",\db,{ |f| synths[0].set(\thresh, f.value) },'horz',initVal: -18).round_(1)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader);

        controls.add(
            NS_Fader("width",ControlSpec(0,1,\lin),{ |f| synths[0].set(\width, f.value) },'horz',initVal: 0.5).round_(0.1)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)
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
                HLayout( 
                    VLayout(controls[0], assignButtons[0] ),
                    VLayout(controls[1], assignButtons[1] ),
                ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3] ),
                HLayout( controls[4], assignButtons[4], controls[5], assignButtons[5] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        buffer.free
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Panel(height:"50%",widgetArray:[
                OSC_XY(snap:true),
                OSC_XY(snap:true),
            ]),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("GateGrains")
    }
}
