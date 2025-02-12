NS_BPFDelay : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_bpfDelay,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr,numChans).sum * numChans.reciprocal.sqrt;
                var tFreq = \tFreq.kr(0.2);
                var trig = Impulse.ar(tFreq);
                var tScale = tFreq.reciprocal;
                var pan = Latch.ar(LFDNoise1.ar(tFreq,0.8),trig);

                sig = sig + LocalIn.ar(5);
                sig = 5.collect({ |i| var del = (i * 0.2) + 0.2; DelayN.ar(sig[i], del,del) });
                LocalOut.ar(sig * \coef.kr(0.7));

                // can I make two streams out of this?
                sig = SelectX.ar(TIRand.ar(0,sig.size-1,trig).lag(0.1),sig);
                sig = BBandPass.ar(sig.tanh, {TExpRand.ar(350,8000,trig).lag(tScale)},\bw.kr(1));
                sig = sig * \trim.kr(1);
                sig = NS_Pan(sig,numChans,pan.lag(tScale),numChans/4);

                sig = LeakDC.ar(sig);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig,numChans,\bus.kr,\mix.kr(1),\thru.kr(0))
            }).add;
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("BPFDelay", Rect(0,0,240,150));

        synths.add( Synth(\ns_bpfDelay,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\tFreq,ControlSpec(0.1,5,\exp),0.2)
        .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\coef,ControlSpec(0.25,0.99,\lin),0.7)
        .addAction(\synth,{ |c| synths[0].set(\coef, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\bw,ControlSpec(0.2,2,\exp),1)
        .addAction(\synth,{ |c| synths[0].set(\bw, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\trim,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]), assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]), assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]), assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]), assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4]), assignButtons[4] ),
                HLayout( NS_ControlButton(controls[5],["▶","bypass"]), assignButtons[5] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width:"20%")], columns: 2)
        ], randCol: true).oscString("BPFDelay")
    }
}
