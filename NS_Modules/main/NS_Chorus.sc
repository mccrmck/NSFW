NS_Chorus : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(5);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_chorus" ++ numChans).asSymbol,
            {
                var in = In.ar(\bus.kr, numChans);
                var sig = in + LocalIn.ar(numChans);
                var depth = \depth.kr(0.5).lag(0.5) * 0.01;
                8.do{ 
                    sig = DelayC.ar(
                        sig, 
                        0.03, 
                        LFDNoise3.kr(\rate.kr(0.2)).range(0.018 - depth,0.018 + depth)
                    ) 
                };

                LocalOut.ar(sig.rotate * \feedB.kr(0.5).lag(0.1));
                //sig =  sig + in;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\rate,ControlSpec(0.01,20,\exp),0.5)
                .addAction(\synth,{ |c| synths[0].set(\rate,c.value) });

                controls[1] = NS_Control(\depth,ControlSpec(0,1,\lin),0.5)
                .addAction(\synth,{ |c| synths[0].set(\depth,c.value) });

                controls[2] = NS_Control(\feedB,ControlSpec(0,0.9,\lin),0.5)
                .addAction(\synth,{ |c| synths[0].set(\feedB,c.value) });

                controls[3] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[4] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
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
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlButton(controls[4], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(width: "70%"),
            OpenStageFader(true, false),
            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height: "20%")
            ])   
        ], columns: 3, randCol: true).oscString("Chorus")
    }
}
