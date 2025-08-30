NS_Pulsar : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(5);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_pulsar" ++ numChans).asSymbol,
            {
                var sig  = In.ar(\bus.kr, numChans);
               


                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth|
                synths.add(synth);

                controls[0] = NS_Control(\freq, ControlSpec(1, 3500, \exp), 40)
                .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });

                controls[1] = NS_Control(\mFreq, ControlSpec(1, 3500, \exp), 4)
                .addAction(\synth,{ |c| synths[0].set(\modFreq, c.value) });

                controls[2] = NS_Control(\mMul, ControlSpec(1, 3500, \amp))
                .addAction(\synth,{ |c| synths[0].set(\modMul, c.value) });

                controls[3] = NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[4] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value);
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("Pulsar", Rect(0,0,180,120));

        win.layout_(
            VLayout(
             

                NS_ControlButton(controls[4], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([



            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height: "20%")
            ])
        ], columns: 3, randCol: true).oscString("Pulsar")
    }
}
