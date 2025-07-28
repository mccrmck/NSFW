NS_FreeVerb : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(10);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_freeVerb" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = HPF.ar(sig,80) + PinkNoise.ar(0.0001);
                sig = BLowShelf.ar(sig, \preLoFreq.kr(200), 1, \preLodB.kr(0));
                sig = BHiShelf.ar(sig, \preHiFreq.kr(8000), 1, \preHidB.kr(0));
                sig = FreeVerb.ar(sig, 1, \room.kr(1), \damp.kr(0.9));
                sig = BLowShelf.ar(sig, \postLoFreq.kr(200), 1, \postLodB.kr(0));
                sig = BHiShelf.ar(sig, \postHiFreq.kr(8000), 1, \postHidB.kr(0));
                
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                // this could use a better interface, I think
                controls[0] = NS_Control(\prLoHz,ControlSpec(20,2500,\exp),200)
                .addAction(\synth,{ |c| synths[0].set(\preLoFreq, c.value) });

                controls[1] = NS_Control(\prLodB,\boostcut,0)
                .addAction(\synth,{ |c| synths[0].set(\preLodB, c.value) });

                controls[2] = NS_Control(\prHiHz,ControlSpec(2500,10000,\exp),8000)
                .addAction(\synth,{ |c| synths[0].set(\preHiFreq, c.value) });

                controls[3] = NS_Control(\prHidB,\boostcut,0)
                .addAction(\synth,{ |c| synths[0].set(\preHidB, c.value) });

                controls[4] = NS_Control(\poLoHz,ControlSpec(20,2500,\exp),200)
                .addAction(\synth,{ |c| synths[0].set(\postLoFreq, c.value) });

                controls[5] = NS_Control(\poLodB,\boostcut,0)
                .addAction(\synth,{ |c| synths[0].set(\postLodB, c.value) });

                controls[6] = NS_Control(\poHiHz,ControlSpec(2500,10000,\exp),8000)
                .addAction(\synth,{ |c| synths[0].set(\postHiFreq, c.value) });

                controls[7] = NS_Control(\poHidB,\boostcut,0)
                .addAction(\synth,{ |c| synths[0].set(\postHidB, c.value) });

                controls[8] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[9] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
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
        this.makeWindow("FreeVerb", Rect(0,0,270,240));

        win.layout_(
            VLayout(
                VLayout( 
                    *4.collect({ |i| NS_ControlFader(controls[i * 2], 1) })
                ),
                HLayout( 
                    *4.collect({ |i| NS_ControlKnob(controls[i * 2 + 1]) })
                ),
                NS_ControlFader(controls[8]),
                NS_ControlButton(controls[9], ["▶", "bypass"])
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStagePanel({OpenStageKnob()} ! 4, columns: 4),
            OpenStagePanel({OpenStageKnob()} ! 4, columns: 4),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2, height: "20%"),
        ], randCol: true).oscString("FreeVerb")
    }
}
