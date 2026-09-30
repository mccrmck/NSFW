NS_LPG : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_lpg" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var sigSum = sig.sum * numChans.reciprocal.sqrt * \gainOffset.kr(1);
                var amp = Amplitude.ar(sigSum, \atk.kr(0.1), \rls.kr(0.1));
                var rq = \rq.kr(0.707);
                // consider making these arguments on a range slider
                var loFreq = 20;
                var hiFreq = 2e4;

                sig = Select.ar(\which.kr(0),[
                    BLowPass.ar(sig, amp.linexp(0, 1, loFreq, hiFreq), rq),
                    BHiPass.ar(sig,  amp.linexp(0, 1, loFreq, hiFreq), rq),
                    BLowPass.ar(sig, amp.linexp(0, 1, hiFreq, loFreq), rq),
                    BHiPass.ar(sig,  amp.linexp(0, 1, hiFreq, loFreq), rq),
                ]);

                sig = LeakDC.ar(sig.tanh);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\trim, \boostcut, 0)
                    .addAction(\synth,{ |c| 
                        synths[0].set(\gainOffset, c.value.dbamp) 
                    }),

                    NS_ControlFloat(\atk, ControlSpec(0.001, 0.1), 0.1)
                    .addAction(\synth,{ |c| synths[0].set(\atk, c.value) }),

                    NS_ControlFloat(\rls, ControlSpec(0.001, 0.1), 0.1)
                    .addAction(\synth,{ |c| synths[0].set(\rls, c.value) }),

                    NS_ControlInt(\filt, 0, 3, 0)
                    .addAction(\synth,{ |c| synths[0].set(\which, c.value) }),

                    NS_ControlFloat(\rq, ControlSpec(1, 0.01, -2), 1/2.sqrt)
                    .addAction(\synth,{ |c| synths[0].set(\rq, c.value) }),

                    NS_ControlFloat(\mix,ControlSpec(0, 1), 1)
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
            NS_ControlFader(controlDict['trim']),
            NS_ControlFader(controlDict['atk'], 0.001),
            NS_ControlFader(controlDict['rls'], 0.001),
            NS_ControlSwitch(controlDict['filt'], ["LPG", "HPG", "ILPG", "IHPG"], 4),
            NS_ControlFader(controlDict['rq'], 0.001),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {
        ^OpenStagePanel().widgetArray_([
            OpenStagePanel().widgetArray_([
                OpenStageXY().width_("75%"), 
                OpenStageSwitch().numPads_(4)
            ]).columns_(2).height_("50%"),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel().widgetArray_([
                OpenStageFader(false).snap_(false), 
                OpenStageButton().width_("20%")
            ]).columns_(2)
        ]).randCol.label_("LPG")
    }
}

