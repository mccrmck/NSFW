NS_SumSplay : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(3);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_sumpSplay" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                Out.ar(\sendBus.kr(), Splay.ar(sig, 1, \sendAmp.kr(0)) * \mute.kr(0) );
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);
         
                controls[0] = NS_Control(\sendBus, \string, "0")
                .addAction(\synth,{ |c| 
                    var val = c.value.asInteger;
                    if(val < (nsServer.options.outChannels - 1),{ // sends a stereo signal
                        synths[0].set(\sendBus, val)
                    },{
                        // could add color change for emphasis?
                        fork{ c.value_("N/A"); 0.5.wait; c.resetValue }
                    })
                });

                controls[1] = NS_Control(\sendAmp, \db.asSpec)
                .addAction(\synth,{ |c| synths[0].set(\sendAmp, c.value.dbamp) });

                controls[2] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
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
        this.makeWindow("SumSplay", Rect(0,0,180,75));

        win.layout_(
            VLayout(
                NS_ControlText(controls[0]),
                NS_ControlFader(controls[1], 1),
                NS_ControlButton(controls[2], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(false, false),
            OpenStageButton(height: "20%")
        ], randCol: true).oscString("SumSplay")
    }
}
