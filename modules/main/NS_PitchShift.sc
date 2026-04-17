NS_PitchShift : NS_SynthModule {

    buildSynthModule {

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_pitchShift" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;
                var pitch = Pitch.kr(sig)[0];
                sig = PitchShiftPA.ar(sig, pitch, \ratio.kr(1), \formant.kr(1), 20, 4);

                // sig = PitchShift.ar(sig,0.05,\ratio.kr(1),\pitchDev.kr(0),0.05);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(

                    NS_Control(\ratio, ControlSpec(0.25, 4, \exp), 1)
                    .addAction(\synth,{ |c| synths[0].set(\ratio, c.value) }),

                    NS_Control(\formant, ControlSpec(0.25, 4, \exp), 1)
                    .addAction(\synth,{ |c| synths[0].set(\formant, c.value) }),

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
            NS_ControlFader(controlDict['ratio']),
            NS_ControlFader(controlDict['formant']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false, false), 
                OpenStageButton(height: "20%")
            ], width: "15%")
        ], columns: 2, randCol: true).oscString("PitchShift")
    }
}
