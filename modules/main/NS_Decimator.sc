NS_Decimator : NS_SynthModule {

    buildSynthModule {
        var sRate = nsServer.options.sampleRate - 1;

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_decimator" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var bits = \bits.kr(10);
                var sRate = \sRate.kr(sRate);

                sig = sig.round(2 ** (1 - bits.max(1))).clip2; // bit reduction
                sig = Latch.ar(sig, Impulse.ar(sRate));        // sRate reduction
                sig = LeakDC.ar(sig);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controls.addAll(
                    NS_Control(\sRate, ControlSpec(80, sRate, \exp), sRate)
                    .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) }),

                    NS_Control(\bits, ControlSpec(1, 10, \lin), 10)
                    .addAction(\synth,{ |c| synths[0].set(\bits, c.value) }),

                    NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_Control(\bypass, ControlSpec(0, 1, \lin,1), 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    })
                )
            }
        );

        loaded = true;
    }

    makeModuleView {
        this.makeWindow("Decimator", Rect(0, 0, 180, 90));

        modView.layout_(
            VLayout(
                NS_ControlFader(controls['sRate'], 1),
                NS_ControlFader(controls['bits']),
                NS_ControlFader(controls['mix']),
                NS_ControlButton.bypass(controls['bypass']),
            )
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(width: "85%"),
            OpenStagePanel([
                OpenStageFader(false, false), 
                OpenStageButton(height: "20%")
            ])
        ], columns: 2, randCol: true).oscString("Decimator")
    }
}
