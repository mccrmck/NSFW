NS_ShortLoops : NS_SynthModule {
    classvar <isSource = false;
    var buffers, samps, phasorBus, phasorStart, phasorEnd;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_shortLoops,{
                var numChans = NSFW.numChans;
                var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr(0 ! numChans);
                var frames   = BufFrames.kr(bufnum);

                var recHead  = Phasor.ar(DC.ar(0),\rec.kr(0), 0, frames);
                var rec      = numChans.collect{ |i| BufWr.ar(sig[i],bufnum[i],recHead) } ;

                var trigLoop = \tLoop.tr(1);
                var plyStart = \playStart.kr(0) + \deviation.kr(0 ! numChans);
                var plyEnd   = \playEnd.kr(48000) + \offset.kr(0);
                var rate     = \rate.kr(1);
                var plyHead  = Phasor.ar(TDelay.ar(T2A.ar(trigLoop),0.02), rate, plyStart,plyEnd,plyStart).wrap(0,frames);
                
                var duckTime = SampleRate.ir * 0.02 * rate;
                var duck     = plyHead > (plyEnd.wrap(0,frames) - duckTime);
                duck         = duck + (plyHead > (frames - duckTime));

                Out.kr(\phasorBus.kr,A2K.kr(recHead));

                sig = numChans.collect{ |i| BufRd.ar(1,bufnum[i],plyHead[i]) } * \mute.kr(0);
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0,duck + trigLoop);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) ) 
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("ShortLoops", Rect(0,0,210,120));

        samps = modGroup.server.sampleRate * 6;
        buffers = Array.newClear(NSFW.numChans);
        phasorBus = Bus.control(modGroup.server,1).set(0);
        phasorStart = 0;
        phasorEnd = samps;
        
        fork {
            var cond = CondVar();
            var chans = NSFW.numChans;
            chans.do({ |index|
                buffers[index] = Buffer.alloc(modGroup.server, samps, 1, { cond.signalOne });
                cond.wait { buffers[index].numFrames == samps };
            });
            modGroup.server.sync;
            synths.add( Synth(\ns_shortLoops,[\bufnum,buffers,\phasorBus,phasorBus,\bus,bus],modGroup) );
        };

        controls[0] = NS_Control(\rate, ControlSpec(0.25,2,\exp),1)
        .addAction(\synth, { |c| synths[0].set(\rate, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\dev, ControlSpec(0,0.5,\lin),0)
        .addAction(\synth, { |c| 
            var dev = { c.value.rand } ! NSFW.numChans;
            var delta = (phasorEnd - phasorStart).wrap(0,samps);
            dev = delta * dev;
            synths[0].set(\tLoop, 1, \deviation, dev) 
        });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\recLoop, ControlSpec(0,1,\lin,1),0)
        .addAction(\synth, { |c| 
            if(c.value == 1,{
                synths[0].set(\rec,1);
                phasorStart = phasorBus.getSynchronous;
            },{
                var offset = 0;
                phasorEnd = phasorBus.getSynchronous;
                if((phasorEnd - phasorStart).isNegative,{ offset = samps });
                synths[0].set(\rec,0, \tLoop,1, \playStart,phasorStart, \playEnd,phasorEnd, \offset,offset, \mute,1)
            })
        });
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(30);

        controls[3] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                 , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1])                 , assignButtons[1] ),
                HLayout( NS_ControlButton(controls[2], ["rec","loop"]), assignButtons[2] ),

                HLayout( NS_ControlFader(controls[3])                 , assignButtons[3] ),
                HLayout( NS_ControlButton(controls[4], ["▶","bypass"]), assignButtons[4] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        buffers.do(_.free);
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
