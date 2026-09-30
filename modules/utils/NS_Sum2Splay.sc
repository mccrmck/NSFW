NS_Sum2Splay : NS_SynthModule {

    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_sum2Splay" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                Out.ar(\sendBus.kr(), Splay.ar(sig, 1, \sendAmp.kr(0)) * \mute.kr(0) );
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlString(\sendBus, "0")
                    .addAction(\synth, { |c| 
                        var val = c.value.asInteger;
                        // TODO: new server methods probably affect all of this
                        if(val < (nsServer.options.outChannels - 1)) {
                            synths[0].set(\sendBus, val)
                        } {
                            // could add color change for emphasis?
                            fork{ c.value_("N/A"); 0.5.wait; c.resetValue }
                        }
                    }),

                    NS_ControlFloat(\sendAmp, \db)
                    .addAction(\synth, { |c| synths[0].set(\sendAmp, c.value.dbamp) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
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
            NS_ControlText(controlDict['sendBus']),
            NS_ControlFader(controlDict['sendAmp'], 1),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader().snap_(false).vertical,
            OpenStageButton().height_("20%")
        ]).randCol.label_("SumSplay")
    }
}
