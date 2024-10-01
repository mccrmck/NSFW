NS_ShortLoops : NS_SynthModule {
    classvar <isSource = false;
    var buffer;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_shortLoops,{
                var numChans = NSFW.numChans;
                var sig    = In.ar(\bus.kr,numChans);
                var bufnum = \bufnum.kr;
                var gate   = \trig.ar(0) + DelayN.ar(Impulse.ar(0));
                var rate   = BufRateScale.kr(bufnum) * \rate.kr(1);
                var wrPos  = Phasor.ar(gate, gate, 0, BufFrames.kr(bufnum), 0);
                var end    = Latch.ar(wrPos,1 - gate); 
                var dev    = \deviation.kr(0 ! numChans);
                var rdPos  = Phasor.ar(Changed.ar(gate) + TDelay.ar(T2A.ar(\reset.tr(0)),0.02),(1 - gate) * rate,dev * end, end, dev * end);
                var duck   = rdPos > (end - (SampleRate.ir * 0.02 * rate));

                var rec    = BufWr.ar(sig,bufnum,wrPos);

                sig = BufRd.ar(numChans,bufnum <! rec,rdPos );
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0,duck + \reset.tr(0));
                sig = sig * (1 - gate).lag(0.02);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )  
            }).add;
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("ShortLoops", Rect(0,0,240,120));

        fork {
            var cond = CondVar();
            buffer = Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 3, NSFW.numChans,{ cond.signalOne });
            modGroup.server.sync;
            cond.wait { buffer.numChannels == NSFW.numChans };
            synths.add( Synth(\ns_shortLoops,[\bufnum,buffer,\bus,bus],modGroup) );
        };

        controls.add(
            NS_Fader("rate",ControlSpec(0.25,2,\exp),{ |f| synths[0].set(\rate, f.value) },'horz',1)
        );
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(45);

        // this could be much better
        controls.add(
            NS_Fader("deviation",ControlSpec(0,0.5,\lin),{ |f| synths[0].set(\reset,1, \deviation, { f.value.rand } ! NSFW.numChans ) },'horz',0)
        );
        assignButtons[1] = NS_AssignButton(this,1,\fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["rec",Color.black,Color.white],["loop",Color.white,Color.black]])
            .action_({ |but|
                synths[0].set(\trig,but.value.asInteger)
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
                HLayout( controls[2], assignButtons[2], controls[4], assignButtons[4] ),
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
            OSC_Fader(horizontal: true,snap: true),
            OSC_Button(height: "40%",mode: 'push'),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("ShortLoops")
    }
}
