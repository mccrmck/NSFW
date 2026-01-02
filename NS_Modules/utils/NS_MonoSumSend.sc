NS_MonoSumSend : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(5);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_monoSumSend" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var sum = sig.sum * numChans.reciprocal.sqrt;
                var lpFreq = \lpFreq.kr(80, 0.1);

                sum = SelectX.ar(\which.kr(0),[
                    sum,
                    LPF.ar(LPF.ar(sum, lpFreq), lpFreq)
                ]);

                sum = NS_Envs(sum, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                Out.ar(\sendBus.kr, sum * \sendAmp.kr(0) * \mute.kr(0));
                ReplaceOut.ar(\bus.kr, sig)
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\lpf, ControlSpec(20,120,\exp), 80)
                .addAction(\synth, { |c| synths[0].set(\lpFreq, c.value) });

                controls[1] = NS_Control(\filter, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| synths[0].set(\which, c.value) });

                controls[2] = NS_Control(\outBus, \string, "0")
                .addAction(\synth,{ |c| 
                    var val = c.value.asInteger;
                    if(val < nsServer.options.outChannels,{
                        synths[0].set(\sendBus, val)
                    },{
                        // could add color change for emphasis?
                        fork{ c.value_("N/A"); 0.5.wait; c.resetValue }
                    })
                });

                controls[3] = NS_Control(\amp, ControlSpec(0,1,\lin), 0)
                .addAction(\synth, { |c| synths[0].set(\sendAmp, c.value) });

                controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value); 
                    synths[0].set(\mute, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("MonoSumSend", Rect(0,0,180,90));        

        win.layout_(
            VLayout(
                HLayout(
                    NS_ControlFader(controls[0], 1),
                    NS_ControlButton(controls[1], ["LPF", "noFilt"]).maxWidth_(30),
                ),
                NS_ControlText(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlButton(controls[4], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
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
