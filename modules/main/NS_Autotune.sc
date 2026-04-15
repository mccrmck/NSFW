NS_Autotune : NS_SynthModule {
    var chords;

    buildSynthModule {

        var transpose = [[1], [0.5, 1], [1, 0.5]].allTuples;

        chords = [
            '5U'  , [0, 7, 12],
            '5D'  , [0,-5,-12],
            '5UD' , [0, 7, -5],

            'maj0', [0, 4, 7],
            'maj3', [0, 3, 8],
            'maj5', [0, 5, 9],

            'min0', [0, 3, 7],
            'min3', [0, 4, 9],
            'min5', [0, 5, 8],
        ];

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_autotune" ++ numChans).asSymbol,
            {
                var sig   = In.ar(\bus.kr, numChans).sum * numChans.reciprocal.sqrt;
                var track = Pitch.kr(sig);
                var pitch = 20.max(track[0]);
                var quant = pitch.cpsmidi.round;
                var diff  = (quant - pitch.cpsmidi).midiratio;
                var harm  = \harm.kr([0, 1.5, 2]).varlag(1, -10);
                var shift = PitchShiftPA.ar(sig, pitch, diff * harm, \formant.kr(1));
                // rough compensation for added voices:
                shift     = shift.sum * 3.reciprocal.sqrt; 
                // this should let transients/noise through without harmonizing - TEST
                sig       = SelectX.ar(track[1].lag(0.01),[sig, shift]);
                sig       = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_Control(\harm, ControlSpec(0, (chords.size / 2) - 1, 'lin', 1), 0)
                    .addAction(\synth,{ |c|
                        var val = c.value;
                        var harm = chords[val * 2 + 1].midiratio;
                        if(val > 2) { harm = harm * transpose.choose };
                        synths[0].set(\harm, harm)
                    }),

                    NS_Control(\formant, ControlSpec(0.5, 2, \exp), 1)
                    .addAction(\synth,{ |c| synths[0].set(\formant, c.value) }),

                    NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    })
                );

                loaded = true
            }
        );
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlSwitch(controlDict['harm'], chords[0, 2..], 3).minHeight_(90),
            NS_ControlFader(controlDict['formant']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageSwitch(9, 3),
            OpenStageFader(false, height: "20%"),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2, height: "20%")
        ], randCol: true).oscString("Autotune")
    }
}
