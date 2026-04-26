NS_RingModIn : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_ringModIn" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var sideChainBus = \sideChain.kr(-1);
                var mod = SelectX.ar(sideChainBus < 0,[
                    In.ar(sideChainBus, numChans),
                    DC.ar(0)
                ]);

                sig = sig * mod * 20.dbamp;
                sig = sig.tanh;
                sig = sig * \trim.kr(1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlString(\sideChain, "in")
                    .addAction(\synth, { |c|
                        var sourcePage = c.value.first.digit;
                        var sourceStrip = c.value.last.digit;

                        // TODO: fix this with new Server methods/names
                        //case
                        //// $i.digit, integer for inputStrip
                        //{ sourcePage == 18 and: {sourceStrip < nsServer.options.inChannels} }{
                        //    var source = nsServer.inputs[sourceStrip];
                        //    synth.set(\sideChain, source.stripBus);  // this is post fader, is it what we want?
                        //}
                        //// if sourcePage == integer, it must be a matrixStrip
                        //{ sourcePage < 10 }{ 
                        //    var thisPage  = strip.stripId.first.digit;
                        //    var thisStrip = strip.stripId.last.digit;
                        //
                        //    var stripBool = sourceStrip != thisStrip;
                        //    var pageBool  = case
                        //    { sourcePage < thisPage}{ true }
                        //    { sourcePage == thisPage and: {sourceStrip < thisStrip} }{ true }
                        //    { false };
                        //
                        //    if(stripBool and: pageBool,{
                        //        var source = nsServer.strips[sourcePage][sourceStrip];
                        //        synth.set(\sideChain, source.stripBus);  // this is post fader, is it what we want?
                        //    },{
                        //        fork{
                        //            // could add color change for emphasis
                        //            c.value_("N/A");
                        //            0.5.wait;
                        //            c.resetValue
                        //        }
                        //
                        //    })
                        //}
                        //{ synth.set(\sideChain, -1) };
                    }),

                    NS_ControlFloat(\trim, \boostcut, 0)
                    .addAction(\synth, { |c| synths[0].set(\trim, c.value.dbamp) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
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
            NS_ControlSink(controlDict['sideChain']).maxHeight_(20),
            NS_ControlFader(controlDict['trim']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("RingModIn")
    }
}
