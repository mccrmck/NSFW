NS_Squish : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(8);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_squish" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var amp = Amplitude.ar(sig, \atk.kr(0.01), \rls.kr(0.1));
                amp = amp.max(-100.dbamp).ampdb;
                amp = (amp - \thresh.kr(-12)).max(0) * (\ratio.kr(4).reciprocal - 1);
                amp = amp.lag(\knee.kr(0)).dbamp;

                sig = sig * amp * \muGain.kr(0).dbamp;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\thresh, \db, -12)
                .addAction(\synth, { |c| synths[0].set(\thresh, c.value) });

                controls[1] = NS_Control(\ratio, ControlSpec(1, 20, \lin), 4)
                .addAction(\synth, { |c| synths[0].set(\ratio, c.value) });

                controls[2] = NS_Control(\atk, ControlSpec(0.001, 0.1, \lin), 0.001)
                .addAction(\synth, { |c| synths[0].set(\atk, c.value) });

                controls[3] = NS_Control(\rls, ControlSpec(0.001, 0.3, \lin), 0.001)
                .addAction(\synth, { |c| synths[0].set(\rls, c.value) });

                controls[4] = NS_Control(\knee, ControlSpec(0, 0.5, \lin), 0.1)
                .addAction(\synth, { |c| synths[0].set(\knee, c.value) });

                controls[5] = NS_Control(\mUp, ControlSpec(0, 20, \db), 0)
                .addAction(\synth, { |c| synths[0].set(\muGain, c.value) });

                controls[6] = NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                .addAction(\synth, { |c| synths[0].set(\mix, c.value) });

                controls[7] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                .addAction(\synth, { |c| 
                    this.gateBool_(c.value);
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("Squish", Rect(0,0,270,180));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 0.1),
                HLayout( 
                    *4.collect({ |i| 
                        NS_ControlKnob(controls[i+1], 0.001).minHeight_(75) 
                    })
                ),
                NS_ControlFader(controls[5], 0.1),
                NS_ControlFader(controls[6]),
                NS_ControlButton(controls[7], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStagePanel({ OpenStageKnob() } ! 4, columns: 4),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2),
        ], randCol: true).oscString("Squish")
    }
}
