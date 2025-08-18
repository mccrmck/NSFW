NS_YAWNalyzer : NS_SynthModule {
    var <netAddr;
    var ip = "127.0.0.1", port = "8000";
    var onsetBut;
    var onsetPath, rmsPath, specPath;
    var rmsLo  = 0, rmsHi  = 100, rmsCurve  = 0;
    var specLo = 0, specHi = 100, specCurve = 0;
    var localResponder;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(20);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_yawnalyzer" ++ numChans).asSymbol,
            {
                var sig  = In.ar(\bus.kr, numChans);
                var mono = sig.sum * numChans.reciprocal.sqrt;

                // onsets
                var onsetBpf = LPF.ar(HPF.ar(mono, \onsetHpf.kr(20)), \onsetLpf.kr(1e4));
                var onsetFft = FFT(LocalBuf(1024), onsetBpf);
                var onsets   = Onsets.kr( 
                    onsetFft, \thresh.kr(0.2), \rcomplex, relaxtime: 0.25
                );
                var trig     = Impulse.ar(\tFreq.kr(20)) + onsets * \bypass.kr(0);
                // rms
                var rmsBpf   = LPF.ar(HPF.ar(mono, \rmsHpf.kr(20)), \rmsLpf.kr(1e4));
                var rms      = RMS.ar(rmsBpf, \rmsSmooth.kr(10));

                // spec centroid
                var spec = FFT(LocalBuf(1024), mono);
                spec     = SpecCentroid.kr(spec);

                spec = LPF.kr(spec, \specSmooth.kr(10));

                SendReply.ar(trig,'/yawnalysis', [onsets, rms, spec]);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
            },
            [\bus, strip.stripBus],
            { |synth|
                synths.add(synth);

                netAddr = NetAddr(ip, port.asInteger);

                localResponder.free;
                localResponder = OSCFunc({ |msg|
                    var onsets = msg[3];
                    var rms    = msg[4].lincurve(0, 1, rmsLo, rmsHi, rmsCurve);
                    var spec   = msg[5].explin(20, 20480, 0, 1)
                    .lincurve(0, 1, specLo, specHi, specCurve);

                    if(onsets.asBoolean,{
                        onsetPath !? { netAddr.sendMsg(onsetPath, 1) }
                    });

                    { onsetBut.value_(onsets.asInteger) }.defer;

                    rmsPath !? { netAddr.sendMsg(rmsPath, rms) };

                    specPath !? { netAddr.sendMsg(specPath, spec) };

                }, '/yawnalysis', argTemplate: [synths[0].nodeID]);


                controls[0] = NS_Control(\ip, \string, "127.0.0.1")
                .addAction(\synth,{ |c| 
                    ip = c.value; 
                    netAddr.disconnect;
                    netAddr = NetAddr(ip, port.asInteger)
                });

                controls[1] = NS_Control(\port, \string, "8000")
                .addAction(\synth,{ |c| port = c.value; netAddr.port_(port.asInteger) });

                controls[2] = NS_Control(\updateFreq, ControlSpec(1, 60, \lin), 30)
                .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });

                // onsets
                controls[3] = NS_Control(\onsetPath, \string, "")
                .addAction(\synth,{ |c| 
                    if(c.value.size > 0,{
                        onsetPath = c.value
                    },{
                        onsetPath = nil;
                    })
                });

                controls[4] = NS_Control(\onsetsHPF, \freq, 20)
                .addAction(\synth,{ |c| synths[0].set(\onsetHPF, c.value) });

                controls[5] = NS_Control(\onsetsLPF, ControlSpec(20,1e4, \exp), 10000)
                .addAction(\synth,{ |c| synths[0].set(\onsetLPF, c.value) });
                
                controls[6] = NS_Control(\thresh, \amp, 0.7)
                .addAction(\synth,{ |c| synths[0].set(\thresh, c.value) });

                // rms
                controls[7] = NS_Control(\rmsPath, \string, "")
                .addAction(\synth,{ |c| 
                    if(c.value.size > 0,{
                        rmsPath = c.value
                    },{
                        rmsPath = nil;
                    })
                });

                controls[8] = NS_Control(\rmsHPF, \freq, 20)
                .addAction(\synth,{ |c| synths[0].set(\rmsHPF, c.value) });

                controls[9] = NS_Control(\rmsLPF, ControlSpec(20, 1e4, \exp), 1e4)
                .addAction(\synth,{ |c| synths[0].set(\rmsLPF, c.value) });

                controls[10] = NS_Control(\rmsLo, ControlSpec(0, 100, \lin), 0)
                .addAction(\synth,{ |c| rmsLo = c.value });

                controls[11] = NS_Control(\rmsHi, ControlSpec(0, 100, \lin), 100)
                .addAction(\synth,{ |c| rmsHi = c.value }); 

                controls[12] = NS_Control(\rmsSmooth, ControlSpec(1, 20, \exp), 10)
                .addAction(\synth,{ |c| synths[0].set(\rmsSmooth, c.value) });

                controls[13] = NS_Control(\rmsCurve, ControlSpec(-10, 10,\lin), 0)
                .addAction(\synth,{ |c| rmsCurve = c.value });

                // spectral centroid
                controls[14] = NS_Control(\specPath, \string, "")
                .addAction(\synth,{ |c| 
                    if(c.value.size > 0,{
                        specPath = c.value
                    },{
                        specPath = nil;
                    })
                });

                controls[15] = NS_Control(\specLo, ControlSpec(0, 100, \lin), 0)
                .addAction(\synth,{ |c| specLo = c.value });

                controls[16] = NS_Control(\specHi, ControlSpec(0, 100, \lin), 100)
                .addAction(\synth,{ |c| specHi = c.value });

                controls[17] = NS_Control(\specSmooth, ControlSpec(1, 20, \exp), 10)
                .addAction(\synth,{ |c| synths[0].set(\specSmooth, c.value) });

                controls[18] = NS_Control(\specCurve, ControlSpec(-10, 10,\lin), 0)
                .addAction(\synth,{ |c| specCurve = c.value });

                controls[19] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value);
                    onsetBut !? { { onsetBut.value_(0) }.defer };
                    synths[0].set(\bypass, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("YAWNalyzer", Rect(0,0,240,120));

        onsetBut = NS_Button([
            ["", NS_Style('textDark'), NS_Style('red')],
            ["", NS_Style('textLight'), NS_Style('green')]
        ]).fixedSize_(20);

        win.layout_(
            VLayout(
                HLayout(
                    NS_ControlText(controls[0]).maxHeight_(30),
                    NS_ControlText(controls[1]).maxHeight_(30)
                ),
                NS_ControlFader(controls[2], 1), 
                // onsets
                NS_ControlText(controls[3]).maxHeight_(30),
                HLayout(
                    NS_ControlFader(controls[4], 1),
                    NS_ControlFader(controls[5], 1)
                ),
                HLayout(
                    NS_ControlFader(controls[6], 0.001),
                    onsetBut
                ),
                // rms
                NS_ControlText(controls[7]).maxHeight_(30),
                HLayout(
                    NS_ControlFader(controls[8], 1),
                    NS_ControlFader(controls[9], 1)
                ),
                HLayout(
                    NS_ControlFader(controls[10], 1),
                    NS_ControlFader(controls[11], 1)
                ),
                HLayout(
                    NS_ControlFader(controls[12]),
                    NS_ControlFader(controls[13]),
                ),
                // spectral centroid
                NS_ControlText(controls[14]).maxHeight_(30),
                HLayout(
                    NS_ControlFader(controls[15], 1),
                    NS_ControlFader(controls[16], 1)
                ),
                HLayout(
                    NS_ControlFader(controls[17]),
                    NS_ControlFader(controls[18]),
                ),
                NS_ControlButton(controls[19], [NS_Style('play'), "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    freeExtra {
        localResponder.free
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageRange(),
            OpenStageButton()
        ], randCol: true).oscString("YAWNalyzer")
    }
}
