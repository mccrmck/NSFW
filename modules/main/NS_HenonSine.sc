NS_HenonSine : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_henonSine" ++ numChans).asSymbol,
            {
                var freqRate = \fRate.kr(0.1);
                var noise = \noise.kr(0.5);
                var spread = \spread.kr(0.5);
                var freq = HenonL.ar(freqRate, noise, spread).clip2;
                var sig = SinOsc.ar(freq.linexp(-1, 1, 80, 3500));
                sig = (sig * \gain.kr(1)).fold2;
                sig = sig * -18.dbamp;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_Control(\fRate, ControlSpec(0, 250, 4), 0.1)
                    .addAction(\synth,{ |c| synths[0].set(\fRate, c.value) }),

                    NS_Control(\noise, ControlSpec(1.1, 1.4, \lin), 0.1)
                    .addAction(\synth,{ |c| synths[0].set(\noise, c.value) }),

                    NS_Control(\gain, ControlSpec(1, 8, \exp), 1)
                    .addAction(\synth,{ |c| synths[0].set(\gain, c.value) }),

                    NS_Control(\spread, ControlSpec(0, 0.3, \lin), 0.1)
                    .addAction(\synth,{ |c| synths[0].set(\spread, c.value) }),

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
            NS_ControlFader(controlDict['fRate'], 0.1),
            NS_ControlFader(controlDict['noise'], 0.001),
            NS_ControlFader(controlDict['gain']),
            NS_ControlFader(controlDict['spread'], 0.001),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false, false), 
                OpenStageButton(height:"20%")
            ], width: "15%")
        ], columns: 3, randCol: true).oscString("HenonSine")
    }
}
