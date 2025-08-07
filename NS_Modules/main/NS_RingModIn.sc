NS_RingModIn : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(5);
       
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
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\sideChain, \string, "in")
                .addAction(\synth,{ |c|
                    var sourcePage = c.value.first.digit;
                    var sourceStrip = c.value.last.digit;

                    case
                    // $i.digit, integer for inputStrip
                    { sourcePage == 18 and: {sourceStrip < nsServer.options.inChannels} }{
                        var source = nsServer.inputs[sourceStrip];
                        synth.set(\sideChain, source.stripBus);  // this is post fader, is it what we want?
                    }
                    // if sourcePage == integer, it must be a matrixStrip
                    { sourcePage < 10 }{ 
                        var thisPage  = strip.stripId.first.digit;
                        var thisStrip = strip.stripId.last.digit;

                        var stripBool = sourceStrip != thisStrip;
                        var pageBool  = case
                        { sourcePage < thisPage}{ true }
                        { sourcePage == thisPage and: {sourceStrip < thisStrip} }{ true }
                        { false };

                        if(stripBool and: pageBool,{
                            var source = nsServer.strips[sourcePage][sourceStrip];
                            synth.set(\sideChain, source.stripBus);  // this is post fader, is it what we want?
                        },{
                            fork{
                                // could add color change for emphasis
                                c.value_("N/A");
                                0.5.wait;
                                c.resetValue
                            }

                        })
                    }
                    { synth.set(\sideChain, -1) };
                });

                controls[1] = NS_Control(\trim, \boostcut, 0)
                .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });

                controls[2] = NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[3] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value);
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("RingModIn", Rect(0,0,180,150));

        win.layout_(
            VLayout(
                NS_ControlSink(controls[0]).maxHeight_(20),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlButton(controls[3], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
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
