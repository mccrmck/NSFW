NS_RefusalOutro : NS_SynthModule {
    classvar <isSource = true;
    var buffer, bufferPath;
    var ampBus;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_refusalOutro,{
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var trig     = \trig.tr(0);
                var sig, pos = Phasor.ar(TDelay.ar(T2A.ar(trig),0.04),BufRateScale.kr(bufnum) * \rate.kr(1),\offset.kr(0) * frames,frames);
                // slighty different than original, should check
                pos = SelectX.ar(DelayN.kr(\which.kr(0),0.04),[pos, pos * LFDNoise1.kr(1).range(0.9,1.1)]);
                sig = BufRd.ar(2,bufnum,pos % frames,4);
                sig = sig * Env([1,0,1],[0.04,0.04]).ar(0,trig + Changed.kr(\which.kr));

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )  
            }).add
        }
    }

    init {
        this.initModuleArrays(6);

        bufferPath = "audio/refusalOutro.wav".resolveRelative;

        fork{
            buffer = Buffer.read(modGroup.server, bufferPath);
            modGroup.server.sync;
            synths.add( Synth(\ns_refusalOutro,[\bus,bus,\bufnum,buffer],modGroup) )
        };

        controls[0] = NS_Control(\rate,ControlSpec(0.25,1,\exp),1)
        .addAction(\synth,{ |c| synths[0].set(\rate,c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\which,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\which,c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \switch).maxWidth_(30);

        controls[2] = NS_Control(\trig,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\trig,c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(30);

        controls[3] = NS_Control(\offset,ControlSpec(0,1),0)
        .addAction(\synth,{ |c| synths[0].set(\trig,1,\offset,c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\amp,\amp)
        .addAction(\synth,{ |c| synths[0].set(\amp,c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass,ControlSpec(0,1,\lin,1))
        .addAction(\synth,{ |c|  
            var val = c.value;
            strip.inSynthGate_(val);
            synths[0].set(\trig,val,\thru, val)
        });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        this.makeWindow("RefusalOutro", Rect(0,0,240,150));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]), assignButtons[0] ),
                HLayout( NS_ControlSwitch(controls[1], ["dry","wet"], 2), assignButtons[1] ),
                HLayout( NS_ControlButton(controls[2], "trig"!2),         assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]), assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4]), assignButtons[4] ),
                HLayout( NS_ControlButton(controls[5], ["▶","bypass"]),   assignButtons[5] ),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Switch(2, 2, 'slide'),
            OSC_Fader(),
            OSC_Button('push'),
            OSC_Panel([OSC_Fader(false), OSC_Button(width:"20%")], columns: 2)      
        ], randCol:true).oscString("RefusalOutro")
    }
}
