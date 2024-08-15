NS_BufferPB : NS_SynthModule{
    classvar <isSource = true;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_bufferPBmono,{
                var numChans = NSFW.numOutChans;
                var bufnum = \bufnum.kr;
                var sig = PlayBuf.ar(1,bufnum,BufRateScale.kr(bufnum), loop: 1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(4);

        this.makeWindow("BufferPB", Rect(0,0,300,250));

        synths.add( Synth(\ns_ringMod,[\bus,bus],modGroup) );


        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(60)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(60);

        controls.add(
            Button()
            .maxWidth_(60)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(60);

        win.layout_(
            HLayout(
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
        ],randCol:true).oscString("BufferPB")
    }
}
