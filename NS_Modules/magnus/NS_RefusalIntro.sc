NS_RefusalIntro : NS_SynthModule {
    classvar <isSource = true;
    var buffer, bufferPath;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_refusalIntro,{
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var sig = PlayBuf.ar(4,bufnum,BufRateScale.kr(bufnum),trigger: \trig.tr(0));
                sig = sig[0..1] + sig[2..3];

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )  
            }).add
        }
    }

    init {
        this.initModuleArrays(2);
        this.makeWindow("RefusalIntro", Rect(0,0,200,60));

        bufferPath = "audio/refusalIntro.wav".resolveRelative;

        fork{
            buffer = Buffer.read(modGroup.server, bufferPath);
            modGroup.server.sync;
            synths.add( Synth(\ns_refusalIntro,[\bus,bus,\bufnum,buffer],modGroup) )
        };

        controls[0] = NS_Control(\amp,\amp,1)
        .addAction(\synth,{ |c| synths[0].set(\amp,c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\bypass,ControlSpec(0,1,\lin,1))
        .addAction(\synth,{ |c|  
            var val = c.value;
            strip.inSynthGate_(val);
            synths[0].set(\trig,val,\thru, val)
        });
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                , assignButtons[0] ),
                HLayout( NS_ControlButton(controls[1],["▶","bypass"]), assignButtons[1] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(false),
            OSC_Button(height:"20%")
        ], randCol:true).oscString("RefusalIntro")
    }
}
