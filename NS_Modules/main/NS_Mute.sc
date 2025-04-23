NS_Mute : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(2);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_mute" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var lag = \lag.kr(0.02);
                var mute = Env.asr(lag, 1, lag, \lin).ar(0, 1 - \mute.kr(0));
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        this.gateBool_(true);

        controls[0] = NS_Control(\lag,ControlSpec(0.01,10,\lin),0.02)
        .addAction(\synth,{ |c| synths[0].set(\lag, c.value) });

        controls[1] = NS_Control(\mute, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| synths[0].set(\mute, c.value) });

        this.makeWindow("Mute", Rect(0,0,210,60));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlButton(controls[1], ["mute","▶"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra {
        this.gateBool_(false)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(false, false),
            OSC_Button(height: "25%")
        ], randCol: true).oscString("Mute")
    }
}
