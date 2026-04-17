NS_Tanh : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_tanh" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);

                sig = BHiShelf.ar(sig, \preHiFreq.kr(8000), 1, \preHidB.kr(0));
                sig = BLowShelf.ar(sig, \preLoFreq.kr(200), 1, \preLodB.kr(0));
                sig = (sig * \gain.kr(1)).tanh;
                sig = BLowShelf.ar(sig, \postLoFreq.kr(200), 1, \postLodB.kr(0));
                sig = BHiShelf.ar(sig, \postHiFreq.kr(8000), 1, \postHidB.kr(0));

                sig = LeakDC.ar(sig);
                sig = sig * \trim.kr(1);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_Control(\preLoHz, ControlSpec(20, 2000, \exp), 200)
                    .addAction(\synth, { |c| synths[0].set(\preLoFreq, c.value) }),

                    NS_Control(\preLodB, \boostcut, 0)
                    .addAction(\synth, { |c| synths[0].set(\preLodB, c.value) }),

                    NS_Control(\preHiHz, ControlSpec(2000, 10000, \exp), 8000)
                    .addAction(\synth,{ |c| synths[0].set(\preHiFreq, c.value) }),

                    NS_Control(\preHidB, \boostcut, 0)
                    .addAction(\synth,{ |c| synths[0].set(\preHidB, c.value) }),

                    NS_Control(\postLoHz, ControlSpec(20, 2000, \exp), 200)
                    .addAction(\synth,{ |c| synths[0].set(\postLoFreq, c.value) }),

                    NS_Control(\postLodB,\boostcut,0)
                    .addAction(\synth,{ |c| synths[0].set(\postLodB, c.value) }),

                    NS_Control(\postHiHz, ControlSpec(2500, 10000, \exp), 8000)
                    .addAction(\synth,{ |c| synths[0].set(\postHiFreq, c.value) }),

                    NS_Control(\postHidB, \boostcut, 0)
                    .addAction(\synth,{ |c| synths[0].set(\postHidB, c.value) }),

                    NS_Control(\gain, ControlSpec(0, 32, \db), 0)
                    .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) }),

                    NS_Control(\trim, \db, 0)
                    .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) }),

                    NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value); 
                        synths[0].set(\thru, c.value)
                    })
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            HLayout(
                NS_ControlFader(controlDict['preLoHz'], 1),
                NS_ControlKnob(controlDict['preLoDb']).minHeight_(60),
            ),
            HLayout(
                NS_ControlFader(controlDict['preHiHz'], 1),
                NS_ControlKnob(controlDict['preHiDb']).minHeight_(60),
            ),
            HLayout(
                NS_ControlFader(controlDict['postLoHz'], 1),
                NS_ControlKnob(controlDict['postLoDb']).minHeight_(60),
            ),
            HLayout( 
                NS_ControlFader(controlDict['postHiHz'], 1),
                NS_ControlKnob(controlDict['postHiDb']).minHeight_(60),
            ),
            NS_ControlFader(controlDict['gain']),
            NS_ControlFader(controlDict['trim']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStagePanel({ OpenStageKnob() } ! 4, columns: 4),
            OpenStagePanel({ OpenStageKnob() } ! 4, columns: 4),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2, height: "20%")
        ], randCol: true).oscString("Tanh")
    }
}
