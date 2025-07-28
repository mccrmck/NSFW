NS_MonoSumSend : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;
        var outChans = nsServer.options.outChannels - 1; // sends a mono signal

        this.initModuleArrays(5);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_monoSumSend" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var sum = sig.sum * numChans.reciprocal.sqrt;
                var coFreq = \coFreq.kr(80, 0.1);

                sum = SelectX.ar(\which.kr(0),[
                    sum,
                    LPF.ar(LPF.ar(sum,coFreq),coFreq)
                ]);

                sum = NS_Envs(sum, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                Out.ar(\sendBus.kr, sum * \sendAmp.kr(0) * \mute.kr(0));
                ReplaceOut.ar(\bus.kr, sig)
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\lpf, ControlSpec(20,120,\exp), 80)
                .addAction(\synth, { |c| synths[0].set(\coFreq, c.value) });

                controls[1] = NS_Control(\filter, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| synths[0].set(\which, c.value) });

                controls[2] = NS_Control(\outBus, ControlSpec(0,outChans,\lin,1), 0)
                .addAction(\synth,{ |c| synths[0].set(\sendBus, c.value) });

                controls[3] = NS_Control(\amp, ControlSpec(0,1,\lin), 0)
                .addAction(\synth, { |c| synths[0].set(\sendAmp, c.value) });

                controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value); 
                    synths[0].set(\mute, c.value)
                });

                { this.makeModuleWindow(outChans) }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow { |outChans|
        this.makeWindow("MonoSumSend", Rect(0,0,300,90));        

        win.layout_(
            VLayout(
                HLayout( 
                    NS_ControlFader(controls[0], 1),
                    NS_ControlButton(controls[1], ["LPF", "noFilt"]),
                ),
                HLayout(
                    NS_ControlMenu(controls[2], (0..outChans) ),
                    NS_ControlButton(controls[4], ["▶", "bypass"]),
                ),
                NS_ControlFader(controls[3]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStageButton(),
            OpenStageFader(false),
            OpenStageButton()
        ], randCol: true).oscString("MonoSumSend")
    }
}
