NS_FreeVerb : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_freeVerb" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = HPF.ar(sig, 80) + PinkNoise.ar(0.0001);
                sig = BLowShelf.ar(sig, \preLoFreq.kr(200), 1, \preLodB.kr(0));
                sig = BHiShelf.ar(sig, \preHiFreq.kr(8000), 1, \preHidB.kr(0));
                sig = FreeVerb.ar(sig, 1, \room.kr(1), \damp.kr(0.9));
                sig = BLowShelf.ar(sig, \postLoFreq.kr(200), 1, \postLodB.kr(0));
                sig = BHiShelf.ar(sig, \postHiFreq.kr(8000), 1, \postHidB.kr(0));
                
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    // this could use a better interface, I think
                    NS_ControlFloat(\prLoHz, ControlSpec(20, 2500, \exp), 200)
                    .addAction(\synth, { |c| synths[0].set(\preLoFreq, c.value) }),

                    NS_ControlFloat(\prLodB, \boostcut, 0)
                    .addAction(\synth, { |c| synths[0].set(\preLodB, c.value) }),

                    NS_ControlFloat(\prHiHz, ControlSpec(2500, 10000, \exp), 8000)
                    .addAction(\synth, { |c| synths[0].set(\preHiFreq, c.value) }),

                    NS_ControlFloat(\prHidB, \boostcut, 0)
                    .addAction(\synth, { |c| synths[0].set(\preHidB, c.value) }),

                    NS_ControlFloat(\poLoHz, ControlSpec(20, 2500, \exp), 200)
                    .addAction(\synth, { |c| synths[0].set(\postLoFreq, c.value) }),

                    NS_ControlFloat(\poLodB, \boostcut, 0)
                    .addAction(\synth, { |c| synths[0].set(\postLodB, c.value) }),

                    NS_ControlFloat(\poHiHz, ControlSpec(2500, 10000, \exp), 8000)
                    .addAction(\synth, { |c| synths[0].set(\postHiFreq, c.value) }),

                    NS_ControlFloat(\poHidB, \boostcut, 0)
                    .addAction(\synth, { |c| synths[0].set(\postHidB, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    }),
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            VLayout( 
                NS_ControlFader(controlDict['prLoHz'], 1),
                NS_ControlFader(controlDict['prHiHz'], 1),
                NS_ControlFader(controlDict['poLoHz'], 1),
                NS_ControlFader(controlDict['poHiHz'], 1),
            ),
            HLayout( 
                NS_ControlKnob(controlDict['prLodB']),
                NS_ControlKnob(controlDict['prHidB']),
                NS_ControlKnob(controlDict['poLodB']),
                NS_ControlKnob(controlDict['poHidB']),
            ),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton(controlDict['bypass'], ["▶", "bypass"])
        )
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
