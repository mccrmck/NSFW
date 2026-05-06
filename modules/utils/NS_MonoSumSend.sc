NS_MonoSumSend : NS_SynthModule {

    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_monoSumSend" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var sum = sig.sum * numChans.reciprocal.sqrt;
                var lpFreq = \lpFreq.kr(80, 0.1);

                sum = SelectX.ar(\which.kr(0), [
                    sum,
                    LPF.ar(LPF.ar(sum, lpFreq), lpFreq)
                ]);

                sum = NS_Envs(sum, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                Out.ar(\sendBus.kr, sum * \sendAmp.kr(0) * \mute.kr(0));
                ReplaceOut.ar(\bus.kr, sig)
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(

                    NS_ControlFloat(\lpf, ControlSpec(20, 120, \exp), 80)
                    .addAction(\synth, { |c| synths[0].set(\lpFreq, c.value) }),

                    NS_ControlInt(\filter, 0, 1, 0)
                    .addAction(\synth,{ |c| synths[0].set(\which, c.value) }),

                    NS_ControlString(\outBus, "0")
                    .addAction(\synth,{ |c| 
                        var val = c.value.asInteger;
                        // TODO: fix this with new server methods
                        if(val < nsServer.options.outChannels) {
                            synths[0].set(\sendBus, val)
                        } {
                            // could add color change for emphasis?
                            fork{ c.value_("N/A"); 0.5.wait; c.resetValue }
                        }
                    }),

                    NS_ControlFloat(\amp, \amp, 0)
                    .addAction(\synth, { |c| synths[0].set(\sendAmp, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value); 
                        synths[0].set(\mute, c.value)
                    })
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            HLayout(
                NS_ControlFader(controlDict['lpf'], 1),
                NS_ControlButton(
                    controlDict['filter'], ["LPF", "noFilt"]
                ).maxWidth_(30),
            ),
            NS_ControlText(controlDict['outBus']),
            NS_ControlFader(controlDict['amp']),
            NS_ControlButton.bypass(controlDict['bypass'])
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageButton(),
            OpenStageFader().snap_(false),
            OpenStageButton()
        ]).randCol.label_("MonoSumSend")
    }
}
