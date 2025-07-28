NS_RefusalIntro : NS_SynthModule {
    var buffer;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(2);

        buffer = Buffer.read(server, "audio/refusalIntro.wav".resolveRelative);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_refusalIntro" ++ numChans).asSymbol,
            {
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var sig = PlayBuf.ar(
                    4, bufnum, BufRateScale.kr(bufnum), trigger: \trig.tr(0)
                );
                sig = sig[0..1] + sig[2..3];

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )  
            },
            [\bus, strip.stripBus, \bufnum, buffer],
            { |synth|
                synths.add(synth);

                controls[0] = NS_Control(\amp, \amp, 1)
                .addAction(\synth,{ |c| synths[0].set(\amp,c.value) });

                controls[1] = NS_Control(\bypass, ControlSpec(0,1,\lin,1))
                .addAction(\synth,{ |c|  
                    var val = c.value;
                    this.gateBool_(val);
                    synths[0].set(\trig, val, \thru, val)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("RefusalIntro", Rect(0,0,200,60));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlButton(controls[1], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(false),
            OpenStageButton(height: "20%")
        ], randCol: true).oscString("RefusalIntro")
    }
}
