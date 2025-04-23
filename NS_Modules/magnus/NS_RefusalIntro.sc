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

        bufferPath = "audio/refusalIntro.wav".resolveRelative;

        fork{
            buffer = Buffer.read(modGroup.server, bufferPath);
            modGroup.server.sync;
            synths.add( 
                Synth(\ns_refusalIntro,
                    [\bus, strip.stripBus, \bufnum, buffer],
                    modGroup
                )
            )
        };

        controls[0] = NS_Control(\amp,\amp,1)
        .addAction(\synth,{ |c| synths[0].set(\amp,c.value) });

        controls[1] = NS_Control(\bypass,ControlSpec(0,1,\lin,1))
        .addAction(\synth,{ |c|  
            var val = c.value;
            this.gateBool_(val);
            synths[0].set(\trig,val,\thru, val)
        });

        this.makeWindow("RefusalIntro", Rect(0,0,200,60));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlButton(controls[1], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(false),
            OSC_Button(height:"20%")
        ], randCol:true).oscString("RefusalIntro")
    }
}
