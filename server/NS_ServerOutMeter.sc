NS_ServerOutMeter {
    var nsServer;
    var <numChannels;
    var meterSynth, responder;

    *initClass {
        ServerBoot.add { |server|
            var srv = NSFW.servers[server.name];
            var numOutChans = srv.options.outChannels;

            SynthDef(\ns_serverOutMeter,{
                var sig = In.ar(\inBus.kr(0), numOutChans);
                var trigFreq = 20;
                SendPeakRMS.kr(sig, trigFreq, 3, "/" ++ server.name ++ "OutLevels")
            }).add
        }
    }

    *new { |nsServer|
        ^super.newCopyArgs(nsServer).init
    }

    init { 
        numChannels = nsServer.options.outChannels;
    }

    addResponder { |levelMeters|
        meterSynth = Synth(
            \ns_serverOutMeter, 
            [\inBus, nsServer.server.outputBus],
            // RootNodes are cached, so this should not produce a new node:
            RootNode(nsServer.server), 
            \addToTail
        );

        responder = OSCFunc(
            { |msg|
                var peakRMS = msg[3..].clump(2);

                peakRMS.do { |peakR, i|
                    { levelMeters[i].value_(*peakR) }.defer
                }
            },
            ("/" ++ nsServer.server.name ++ "OutLevels").asSymbol,
            nsServer.server.addr, nil, [meterSynth.nodeID]
        )
    }

    freeResponder { |levelMeters|
        levelMeters.do { |meter| meter.value_(0, 0) };
        meterSynth.free;
        meterSynth = nil;
        responder.free;
        responder = nil;
    }
}
