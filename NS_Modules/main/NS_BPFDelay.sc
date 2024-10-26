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
        this.initModuleArrays(5);
        this.makeWindow("BPFDelay", Rect(0,0,225,270));

        synths.add( Synth(\ns_bpfDelay,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("tFreq",ControlSpec(0.1,5,\exp),"coef",ControlSpec(0.25,0.99,\lin),{ |xy| 
                synths[0].set(\tFreq,xy.x, \coef, xy.y);
            },[0.2,0.7]).round_([0.01,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
           NS_Fader("bw",ControlSpec(0.2,2,\exp),{ |f| synths[0].set(\bw, f.value) },'horz',initVal: 1) 
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
           NS_Fader("trim",\boostcut,{ |f| synths[0].set(\trim, f.value.dbamp) },'horz',initVal: 0) 
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);
         
        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal: 1)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                synths[0].set(\thru,val);
                strip.inSynthGate_(val);
            })
        );
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout(
                    controls[0], assignButtons[0],
                    HLayout( controls[1], assignButtons[1] ),
                    HLayout( controls[2], assignButtons[2] ),
                ),
                VLayout( controls[3], assignButtons[3], controls[4], assignButtons[4]  )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader("15%"),
            OSC_Fader("15%"),
            OSC_Panel("15%",horizontal: false, widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
            ])
        ],randCol:true).oscString("BPFDelay")
    }
}
