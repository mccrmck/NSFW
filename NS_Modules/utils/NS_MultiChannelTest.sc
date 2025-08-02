NS_MultiChannelTest : NS_SynthModule {
    var numBox, currentChan = 0;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;
        var outChans = nsServer.options.outChannels;

        this.initModuleArrays(5);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_multiChannelTest" ++ numChans).asSymbol,
            {
                var sig = PinkNoise.ar();
                var pan = SelectX.kr(\which.kr(1),[
                    LFSaw.kr(\rate.kr(0.05)).range(0, 2), 
                    \chan.kr(0) 
                ]);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                sig = NS_Pan(sig, numChans, pan, 1, 0);
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, strip.stripBus],
            { |synth|
                synths.add(synth);

                controls[0] = NS_Control(\prev, ControlSpec(0,0,'lin',0),0)
                .addAction(\synth,{ |c|
                    currentChan = (currentChan - 1).asFloat.wrap(0, outChans);
                    synths[0].set(\which, 1, \chan, (currentChan * 2) / outChans)
                });

                controls[1] = NS_Control(\next, ControlSpec(0,0,'lin',0),0)
                .addAction(\synth,{ |c|
                    currentChan = (currentChan + 1).asFloat.wrap(0, outChans);
                    synths[0].set(\which, 1, \chan, (currentChan * 2) / outChans)
                });

                controls[2] = NS_Control(\rate, ControlSpec(0.01,0.1,'lin'), 0.05)
                .addAction(\synth,{ |c| 
                    synths[0].set(\which, 0, \rate, c.value)
                });

                controls[3] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
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
        this.makeWindow("MultiChannelTest", Rect(0,0,240,90));

        win.layout_(
            VLayout(
                HLayout( 
                    NS_ControlButton(controls[0], ["prev"]),
                    NS_ControlButton(controls[0], ["next"])
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
            OpenStagePanel({ OpenStageButton() } ! 2),
            OpenStageFader(false),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("MCTest")
    }
}
