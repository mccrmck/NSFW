NS_CombFilter : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(4);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_combFilter" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = CombC.ar(
                    sig, 0.2, \delay.kr(250).reciprocal.lag, \decay.kr(0.5)
                );
                sig = sig + PinkNoise.ar(0.0001);
                sig = LeakDC.ar(sig.tanh);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\freq, ControlSpec(20,1200,\exp), 250)
        .addAction(\synth,{ |c| synths[0].set(\delay, c.value) });

        controls[1] = NS_Control(\decay, ControlSpec(0.1,3,\exp), 0.5)
        .addAction(\synth,{ |c| synths[0].set(\decay, c.value) });

        controls[2] = NS_Control(\mix, ControlSpec(0,1,\lin), 1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

        controls[3] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| 
            this.gateBool_(c.value);
            synths[0].set(\thru, c.value)
        });

        { this.makeModuleWindow }.defer;
        loaded = true;
    }

    makeModuleWindow {
        this.makeWindow("Comb Filter", Rect(0,0,210,90));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 1),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlButton(controls[3], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height: "20%")
            ], width: "15%")
        ], columns: 2, randCol: true).oscString("CombFilter")
    }
}
