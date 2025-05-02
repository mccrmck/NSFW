NS_Test : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(3);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_test" ++ numChans).asSymbol,
            {
                var freq = LFDNoise3.kr(1).range(80, 8000);
                var sig = Select.ar(\which.kr(0),[
                    SinOsc.ar(freq, mul: AmpCompA.kr(freq, 80)),
                    PinkNoise.ar()
                ]);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\which, ControlSpec(0,1,'lin',1), 0)
                .addAction(\synth,{ |c| synths[0].set(\which, c.value) });

                controls[1] = NS_Control(\amp, \amp.asSpec)
                .addAction(\synth,{ |c| synths[0].set(\amp, c.value) });

                controls[2] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
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
        this.makeWindow("Test", Rect(0,0,150,60));

        win.layout_(
            VLayout(
                NS_ControlSwitch(controls[0], ["sine", "pink"], 2),
                NS_ControlFader(controls[1]),
                NS_ControlButton(controls[2], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Switch(2, 2),
            OSC_Fader(false),
            OSC_Button()
        ], randCol: true).oscString("Test")
    }
}
