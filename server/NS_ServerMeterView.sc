NS_ServerOutMeterView : NS_Widget {

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var numOutChans = nsServer.options.outChannels;

        var meterStack = if(numOutChans > 16,{
            GridLayout.columns( nsServer.outMeter.outLevelMeters.clump(numOutChans / 2) )
        },{
            VLayout( *nsServer.outMeter.outLevelMeters )
        });

        view = NS_ContainerView()
        .maxHeight_(
            NS_Style('viewMargins')[1] + // top margin
            20 + 2 + 20 +                // label + divider + button
            (numOutChans * (20 + 2)) +   // NS_LevelMeter height
            NS_Style('viewMargins')[3]   // bottom margin
        )
        .layout_(
            VLayout(
                StaticText()
                .string_("outputs")
                .align_(\center)
                .stringColor_( NS_Style('textDark') ),
                NS_HDivider(),
                NS_Button([
                    ["startMeter", NS_Style('textLight'), NS_Style('bGroundDark')],
                    ["stopMeter", NS_Style('bGroundLight'), NS_Style('textDark')]
                ])
                .addLeftClickAction({ |b|
                    if(b.value == 1,{
                        nsServer.outMeter.startMetering;
                    },{
                        nsServer.outMeter.stopMetering
                    })
                }),
                meterStack
            ).spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'));
        )
    }
}

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
        outLevelMeters = numOutChans.collect({ |i| NS_LevelMeter(i) });
    }

    startMetering {
        meterSynth = Synth(
            \ns_serverOutMeter, 
            [\inBus, nsServer.server.outputBus],
            RootNode(nsServer.server),
            \addToTail
        );

        responder = OSCFunc({ |msg|
            var peakRMS = msg[3..].clump(2);

            peakRMS.do({ |peakR, i|
                { outLevelMeters[i].value_(*peakR) }.defer
            })

        },("/" ++ nsServer.server.name ++ "OutLevels").asSymbol, nsServer.server.addr, nil, [meterSynth.nodeID])
    }

    stopMetering {
        outLevelMeters.do({ |meter| meter.value_(0, 0) });
        meterSynth.free;
        meterSynth = nil;
        responder.free;
        responder = nil;
    }
}
