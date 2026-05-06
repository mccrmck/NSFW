NS_BPFDelay : NS_SynthModule {

    buildSynthModule {

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_bpfDelay" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal.sqrt;
                var tFreq = \tFreq.kr(0.2);
                var trig = Impulse.ar(tFreq);
                var tScale = tFreq.reciprocal;

                sig = sig + LocalIn.ar(5);
                sig = 5.collect({ |i| var del = (i * 0.2) + 0.2; DelayN.ar(sig[i], del, del) });
                LocalOut.ar(sig * \feedB.kr(-3.dbamp));

                // should I have more than 2 voices?
                // maybe all 5 voices but with modulating levels?
                sig = 2.collect({
                    var pan = Latch.ar(LFDNoise1.ar(tFreq, 0.8), trig);
                    var tmp = SelectX.ar(TIRand.ar(0, sig.size - 1, trig).lag(tScale), sig);
                    tmp = BBandPass.ar(tmp.tanh, TExpRand.ar(350,8000,trig).lag(tScale), \bw.kr(1));
                    NS_Pan(tmp, numChans, pan.lag(tScale), numChans / 4);
                });
                sig = sig.sum * \trim.kr(1);
                sig = LeakDC.ar(sig);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\tFreq, ControlSpec(0.1, 5, \exp), 0.2)
                    .addAction(\synth, { |c| synths[0].set(\tFreq, c.value) }),

                    NS_ControlFloat(\feedB, ControlSpec(-12, 0, \db), -3)
                    .addAction(\synth, { |c| synths[0].set(\feedB, c.value.dbamp) }),

                    NS_ControlFloat(\bw, ControlSpec(0.2, 2, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\bw, c.value) }),

                    NS_ControlFloat(\trim, \boostcut, 0)
                    .addAction(\synth, { |c| synths[0].set(\trim, c.value.dbamp) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1, \lin), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
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
            NS_ControlFader(controlDict['feedB']),
            NS_ControlFader(controlDict['bw']),
            NS_ControlFader(controlDict['trim']),
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
                OpenStageButton().width_("20%")
            ]).columns_(2)
        ]).randCol.label_("BPFDelay")
    }
}
