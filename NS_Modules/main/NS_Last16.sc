NS_Last16 : NS_SynthModule {
    classvar <isSource = false;
    var buffer;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_last16,{
                var numChans = NSFW.numOutChans;
                var sig      = In.ar(\bus.kr,numChans);
                var thresh   = \thresh.kr(-60);
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var sliceDur = SampleRate.ir * 0.01;
                var gate     = FluidAmpGate.ar(sig,10,10,thresh,thresh-5,sliceDur,sliceDur,sliceDur,sliceDur);
                var pos      = Phasor.ar(DC.ar(0),gate,0,frames);
                var trig     = 1 - \trig.kr(0);
                // does this need a ducker in the recording chain?
               
                BufWr.ar(sig,bufnum,pos);

                sig = PlayBuf.ar(numChans,bufnum,BufRateScale.kr(bufnum) * \rate.kr(1),DelayN.kr(trig,0.02),\startPos.kr(0) * frames,1);
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0,trig);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("Last16", Rect(0,0,210,120));

        fork {
            buffer = Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 16,NSFW.numOutChans);
            modGroup.server.sync;
            synths.add( Synth(\ns_last16,[\bufnum,buffer,\bus,bus],modGroup) );
        };

        controls.add(
            NS_Fader("rate",ControlSpec(0.5,2,\exp),{ |f| synths[0].set(\rate,f.value) },'horz',1)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("pos",ControlSpec(0,1,'lin'),{ |f| synths[0].set(\startPos,f.value) },'horz',0)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["trig",Color.black,Color.white],["trig",Color.white,Color.black]])
            .action_({ |but|
                synths[0].set(\trig,but.value)
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

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
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[3], assignButtons[3] ),
                HLayout( controls[2], assignButtons[2], controls[4], assignButtons[4] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        buffer.free
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true, snap: true),
            OSC_Button();
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("Last16")
    }
}
