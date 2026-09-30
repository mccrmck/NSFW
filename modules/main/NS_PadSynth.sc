NS_PadSynth : NS_SynthModule {

    buildSynthModule {

        var root  = 36;
        var chord = [0, 7, 16];

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_padSynth" ++ numChans).asSymbol,
            {
                var freq = \freq.kr([36, 43, 52].midicps);
                var fFreq = LFNoise2.kr(0.3).range(1000, 3000);
                var width = LFNoise2.kr(0.4).range(0.1, 0.4);
                // var sig = VarSaw.ar(
                //     freq * LFNoise2.kr(0.1!3).range(-0.1,0.1).midiratio,
                //     width: width,
                //     mul: \gain.kr(0.2).lag(0.1)
                // ).fold2.sum;
                var sig = Pulse.ar(
                    freq.lag(0.1) * LFNoise2.kr(0.1!3).range(-0.1, 0.1).midiratio,
                    width: width,
                    mul: \gain.kr(0.5).lag(0.1)
                ).fold2.sum;
                sig = sig + PinkNoise.ar(\noiseAmp.kr(0));
                sig = sig * -12.dbamp;

                sig = RLPF.ar(sig.tanh, fFreq, \rq.kr(0.5));
                sig = sig + CombC.ar(sig, 0.4, LFNoise2.kr(0.1).range(0.2,0.4), 2, 0.5);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);


                controlDict.addAll(
                    NS_ControlInt(\root, 0, 11, 0)
                    .addAction(\synth, { |c|
                        var notes = (36..47);
                        root = notes[c.value];
                        synths[0].set(
                            \freq,
                            (chord + root).midicps * ([1] ++ 
                            ({ [0.5,1,2].choose }!2)) 
                        )
                    }),

                    NS_ControlInt(\chord, 0, 1, 0)
                    .addAction(\synth, { |c|
                        var chords = [[0, 19, 28], [0, 19, 27]];
                        chord = chords[c.value];
                        synths[0].set(
                            \freq, 
                            (chord + root).midicps * ([1] ++ 
                            ({ [0.5, 1, 2].choose }!2)) 
                        )
                    }),

                    NS_ControlFloat(\rq, ControlSpec(0.1, 1, \exp), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\rq, c.value) }),

                    NS_ControlFloat(\noise, \amp, 0)
                    .addAction(\synth, { |c| synths[0].set(\noiseAmp, c.value) }),

                    NS_ControlFloat(\gain, ControlSpec(0.5, 2, \amp), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\gain, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
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
            NS_ControlSwitch(
                controlDict['root'], 
                ["C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"], 
                6
            ), 
            NS_ControlSwitch(controlDict['chord'], ["maj", "min"], 2),
            NS_ControlFader(controlDict['rq']),
            NS_ControlFader(controlDict['noise']),
            NS_ControlFader(controlDict['gain']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageSwitch().numPads_(12),
            OpenStageSwitch().numPads_(2),
            OpenStageFader().vertical,
            OpenStageFader().vertical,
            OpenStageFader().vertical,
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false).vertical, 
                OpenStageButton().height_("20%")
            ])
        ]).columns_(6).randCol.label_("PadSynth")
    }
}
