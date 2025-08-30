NS_Chorus : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;
        var voices   = 4;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_chorus" ++ numChans).asSymbol,
            {
                var in    = In.ar(\bus.kr, numChans);
                var sig   = in + LocalIn.ar(numChans);
                var dTime = \dTime.kr(0.1,0.5);
                var depth = \depth.kr(0.5).lag(0.5);
                var noise = PinkNoise.ar(0.0001);
                voices.do({ |i|
                    var phs = (i + 90).degrad;
                    var mod = SinOsc.kr(0.01, phs);
                    mod = (mod + SinOsc.kr(\rate.kr(0.2), phs)) * 0.5 * depth;
                    mod = mod.linexp(-1, 1, dTime / 2, dTime * 2);
                    sig = DelayC.ar(sig + noise, 0.1, mod)
                });

                sig = sig * voices.reciprocal.sqrt;

                LocalOut.ar(sig * \feedB.kr(0.5));
                sig = sig + in;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\rate, ControlSpec(0.01, 7, \exp), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\rate,c.value) });

                controls[1] = NS_Control(\dTime, ControlSpec(0.01, 0.05, \lin), 0.015)
                .addAction(\synth,{ |c| synths[0].set(\dTime,c.value) });

                controls[2] = NS_Control(\depth, ControlSpec(0.01, 1, \exp), 0.05)
                .addAction(\synth,{ |c| synths[0].set(\depth,c.value) });

                controls[3] = NS_Control(\feedB, ControlSpec(0, 0.9, \lin), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\feedB,c.value) });

                controls[4] = NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0, 1, \lin,1), 0)
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

        this.makeWindow("Chorus", Rect(0,0,180,120));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1], 0.001),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(height: "20%")
            ], columns: 2)   
        ], randCol: true).oscString("Chorus")
    }
}
