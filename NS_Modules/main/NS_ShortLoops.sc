NS_ShortLoops : NS_SynthModule {
    classvar <isSource = false;
    var buffer, samps, phasorBus, phasorStart, phasorEnd;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_shortLoops,{
                var numChans = NSFW.numChans;
                var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);

                var recHead  = Phasor.ar(DC.ar(0),\rec.kr(0), 0, frames);
                var rec      = BufWr.ar(sig,bufnum,recHead);

                var trigLoop = \tLoop.tr(1);
                var plyStart = \playStart.kr(0) + \deviation.kr(0 ! numChans);
                var plyEnd   = \playEnd.kr(48000) + \offset.kr(0);
                var rate     = \rate.kr(1);
                var plyHead  = Phasor.ar(TDelay.ar(T2A.ar(trigLoop),0.02), rate, plyStart,plyEnd,plyStart).wrap(0,frames);
                
                var duckTime = SampleRate.ir * 0.02 * rate;
                var duck     = plyHead > (plyEnd.wrap(0,frames) - duckTime);
                duck         = duck + (plyHead > (frames - duckTime));

                Out.kr(\phasorBus.kr,A2K.kr(recHead));

                sig = BufRd.ar(numChans,bufnum,plyHead) * \mute.kr(0);
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0,duck + trigLoop);


                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) ) 
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("ShortLoops", Rect(0,0,240,120));

        samps = modGroup.server.sampleRate * 6;
        
        phasorBus = Bus.control(modGroup.server,1).set(0);
        phasorStart = 0;
        phasorEnd = samps;
        
        fork {
            var cond = CondVar();
            var chans = NSFW.numChans;
            buffer = Buffer.alloc(modGroup.server, samps, chans, { cond.signalOne });
            cond.wait { (buffer.numFrames * buffer.numChannels) == (samps * chans) };
            modGroup.server.sync;
            synths.add( Synth(\ns_shortLoops,[\bufnum,buffer,\phasorBus,phasorBus,\bus,bus],modGroup) );
        };

        controls.add(
            NS_Fader("rate",ControlSpec(0.25,2,\exp),{ |f| synths[0].set(\rate,f.value) },'horz',1)
        );
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(45);

        controls.add(
            NS_Fader("deviation",ControlSpec(0,0.5,\lin),{ |f| 
                var dev = { f.value.rand } ! NSFW.numChans;
                var delta = (phasorEnd - phasorStart).wrap(0,samps);
                dev = delta * dev;
                synths[0].set(\tLoop,1,\deviation, dev) 
            },'horz',0)
        );
        assignButtons[1] = NS_AssignButton(this,1,\fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["rec",Color.black,Color.white],["loop",Color.white,Color.black]])
            .action_({ |but|

                if(but.value == 1,{
                    synths[0].set(\rec,1);
                    phasorStart = phasorBus.getSynchronous;
                },{
                    var offset = 0;
                    phasorEnd = phasorBus.getSynchronous;
                    if((phasorEnd - phasorStart).isNegative,{ offset = samps });
                    synths[0].set(\rec,0,\tLoop,1,\playStart,phasorStart,\playEnd,phasorEnd,\offset,offset,\mute,1)
                })
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
        buffer.free;
        phasorBus.free;
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
