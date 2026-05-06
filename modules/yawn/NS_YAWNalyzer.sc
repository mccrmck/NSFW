NS_YAWNalyzer : NS_SynthModule {
    var <netAddr;
    var ip = "127.0.0.1", port = "8000";
    var onsetBut, busses;
    var localResponder;

    buildSynthModule {
        var server = modGroup.server;
        var onsetPath = nil;
        var rmsPath   = Array.newClear(3);
        var specPath  = Array.newClear(3);

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
                var rms       = 3.collect { |i| 
                    var hpfRMS = HPF.ar(mono,   rmsHPF[i]);
                    var lpfRMS = LPF.ar(hpfRMS, rmsLPF[i]);
                    RMS.ar(lpfRMS, rmsSmooth[i])
                };

                // spec centroid
                var specSmooth = In.kr(\specSmooth.kr, 3);
                var spec       = SpecCentroid.kr( FFT(LocalBuf(1024), mono) );

                spec = 3.collect { |i| LPF.kr(spec, specSmooth[i]) };

                SendReply.ar(trig, '/yawnalysis', [onsets] ++ rms ++ spec);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
            },
            [
                \bus,        modBus,
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

                    if(onsets.asBoolean) {
                        onsetPath !? { netAddr.sendMsg(onsetPath, 1) }
                    };

                    //{ onsetBut.value_(onsets.asInteger) }.defer;

                    3.do { |i|

                        if(rmsPath[i].notNil) {
                            var val = rms[i].lincurve(
                                busses['rmsRange'][i].subBus(0).getSynchronous,
                                busses['rmsRange'][i].subBus(1).getSynchronous,
                                0,
                                100,
                                busses['rmsCurve'].subBus(i).getSynchronous
                            );
                            netAddr.sendMsg(rmsPath[i], val)
                        };

                        if(specPath[i].notNil) {
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
                        }
                    };

                }, '/yawnalysis', argTemplate: [synths[0].nodeID]);

                controlDict.addAll(

                    NS_ControlString(\ip, "127.0.0.1")
                    .addAction(\synth, { |c| 
                        ip = c.value; 
                        netAddr.disconnect;
                        netAddr = NetAddr(ip, port.asInteger)
                    }),

                    NS_ControlString(\port, "8000")
                    .addAction(\synth, { |c| 
                        port = c.value; 
                        netAddr.port_(port.asInteger) 
                    }),

                    NS_ControlFloat(\updateFreq, ControlSpec(1, 60, \lin), 30)
                    .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) }),

                    // onsets
                    NS_ControlString(\onsetPath, "")
                    .addAction(\synth, { |c| 
                        if(c.value.size > 0) 
                        { onsetPath = c.value } 
                        { onsetPath = nil }
                    }),

                    NS_ControlFloat(\onsetLoHz, \freq, 20)
                    .addAction(\synth, { |c| synths[0].set(\onsetHPF, c.value) }),

                    NS_ControlFloat(\onsetHiHz, \freq, 1e4)
                    .addAction(\synth, { |c| synths[0].set(\onsetLPF, c.value) }),

                    NS_ControlFloat(\thresh, \amp, 0.7)
                    .addAction(\synth, { |c| synths[0].set(\thresh, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        //onsetBut !? { { onsetBut.value_(0) }.defer };
                        synths[0].set(\bypass, c.value)
                    }),
                );
                
                3.do { |i|

                    controlDict.addAll(

                        // rms
                        NS_ControlString("rmsPath" ++ i, "")
                        .addAction(\synth, { |c| 
                            if(c.value.size > 0)
                            { rmsPath[i] = c.value }
                            { rmsPath[i] = nil }
                        }),

                        NS_ControlFloat("rmsHPF" ++ i, \freq, 20)
                        .addAction(\synth, { |c| 
                            busses['rmsHPF'].subBus(i).set(c.value)
                        }),

                        NS_ControlFloat("rmsLPF" ++ i, \freq, 1e4)
                        .addAction(\synth, { |c|
                            busses['rmsLPF'].subBus(i).set(c.value)
                        }),

                        NS_ControlFloat("rmsClipLo" ++ i, \amp, 0)
                        .addAction(\synth, { |c| 
                            busses['rmsRange'][i].subBus(0).set(c.value)
                        }),

                        NS_ControlFloat("rmsClipHi" ++ i, \amp, 1)
                        .addAction(\synth, { |c| 
                            busses['rmsRange'][i].subBus(1).set(c.value)
                        }),

                        NS_ControlFloat("rmsSmooth" ++ i, ControlSpec(1, 20, \exp), 10)
                        .addAction(\synth, { |c| 
                            busses['rmsSmooth'].subBus(i).set(c.value)
                        }),

                        NS_ControlFloat("rmsCurve" ++ i, ControlSpec(-10, 10), 0)
                        .addAction(\synth, { |c| 
                            busses['rmsCurve'].subBus(i).set(c.value)
                        }),

                        // spectral centroid
                        NS_ControlString("specPath" ++ i, "")
                        .addAction(\synth, { |c| 
                            if(c.value.size > 0)
                            { specPath[i] = c.value }
                            { specPath[i] = nil }
                        }),

                        NS_ControlFloat("specClipLo" ++ i, \freq, 20)
                        .addAction(\synth, { |c| 
                            busses['specRange'][i].subBus(0).set(c.value)
                        }),

                        NS_ControlFloat("specClipHi" ++ i, \freq, 2e4)
                        .addAction(\synth, { |c| 
                            busses['specRange'][i].subBus(1).set(c.value)
                        }),

                        NS_ControlFloat("specSmooth" ++ i, ControlSpec(1, 20, \exp), 10)
                        .addAction(\synth, { |c| 
                            busses['specSmooth'].subBus(i).set(c.value)
                        }),

                        NS_ControlFloat("specCurve" ++ i, ControlSpec(-10, 10), 0)
                        .addAction(\synth, { |c| 
                            busses['specCurve'].subBus(i).set(c.value)
                        }),
                    )
                };

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        var onsets, bands; 

        onsetBut = NS_Button([
            ["", NS_Style('textDark'), NS_Style('red')],
            ["", NS_Style('textLight'), NS_Style('green')]
        ]).fixedSize_(20);

        onsets = VLayout(
            HLayout(
                NS_ControlText(controlDict['ip']),
                NS_ControlText(controlDict['port']),
            ),
            NS_ControlFader(controlDict['updateFreq'], 1), 
            NS_ControlButton(controlDict['bypass'], [NS_Style('play'), "bypass"]),

            NS_Header("onsets"),
            // onsets
            NS_ControlText(controlDict['onsetPath']),
            NS_ControlRange(controlDict['onsetLoHz'], controlDict['onsetHiHz'], 1),
            HLayout(
                NS_ControlFader(controlDict['thresh'], 0.001),
                onsetBut
            ),
        );

        bands = 3.collect { |i|
            VLayout(
                NS_Header("rms" ++ i),
                NS_ControlText(controlDict[("rmsPath" ++ i).asSymbol]),
                NS_ControlRange(
                    controlDict[("rmsHPF" ++ i).asSymbol],
                    controlDict[("rmsLPF" ++ i).asSymbol], 
                    1
                ),

                NS_ControlRange(
                    controlDict[("rmsClipLo" ++ i).asSymbol],
                    controlDict[("rmsClipHi" ++ i).asSymbol],
                ),

                NS_ControlFader(controlDict[("rmsSmooth" ++ i).asSymbol], 1),
                NS_ControlFader(controlDict[("rmsCurve" ++ i).asSymbol], 1),

                NS_Header("spec" ++ i),
                NS_ControlText(controlDict[("specPath" ++ i).asSymbol]),
                NS_ControlRange(
                    controlDict[("specClipLo" ++ i).asSymbol],
                    controlDict[("specClipHi" ++ i).asSymbol]
                ),
                NS_ControlFader(controlDict[("specSmooth" ++ i).asSymbol]),
                NS_ControlFader(controlDict[("specCurve" ++ i).asSymbol]),
            )
        };

        ^VLayout(*([onsets] ++ bands))
    }

    freeExtra {
        busses.do(_.free);
        localResponder.free
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageRange(),
            OpenStageRange(),
            OpenStageRange(),
            OpenStageRange(),
            OpenStageRange(),
            OpenStageRange(),
            OpenStageButton()
        ]).randCol.label_("YAWNalyzer")
    }
}
