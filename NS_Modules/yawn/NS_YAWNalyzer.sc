NS_YAWNalyzer : NS_SynthModule {
    var <netAddr;
    var ip = "127.0.0.1", port = "8000";
    var onsetBut, busses;
    var onsetPath, rmsPath, specPath;
    var localResponder;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(44);

        busses = (
            rmsSmooth:  Bus.control(server, 3).value_(10),
            rmsHPF:     Bus.control(server, 3).value_(20),
            rmsLPF:     Bus.control(server, 3).value_(1e4),
            rmsRange:   { Bus.control(server, 2).setn([0, 1]) } ! 3,
            rmsCurve:   Bus.control(server, 3).value_(0),

            specSmooth: Bus.control(server, 3).value_(10),
            specRange:  { Bus.control(server, 2).setn([0, 1]) } ! 3,
            specCurve:  Bus.control(server, 3).value_(0)
        );

        rmsPath  = Array.newClear(3);
        specPath = Array.newClear(3);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_yawnalyzer" ++ numChans).asSymbol,
            {
                var sig  = In.ar(\bus.kr, numChans);
                var mono = sig.sum * numChans.reciprocal.sqrt;

                // onsets
                var onsetBpf = LPF.ar(HPF.ar(mono, \onsetHpf.kr(20)), \onsetLpf.kr(1e4));
                var onsets   = Onsets.kr(
                    FFT(LocalBuf(1024), onsetBpf), \thresh.kr(0.2)
                );
                var trig     = Impulse.ar(\tFreq.kr(20)) + onsets * \bypass.kr(0);

                // rms
                var rmsSmooth = In.kr(\rmsSmooth.kr, 3);
                var rmsHPF    = In.kr(\rmsHPF.kr, 3); 
                var rmsLPF    = In.kr(\rmsLPF.kr, 3); 
                var rms       = 3.collect({ |i| 
                    var hpfRMS = HPF.ar(mono,   rmsHPF[i]);
                    var lpfRMS = LPF.ar(hpfRMS, rmsLPF[i]);
                    RMS.ar(lpfRMS, rmsSmooth[i])
                });

                // spec centroid
                var specSmooth = In.kr(\specSmooth.kr, 3);
                var spec       = SpecCentroid.kr( FFT(LocalBuf(1024), mono) );

                spec = 3.collect({ |i| LPF.kr(spec, specSmooth[i]) });

                SendReply.ar(trig, '/yawnalysis', [onsets] ++ rms ++ spec);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
            },
            [
                \bus,        strip.stripBus,
                \rmsSmooth,  busses['rmsSmooth'],
                \rmsHPF,     busses['rmsHPF'],
                \rmsLPF,     busses['rmsLPF'],
                \specSmooth, busses['specSmooth']
            ],
            { |synth|
                synths.add(synth);

                netAddr = NetAddr(ip, port.asInteger);

                localResponder.free;
                localResponder = OSCFunc({ |msg|
                    var onsets = msg[3];
                    var rms    = msg[4..6];
                    var spec   = msg[7..9];

                    if(onsets.asBoolean,{
                        onsetPath !? { netAddr.sendMsg(onsetPath, 1) }
                    });

                    { onsetBut.value_(onsets.asInteger) }.defer;

                    3.do({ |i|

                        if(rmsPath[i].notNil, {
                            var val = rms[i].lincurve(
                                busses['rmsRange'][i].subBus(0).getSynchronous,
                                busses['rmsRange'][i].subBus(1).getSynchronous,
                                0,
                                100,
                                busses['rmsCurve'].subBus(i).getSynchronous
                            );
                            netAddr.sendMsg(rmsPath[i], val)
                        });

                        if(specPath[i].notNil, {
                            var val = spec[i]
                            .explin(
                                busses['specRange'][i].subBus(0).getSynchronous,
                                busses['specRange'][i].subBus(1).getSynchronous,
                                0,
                                1
                            )
                            .lincurve(
                                0, 1, 0, 100, busses['specCurve'].subBus(i).getSynchronous
                            );

                            netAddr.sendMsg(specPath[i], val)
                        })
                    });

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

                controls[4] = NS_Control(\onsetsLoHz, \freq, 20)
                .addAction(\synth,{ |c| synths[0].set(\onsetHPF, c.value) });

                controls[5] = NS_Control(\onsetsHiHz, ControlSpec(20, 1e4, \exp), 10000)
                .addAction(\synth,{ |c| synths[0].set(\onsetLPF, c.value) });

                controls[6] = NS_Control(\thresh, \amp, 0.7)
                .addAction(\synth,{ |c| synths[0].set(\thresh, c.value) });

                3.do({ |i|
                    var count = (i * 12) + 7;

                    // rms
                    controls[count] = NS_Control("rmsPath" ++ i, \string, "")
                    .addAction(\synth,{ |c| 
                        if(c.value.size > 0,{
                            rmsPath[i] = c.value
                        },{
                            rmsPath[i] = nil;
                        })
                    });

                    controls[count + 1] = NS_Control("rmsHPF" ++ i, \freq, 20)
                    .addAction(\synth,{ |c| busses['rmsHPF'].subBus(i).set(c.value) });

                    controls[count + 2] = NS_Control("rmsLPF" ++ i, ControlSpec(20, 1e4, \exp), 1e4)
                    .addAction(\synth,{ |c| busses['rmsLPF'].subBus(i).set(c.value) });

                    controls[count + 3] = NS_Control("rmsClipLo" ++ i, \amp, 0)
                    .addAction(\synth,{ |c| busses['rmsRange'][i].subBus(0).set(c.value) });

                    controls[count + 4] = NS_Control("rmsClipHi" ++ i, \amp, 1)
                    .addAction(\synth,{ |c| busses['rmsRange'][i].subBus(1).set(c.value) });

                    controls[count + 5] = NS_Control("rmsSmooth" ++ i, ControlSpec(1, 20, \exp), 10)
                    .addAction(\synth,{ |c| busses['rmsSmooth'].subBus(i).set(c.value) });

                    controls[count + 6] = NS_Control("rmsCurve" ++ i, ControlSpec(-10, 10,\lin), 0)
                    .addAction(\synth,{ |c| busses['rmsCurve'].subBus(i).set(c.value) });

                    // spectral centroid
                    controls[count + 7] = NS_Control("specPath" ++ i, \string, "")
                    .addAction(\synth,{ |c| 
                        if(c.value.size > 0,{
                            specPath[i] = c.value
                        },{
                            specPath[i] = nil;
                        })
                    });

                    controls[count + 8] = NS_Control("specClipLo" ++ i, \freq, 20)
                    .addAction(\synth,{ |c| busses['specRange'][i].subBus(0).set(c.value) });

                    controls[count + 9] = NS_Control("specClipHi" ++ i, \freq, 2e4)
                    .addAction(\synth,{ |c| busses['specRange'][i].subBus(1).set(c.value) });

                    controls[count + 10] = NS_Control("specSmooth" ++ i, ControlSpec(1, 20, \exp), 10)
                    .addAction(\synth,{ |c| busses['specSmooth'].subBus(i).set(c.value) });

                    controls[count + 11] = NS_Control("specCurve" ++ i, ControlSpec(-10, 10,\lin), 0)
                    .addAction(\synth,{ |c| busses['specCurve'].subBus(i).set(c.value) });

                });

                controls[43] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
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
        this.makeWindow("YAWNalyzer", Rect(0, 0, 600, 90));

        onsetBut = NS_Button([
            ["", NS_Style('textDark'), NS_Style('red')],
            ["", NS_Style('textLight'), NS_Style('green')]
        ]).fixedSize_(20);

        win.layout_(
            HLayout(
                *[
                    VLayout(
                        // onsets
                        NS_ControlText(controls[3]).maxHeight_(30),
                        NS_ControlFader(controls[4], 1),
                        NS_ControlFader(controls[5], 1),
                        HLayout(
                            NS_ControlFader(controls[6], 0.001),
                            onsetBut
                        ),
                        nil,

                        NS_ControlText(controls[0]).maxHeight_(30),
                        NS_ControlText(controls[1]).maxHeight_(30),
                        NS_ControlFader(controls[2], 1), 
                        NS_ControlButton(controls[43], [NS_Style('play'), "bypass"]),
                    )
                ] ++
                3.collect({ |i|
                    var count = (i * 12) + 7;

                    VLayout(
                        // rms
                        NS_ControlText(controls[count]).maxHeight_(30),
                        NS_ControlFader(controls[count + 1], 1),
                        NS_ControlFader(controls[count + 2], 1),

                        NS_ControlFader(controls[count + 3]),
                        NS_ControlFader(controls[count + 4]),

                        NS_ControlFader(controls[count + 5]),
                        NS_ControlFader(controls[count + 6]),

                        // spec
                        NS_ControlText(controls[count + 7]).maxHeight_(30),
                        NS_ControlFader(controls[count + 8] ),
                        NS_ControlFader(controls[count + 9]),

                        NS_ControlFader(controls[count + 10]),
                        NS_ControlFader(controls[count + 11]),
                    )
                })
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    freeExtra {
        busses.do(_.free);
        localResponder.free
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageRange(false),
            OpenStageRange(false),
            OpenStageRange(false),
            OpenStageRange(false),
            OpenStageRange(false),
            OpenStageRange(false),
            OpenStageButton()
        ], randCol: true).oscString("YAWNalyzer")
    }
}
