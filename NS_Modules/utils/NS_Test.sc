NS_Test : NS_SynthModule {
    var currentChan = 0;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_test" ++ numChans).asSymbol,
            {
                var freq = LFDNoise3.kr(1).range(80, 8000);
                var sig = Select.ar(\whichSig.kr(0),[
                    SinOsc.ar(freq, mul: AmpCompA.kr(freq, 80)) * -6.dbamp,
                    PinkNoise.ar(),
                ]);
                var pan = SelectX.kr(\whichPan.kr(0),[
                    \chan.kr(0),
                    LFSaw.ar(\rate.kr(0.05), 1).range(0, 2)
                ]);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0));
                sig = PanAz.ar(numChans, sig, pan, 1, 1, 0);
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\whichSig, ControlSpec(0, 1, 'lin', 1), 0)
                .addAction(\synth,{ |c| synths[0].set(\whichSig, c.value) });


                // these args should change use `synth.get` to increment or decrement
                // that way they can be in sync with the LFSaw
                controls[1] = NS_Control(\prev, ControlSpec(0, 0, 'lin', 0),0)
                .addAction(\synth,{ |c|
                    currentChan = (currentChan - 1).wrap(0, numChans - 1);
                    synths[0].set(\whichPan, 0, \chan, (currentChan * 2) / numChans)
                }, false);

                controls[2] = NS_Control(\next, ControlSpec(0, 0, 'lin', 0), 0)
                .addAction(\synth,{ |c|
                    currentChan = (currentChan + 1).wrap(0, numChans - 1);
                    synths[0].set(\whichPan, 0, \chan, (currentChan * 2) / numChans)
                }, false); 

                controls[3] = NS_Control(\rate, ControlSpec(0, 0.255555, 'lin'), 0.05)
                .addAction(\synth,{ |c| 
                    synths[0].set(\whichPan, 1, \rate, c.value)
                });

                controls[4] = NS_Control(\amp, \db)
                .addAction(\synth,{ |c| synths[0].set(\amp, c.value.dbamp) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
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
                NS_ControlSwitch(controls[0], ["sine", "noise"], 2),
                HLayout(
                    NS_ControlButton(controls[1], ["prev"]),
                    NS_ControlButton(controls[2], ["next"])
                ),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageSwitch(2, 2),
            OpenStagePanel({ OpenStageButton() } ! 2, columns: 2),
            OpenStageFader(false),
            OpenStageFader(false),
            OpenStageButton()
        ], randCol: true).oscString("Test")
    }
}
