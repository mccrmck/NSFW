NS_ServerOutMeter {
    var nsServer;
    var <outLevelMeters;
    var meterSynth, responder;

    *initClass {
        ServerBoot.add { |server|
            var srv = NSFW.servers[server.name];
            var numOutChans = srv !? { srv.options.outChannels } ?? { 4 }; 

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
        var numOutChans = nsServer.options.outChannels;
        // this needs to move to the view...
        outLevelMeters = numOutChans.collect({ |i| NS_LevelMeter(i) }); 
    }

    startMetering {
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

                peakRMS.do({ |peakR, i|
                    { outLevelMeters[i].value_(*peakR) }.defer
                })
            },
            ("/" ++ nsServer.server.name ++ "OutLevels").asSymbol,
            nsServer.server.addr, nil, [meterSynth.nodeID]
        )
    }

    stopMetering {
        outLevelMeters.do({ |meter| meter.value_(0, 0) });
        meterSynth.free;
        meterSynth = nil;
        responder.free;
        responder = nil;
    }
}
