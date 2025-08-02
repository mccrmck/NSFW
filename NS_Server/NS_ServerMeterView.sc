NS_ServerOutMeterView : NS_Widget {

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var numChans = nsServer.options.outChannels;

        // NS_ContainerView doesn't work for some reason..
        view = UserView()
        .maxHeight_(
            NS_Style('viewMargins')[1] + // top margin
            20 + 2 + 20 +             // label + divider + button
            (numChans * 20) +         // NS_LevelMeter height
            NS_Style('viewMargins')[3]   // bottom margin
        )
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var rect = Rect(0,0,w,h);
            var rad = NS_Style('radius');

            Pen.fillColor_( NS_Style('highlight') );
            Pen.addRoundedRect(rect, rad, rad);
            Pen.fill;
        })
        .layout_(
            VLayout(
                StaticText()
                .string_("outputs")
                .align_(\center)
                .stringColor_(NS_Style('textDark') ),
                UserView()
                .fixedHeight_(2)
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var rect = Rect(0,0,w,h);
                    var rad = NS_Style('radius');

                    Pen.fillColor_( NS_Style('bGroundDark') );
                    Pen.addRoundedRect(rect, rad, rad);
                    Pen.fill;
                }),
                NS_Button([
                    ["startMeter", NS_Style('textLight'), NS_Style('bGroundDark')],
                    ["stopMeter", NS_Style('bGroundLight'), NS_Style('textDark')]
                ]).addLeftClickAction({ |b|
                    if(b.value == 1,{
                        nsServer.outMeter.startMetering;

                    },{
                        nsServer.outMeter.stopMetering
                    })
                }),
                VLayout(
                    *nsServer.outMeter.outLevelMeters
                )
            )
        );

        view.layout.spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'));
    }
}

NS_ServerOutMeter {
    var nsServer;
    var <outLevelMeters;
    var meterSynth, responder;

    *initClass {
        ServerBoot.add { |server|
            var numChans = NSFW.numChans;

            SynthDef(\ns_serverOutMeter,{
                var sig = In.ar(0, numChans);
                var trigFreq = 20;
                SendPeakRMS.kr(sig, trigFreq, 3, "/" ++ server.name ++ "OutLevels")
            }).add
        }
    }

    *new { |nsServer|
        ^super.newCopyArgs(nsServer).init
    }

    init {
        var numChans = nsServer.options.outChannels;
        outLevelMeters = numChans.collect({ |i| NS_LevelMeter(i) });

        responder = OSCFunc({ |msg|
            var peakRMS = msg[3..].clump(2);

            peakRMS.do({ |peakR, i|
                { outLevelMeters[i].value_(*peakR) }.defer
            })

        },("/" ++ nsServer.server.name ++ "OutLevels").asSymbol, nsServer.server.addr)
    }

    startMetering {
        meterSynth = Synth(\ns_serverOutMeter,[], RootNode(nsServer.server), \addToTail);
    }

    stopMetering {
        meterSynth.free;
        meterSynth = nil;
    }

}
