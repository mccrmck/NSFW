NS_Chorus : NS_SynthModule {

    buildSynthModule {
        var voices   = 4;

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_chorus" ++ numChans).asSymbol,
            {
                var in    = In.ar(\bus.kr, numChans);
                var sig   = in + LocalIn.ar(numChans);
                var dTime = \dTime.kr(0.1,0.5);
                var depth = \depth.kr(0.5).lag(0.5);
                var noise = PinkNoise.ar(0.0001);
                voices.do({ |i|
                    var phs = (i + (360/voices)).degrad;
                    var mod = SinOsc.kr(0.01, phs);
                    mod = (mod + SinOsc.kr(\rate.kr(0.2), phs)) * 0.5 * depth;
                    mod = mod.linexp(-1, 1, dTime / 2, dTime * 2);
                    sig = DelayC.ar(sig + noise, 0.1, mod)
                });

                sig = sig * voices.reciprocal.sqrt;

                LocalOut.ar(sig * \feedB.kr(0.5));
                sig = sig + in;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(

                    NS_ControlFloat(\rate, ControlSpec(0.01, 7, \exp), 0.5)
                    .addAction(\synth,{ |c| synths[0].set(\rate, c.value) }),

                    NS_ControlFloat(\dTime, ControlSpec(0.01, 0.05), 0.015)
                    .addAction(\synth,{ |c| synths[0].set(\dTime, c.value) }),

                    NS_ControlFloat(\depth, ControlSpec(0.01, 1, \exp), 0.05)
                    .addAction(\synth,{ |c| synths[0].set(\depth, c.value) }),

                    NS_ControlFloat(\feedB, ControlSpec(0, 0.9), 0.5)
                    .addAction(\synth,{ |c| synths[0].set(\feedB, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value); 
                        synths[0].set(\thru, c.value)
                    })
                );

                loaded = true;
            }
        );
    }

    nsModuleLayout {
            ^VLayout(
                NS_ControlFader(controlDict['rate']),
                NS_ControlFader(controlDict['dTime'], 0.001),
                NS_ControlFader(controlDict['depth']),
                NS_ControlFader(controlDict['feedB']),
                NS_ControlFader(controlDict['mix']),
                NS_ControlButton.bypass(controlDict['bypass']),
            )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false),
                OpenStageButton().height("20%")
            ]).columns_(2)   
        ]).randCol.label_("Chorus")
    }
}
