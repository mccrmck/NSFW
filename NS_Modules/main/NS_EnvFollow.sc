NS_EnvFollow : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(7);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_envFollow" ++ numChans).asSymbol,
            {
                var sr = SampleRate.ir;
                var sig = In.ar(\bus.kr, numChans);
                var sideChainBus = \sideChain.kr(-1);
                var amp = SelectX.ar(sideChainBus < 0,[
                    In.ar(sideChainBus, numChans).sum * numChans.reciprocal.sqrt,
                    DC.ar(0)
                ]);

                amp = amp * \gain.kr(1);
                amp = FluidLoudness.kr(
                    amp,
                    [\loudness],
                    windowSize: sr * 0.4,
                    hopSize: sr * 0.1
                ).dbamp;

                sig = sig * LagUD.kr(amp,\up.kr(0.1),\down.kr(0.1));
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

                controls[1] = NS_Control(\gain, \boostcut, 0)
                .addAction(\synth, { |c| synths[0].set(\gain, c.value.dbamp) });

                controls[2] = NS_Control(\atk, ControlSpec(0.001, 1, \exp), 0.005)
                .addAction(\synth, { |c| synths[0].set(\up, c.value) });

                controls[3] = NS_Control(\rls, ControlSpec(0.01, 1, \exp), 0.01)
                .addAction(\synth, { |c| synths[0].set(\down, c.value) });

                controls[4] = NS_Control(\trim, \boostcut, 0)
                .addAction(\synth, { |c| synths[0].set(\trim, c.value.dbamp) });

                controls[5] = NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[6] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
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
        this.makeWindow("EnvFollow", Rect(0,0,180,150));

        win.layout_(
            VLayout(
                NS_ControlSink(controls[0]).maxHeight_(20),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2], 0.001),
                NS_ControlFader(controls[3], 0.001),
                NS_ControlFader(controls[4]),
                NS_ControlFader(controls[5]),
                NS_ControlButton(controls[6], ["▶","bypass"]), 
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

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
        ], randCol: true).oscString("EnvFollow")
    }
}
