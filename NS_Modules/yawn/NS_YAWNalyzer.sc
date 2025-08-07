NS_YAWNalyzer : NS_SynthModule {
    var <netAddr;
    var ip = "127.0.0.1", port = "8000";
    var onsetBut;
    var onsetPath, onsetMsg;
    var rmsPath, rmsMsg;
    var localResponder;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(10);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_yawnalyzer" ++ numChans).asSymbol,
            {
                var sig  = In.ar(\bus.kr, numChans);
                var mono = sig.sum * numChans.reciprocal.sqrt;
                var sr   = SampleRate.ir;

                var chain  = FFT(LocalBuf(1024), mono);
                var onsets = Onsets.kr(              // trig
                    chain, \thresh.kr(0.2), \rcomplex, relaxtime: 0.25 // s between trigs
                );
                var rms    = RMS.ar(mono, \smooth.kr(4));
                var trig   = Impulse.ar(\tFreq.kr(20));

                trig = trig + onsets * \bypass.kr(0);

                SendReply.ar(trig,'/yawnalysis', [Sweep.ar, onsets, rms]);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
            },
            [\bus, strip.stripBus],
            { |synth|
                synths.add(synth);

                netAddr = NetAddr(ip, port);

                localResponder.free;
                localResponder = OSCFunc({ |msg|
                    var time   = msg[3];    // use this for conditional logic?
                    var onsets = msg[4];
                    var rms    = msg[5];

                    if(onsets.asBoolean,{
                      onsetPath !? { netAddr.sendMsg(onsetPath, onsetMsg) }
                    });
                    
                    { onsetBut.value_(onsets.asInteger) }.defer;

                    rmsPath !? { netAddr.sendMsg(rmsPath, rmsMsg, rms) };

                }, '/yawnalysis', argTemplate: [synths[0].nodeID]);


                controls[0] = NS_Control(\ip, \string, "127.0.0.1")
                .addAction(\synth,{ |c| 
                    ip = c.value; 
                    netAddr.disconnect;
                    netAddr = NetAddr(ip, port.asInteger)
                });

                controls[1] = NS_Control(\port, \string, "8000")
                .addAction(\synth,{ |c| port = c.value; netAddr.port_(port.asInteger) });

                controls[2] = NS_Control(\updateFreq, ControlSpec(1, 40, \lin), 20)
                .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });

                controls[3] = NS_Control(\thresh, \amp, 0.2)
                .addAction(\synth,{ |c| synths[0].set(\thresh, c.value) });

                controls[4] = NS_Control(\onsetPath, \string, "")
                .addAction(\synth,{ |c| 
                    if(onsetPath.size > 0,{
                        onsetPath = c.value
                    },{
                        onsetPath = nil;
                    })
                });

                controls[5] = NS_Control(\onsetMsg, \string, "")
                .addAction(\synth,{ |c| onsetMsg = c.value });

                controls[6] = NS_Control(\rmsPath, \string, "")
                .addAction(\synth,{ |c| 
                    if(rmsPath.size > 0,{
                        rmsPath = c.value
                    },{
                        rmsPath = nil;
                    })
                });

                controls[7] = NS_Control(\rmsMsg, \string, "")
                .addAction(\synth,{ |c| rmsMsg = c.value });

                controls[8] = NS_Control(\smoothRMS, ControlSpec(0.1, 10, \exp), 4)
                .addAction(\synth,{ |c| synths[0].set(\smooth, c.value) });

                controls[9] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value);
                    onsetBut !? { onsetBut.value_(0) };
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
                HLayout(
                    NS_ControlFader(controls[3], 0.001),
                    onsetBut
                ),
                HLayout(
                    NS_ControlText(controls[4]).maxHeight_(30),
                    NS_ControlText(controls[5]).maxHeight_(30)
                ),
                HLayout(
                    NS_ControlText(controls[6]).maxHeight_(30),
                    NS_ControlText(controls[7]).maxHeight_(30)
                ),
                NS_ControlFader(controls[8]),
                NS_ControlButton(controls[9], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    freeExtra {
        localResponder.free
    }

    *oscFragment {       
        ^OpenStagePanel([

            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height: "20%")
            ])
        ], columns: 3, randCol: true).oscString("RingMod")
    }
}
