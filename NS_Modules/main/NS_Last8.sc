NS_Last8 : NS_SynthModule {
    classvar <isSource = false;
    var buffer;
    var mixBus, rateBus, posBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_last8Rec,{
                var numChans = NSFW.numChans;
                var sig      = In.ar(\bus.kr,numChans);
                var rec      = RecordBuf.ar(sig,\bufnum.kr,run: \rec.kr(1));
                NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
            }).add;

            SynthDef(\ns_last8Play,{
                var numChans = NSFW.numChans;
                var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var trig     = \trig.tr();
                var rate     = BufRateScale.kr(bufnum) * \rate.kr(1);
                var pos      = Phasor.ar(TDelay.ar(T2A.ar(trig),0.02),rate,0,frames,\startPos.kr(0) * frames);
                var duckTime = SampleRate.ir * 0.02 * rate;
                var duck     = pos > (frames - duckTime);

                sig = BufRd.ar(numChans,bufnum,pos);
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0,duck + trig);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("Last8", Rect(0,0,210,120));
        synths = Array.newClear(2);

        mixBus = Bus.control(modGroup.server,1).set(1);
        rateBus = Bus.control(modGroup.server,1).set(1);
        posBus = Bus.control(modGroup.server,1).set(0);

        fork {
            var cond = CondVar();
            var samps = modGroup.server.sampleRate * 8;
            var chans = NSFW.numChans;
            buffer = Buffer.alloc(modGroup.server, samps, chans, { cond.signalOne });
            cond.wait { (buffer.numFrames * buffer.numChannels) == (samps * chans) };
            modGroup.server.sync;
            synths.put(0, Synth(\ns_last8Rec,[\bufnum,buffer,\bus,bus],modGroup) );
        };

        controls.add(
            NS_Fader("rate",ControlSpec(0.5,2,\exp),{ |f| rateBus.set(f.value) },'horz',1)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("pos",ControlSpec(0,1,'lin'),{ |f| posBus.set(f.value) },'horz',0)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["trig",Color.black,Color.white],["trig",Color.white,Color.black]])
            .action_({ |but|
                synths[1].set(\trig,but.value)
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| mixBus.set(f.value) },'horz',initVal:1)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                if(val == 0,{
                    synths[1].set(\gate,0);
                    synths[1] = nil
                },{
                    synths.put(1,
                        Synth(\ns_last8Play,[
                            \bufnum,buffer,
                            \rate,rateBus.asMap,
                            \startPos,posBus.asMap,
                            \mix,mixBus.asMap,
                            \bus,bus
                        ],modGroup,\addToTail)
                    )
                });
                strip.inSynthGate_(val);
                synths[0].set(\rec,1 - val);
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
        buffer.free;
        rateBus.free;
        posBus.free;
        mixBus.free;
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Fader(horizontal: true, snap: true),
            OSC_Fader(horizontal: true, snap: true),
            OSC_Button(),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("Last8")
    }
}
