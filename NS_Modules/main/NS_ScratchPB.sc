NS_ScratchPB : NS_SynthModule {
    classvar <isSource = false;
    var buffer;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_scratchPB,{
                var numChans = NSFW.numChans;
                var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var freq     = \freq.kr(4);             // freq could use some noise, no?
                var scratch  = LFDNoise0.ar(freq,\width.kr(0.5));
                var pos      = Phasor.ar(DC.ar(0),BufRateScale.kr(bufnum) * (scratch + 1) * scratch.sign,0,frames);
                var rec      = RecordBuf.ar(sig,bufnum,run:\run.kr(1));

                sig = BufRd.ar(numChans,bufnum <! rec,pos,1);
                sig = HPF.ar(sig,20).tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(3);
        this.makeWindow("ScratchPB", Rect(0,0,210,240));

        fork {
            var cond = CondVar();
            buffer = Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 2, NSFW.numChans,{ cond.signalOne });
            modGroup.server.sync;
            cond.wait { buffer.numChannels == NSFW.numChans };
            synths.add( Synth(\ns_scratchPB,[\bufnum,buffer,\bus,bus],modGroup) );
        };

        controls.add(
            NS_XY("freq",ControlSpec(0.1,36,1.5),"width",ControlSpec(0.01,1,\lin),{ |xy|
                synths[0].set(\freq,xy.x,\width,xy.y)
            },[4,0.5]).round_([0.1,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(45)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\run, 1 - val,\thru, val)
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout( controls[0], assignButtons[0] ),
                VLayout( controls[1], assignButtons[1], controls[2], assignButtons[2] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        buffer.free
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_XY(snap:true),
            OSC_Panel(horizontal: false, widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
            ])
        ],randCol:true).oscString("ScratchPB")
    }
}
