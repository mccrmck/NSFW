NS_RefusalIntro : NS_SynthModule {
    classvar <isSource = true;
    var buffer, bufferPath;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_refusalIntro,{
                var numChans = NSFW.numOutChans;
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
        strip.inSynthGate_(1);
        this.makeWindow("RefusalIntro", Rect(0,0,200,60));

        bufferPath = "audio/refusalIntro.wav".resolveRelative;

        fork{
            buffer = Buffer.read(modGroup.server, bufferPath);
            modGroup.server.sync;
            synths.add( Synth(\ns_refusalIntro,[\bus,bus,\bufnum,buffer],modGroup) )
        };

        controls.add(
            NS_Fader("amp",\amp,{ |f| synths[0].set(\amp,f.value) },'horz',initVal:1)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["stop",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                synths[0].set(\trig,1,\thru, val)
            })
        );
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        buffer.free;
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(),
            OSC_Button(height:"20%")
        ],randCol:true).oscString("RefusalIntro")
    }
}
