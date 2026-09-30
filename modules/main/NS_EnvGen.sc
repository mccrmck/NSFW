NS_EnvGen : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_envGen" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var tFreq = \tFreq.kr(0.01);
                var rFreq = \rFreq.kr(0.25);
                var rMult = tFreq * \rMult.kr(1);

                // bug: revPerc can't update it's length during it's first (long) segment
                // maybe I cen refactor this using Demand Ugens and/or Latch?
                // or use a Phasor with windowing!
                var ramp = Select.kr(\which.kr(0),[
                    0,
                    LFSaw.kr(rFreq).range(0, rMult),
                    LFTri.kr(rFreq).range(0, rMult)
                ]);
                var env = \env.kr(Env.perc(0.01,0.99,1,-4).asArray);
                tFreq = tFreq + ramp;

                env = EnvGen.ar(env, Impulse.kr(tFreq), timeScale: \tScale.kr(1) * tFreq.reciprocal);

                sig = sig * env;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\tFreq, ControlSpec(0.05, 12, \exp), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\tFreq, c.value) }),

                    NS_ControlFloat(\tScale, ControlSpec(0.01, 1), 1)
                    .addAction(\synth,{ |c| synths[0].set(\tScale, c.value) }),

                    NS_ControlInt(\ramp, 0, 2, 0)
                    .addAction(\synth, { |c| synths[0].set(\which, c.value) }),

                    NS_ControlInt(\window, 0, 2, 0)
                    .addAction(\synth,{ |c| 
                        var env = c.value.switch(
                            0, { Env.perc(0.01, 0.99, 1, 4.neg).asArray },
                            1, { Env([0,1,0], [0.5,0.5], 'wel').asArray },
                            2, { Env.perc(0.99, 0.01, 1, 4).asArray }
                        );

                        synths[0].set(\env, env) 
                    }),

                    NS_ControlFloat(\rampHz, ControlSpec(0.1, 5, \exp), 0.25)
                    .addAction(\synth, { |c| synths[0].set(\rFreq, c.value) }),

                    NS_ControlFloat(\rampMul, ControlSpec(1, 10, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\rMult, c.value) }),

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
        );
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['tFreq']),
            NS_ControlFader(controlDict['tScale']),
            NS_ControlSwitch(controlDict['ramp'], ["impulse", "saw", "tri"], 3),
            NS_ControlSwitch(controlDict['window'], ["perc", "welch", "revPerc"], 3),
            NS_ControlFader(controlDict['rampHz']),
            NS_ControlFader(controlDict['rampMul']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader().vertical,
            OpenStageFader().vertical,
            OpenStageSwitch().numPads_(3),
            OpenStageSwitch().numPads_(3),
            OpenStageFader().vertical,
            OpenStageFader().vertical,
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false).vertical,
                OpenStageButton().height_("20%")
            ])     
        ]).columns_(7).randCol.label_("EnvGen")
    }
}
