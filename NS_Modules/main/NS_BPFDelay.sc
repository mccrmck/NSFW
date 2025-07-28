NS_BPFDelay : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_bpfDelay" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal.sqrt;
                var tFreq = \tFreq.kr(0.2);
                var trig = Impulse.ar(tFreq);
                var tScale = tFreq.reciprocal;
                var pan = Latch.ar(LFDNoise1.ar(tFreq,0.8),trig);

                sig = sig + LocalIn.ar(5);
                sig = 5.collect({ |i| var del = (i * 0.2) + 0.2; DelayN.ar(sig[i], del, del) });
                LocalOut.ar(sig * \coef.kr(0.7));

                // can I make two streams out of this?
                sig = SelectX.ar(TIRand.ar(0,sig.size-1,trig).lag(tScale),sig);
                sig = BBandPass.ar(sig.tanh, {TExpRand.ar(350,8000,trig).lag(tScale)},\bw.kr(1));
                sig = sig * \trim.kr(1);
                sig = NS_Pan(sig,numChans,pan.lag(tScale),numChans/4);

                sig = LeakDC.ar(sig);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig,numChans,\bus.kr,\mix.kr(1),\thru.kr(0))
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);
        
                controls[0] = NS_Control(\tFreq, ControlSpec(0.1,5,\exp), 0.2)
                .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });

                controls[1] = NS_Control(\coef, ControlSpec(0.25,0.99,\lin), 0.7)
                .addAction(\synth,{ |c| synths[0].set(\coef, c.value) });

                controls[2] = NS_Control(\bw, ControlSpec(0.2,2, \exp), 1)
                .addAction(\synth,{ |c| synths[0].set(\bw, c.value) });

                controls[3] = NS_Control(\trim, \boostcut, 0)
                .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });

                controls[4] = NS_Control(\mix, ControlSpec(0,1,\lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value); 
                    synths[0].set(\thru, c.value) 
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        );
    }

    makeModuleWindow {
        this.makeWindow("BPFDelay", Rect(0,0,180,150));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width:"20%")
            ], columns: 2)
        ], randCol: true).oscString("BPFDelay")
    }
}
