NS_VarDelay : NS_SynthModule {
    var buffer;

    buildSynthModule {
        buffer = Buffer.allocConsecutive(
            numChans, nsServer.server, nsServer.options.sampleRate
        );      

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_varDelay" ++ numChans).asSymbol,
            {
                var sig     = In.ar(\bus.kr, numChans);
                var buffer  = \buffer.kr(0 ! numChans);
                var clip    = \clip.kr(1, 0.1);
                var sinFreq = \sinFreq.kr(0.05) * ({ 0.9.rrand(1) } ! numChans);
                var tap     = DelTapWr.ar(buffer, sig + LocalIn.ar(numChans));

                sig = DelTapRd.ar(
                    buffer,
                    tap,
                    \dTime.kr(0.2, 0.05) + SinOsc.ar(sinFreq).range(-0.02, 0),
                    2
                ); 
                sig = sig + PinkNoise.ar(0.0001);
                sig = Clip.ar(sig, clip.neg, clip);

                LocalOut.ar(sig.rotate(1) * \feedB.kr(-0.5.dbamp));

                sig = LeakDC.ar(sig);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
            },
            [\bus, modBus, \buffer, buffer],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\dTime, ControlSpec(0.01, 1), 0.2)
                    .addAction(\synth,{ |c| synths[0].set(\dTime, c.value) }),

                    NS_ControlFloat(\clip, ControlSpec(0.01, 1), 1)
                    .addAction(\synth,{ |c| synths[0].set(\clip, c.value) }),

                    NS_ControlFloat(\sinFreq, ControlSpec(0.01, 40,\exp), 0.05)
                    .addAction(\synth,{ |c| synths[0].set(\sinFreq, c.value) }),

                    NS_ControlFloat(\feedB, ControlSpec(-6, 3, \db), -1)
                    .addAction(\synth,{ |c| synths[0].set(\feedB, c.value.dbamp) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1, \lin), 0)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

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
            NS_ControlFader(controlDict['dTime']),
            NS_ControlFader(controlDict['clip']),
            NS_ControlFader(controlDict['sinFreq']),
            NS_ControlFader(controlDict['feedB']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("VarDelay")
    }
}
