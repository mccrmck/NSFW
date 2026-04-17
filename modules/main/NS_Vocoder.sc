NS_Vocoder : NS_SynthModule {

    // based on Eli Fieldsteel's Mini Tutorial: 12
    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_vocoder" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;
                var gate = Amplitude.ar(sig,0.01,0.1) > -60.dbamp;

                var numBands = 30;
                var bpfhz = (1..numBands).linexp(1, numBands, 100, 8000);
                var rq = \rq.kr(2 ** (-1/6));
                var bpfmod = BPF.ar(sig, bpfhz, rq, rq.reciprocal.sqrt);
                var track = Amplitude.ar(bpfmod,0.01,0.1).tanh;
                var pitch = FluidPitch.kr(sig,[\pitch]);

                var car = SawDPW.ar(\octave.kr(1) * 20.max(pitch).lag(\port.kr(0.01))).tanh;
                car = SelectX.ar(pitch > 5000, [car, PinkNoise.ar]);
                sig = BPF.ar(car, bpfhz, rq, rq.reciprocal.sqrt).tanh * track * gate;
                sig = LeakDC.ar(sig.sum);

                sig = (sig * \trim.kr(1)).tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_Control(\port, ControlSpec(0, 0.5, \lin))
                    .addAction(\synth,{ |c| synths[0].set(\port, c.value) }),

                    NS_Control(\octave, ControlSpec(0, 4, \lin, 1), 2)
                    .addAction(\synth,{ |c| 
                        synths[0].set(\octave, [0.25,0.5,1,2,4].at(c.value))
                    }),

                    NS_Control(\rq, ControlSpec(0.01, 1, \exp), 2 ** (-1/6))
                    .addAction(\synth,{ |c| synths[0].set(\rq, c.value) }),

                    NS_Control(\trim, ControlSpec(-9, 9, \db), 0)
                    .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) }),

                    NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
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
            NS_ControlFader(controlDict['port']),
            NS_ControlSwitch(
                controlDict['octave'], 
                ["16vb", "8vb", "nat", "8va", "16va"],
                5
            ),
            NS_ControlFader(controlDict['rq'], 0.001),
            NS_ControlFader(controlDict['trim']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {  
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStageSwitch(5, 5),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("Vocoder")
    }
}
