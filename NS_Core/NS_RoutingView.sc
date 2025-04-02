NS_MatrixRoutingView {
    var <view;

    *new { |strip|
        ^super.new.init(strip)
    }

    init { |strip|
        var server = NSFW.servers[strip.stripGroup.server.name];
        var thisPage = strip.pageIndex;
        var thisStrip = strip.stripIndex;

        var stripButtons = NS_MatrixServer.numPages.collect({ |pageIndex|
            NS_MatrixServer.numStrips.collect({ |stripIndex|
                var string = "%:%".format(pageIndex, stripIndex);
                // booleans to exclude strips
                var pageBool  = pageIndex < thisPage;  
                var stripBool = stripIndex == thisStrip;
                var samePageBool = (pageIndex == thisPage) and: (stripIndex <= thisStrip);

                if(pageBool or: stripBool or: samePageBool,{
                    Button()
                    .enabled_(false)
                    .maxWidth_(30)
                    .font_(Font(*NS_Style.smallFont))
                    .states_([[string, NS_Style.textDark, NS_Style.darklight]])
                },{
                    Button()
                    .maxWidth_(30)
                    .font_(Font(*NS_Style.smallFont))
                    .states_([
                        [
                            string,
                            NS_Style.textDark,
                            NS_Style.bGroundLight
                        ],
                        [
                            string,
                            NS_Style.textLight,
                            NS_Style.bGroundDark
                        ]
                    ])
                    .action_({ |but|
                        var outStrip = server.strips[pageIndex][stripIndex];

                        if(but.value == 0,{
                            strip.removeSend(outStrip.stripBus);
                            "%:% no longer sending to %:%".format(
                                this.pageIndex, this.stripIndex,
                                outStrip.pageIndex, outStrip.stripIndex,
                            ).postln
                        },{
                            strip.addSend(outStrip.stripBus);
                            "%:% sending to %:%".format(
                                strip.pageIndex, strip.stripIndex,
                                outStrip.pageIndex, outStrip.stripIndex,
                            ).postln
                        })
                    })
                })               
            })
        });

        var outButtons = server.outMixer.collect({ |outStrip, outIndex|
            var string = "%:%".format(outStrip.pageIndex, outStrip.stripIndex);

            Button()
            .maxWidth_(30)
            .font_(Font(*NS_Style.smallFont))
            .states_([
                [
                    string,
                    NS_Style.textDark,
                    NS_Style.bGroundLight
                ],
                [
                    string,
                    NS_Style.textLight,
                    NS_Style.bGroundDark
                ]
            ])
            .action_({ |but|
                if(but.value == 0,{
                    strip.removeSend(outStrip.stripBus);
                    "%:% no longer sending to %:%".format(
                        this.pageIndex, this.stripIndex,
                        outStrip.pageIndex, outStrip.stripIndex,
                    ).postln
                },{
                    strip.addSend(outStrip.stripBus);
                    "%:% sending to %:%".format(
                        strip.pageIndex, strip.stripIndex,
                        outStrip.pageIndex, outStrip.stripIndex,
                    ).postln
                })
            })
        });

        view = View()
        .background_(NS_Style.bGroundDark)
        .layout_(
            VLayout(
                StaticText()
                .string_("strips")
                .align_(\center)
                .stringColor_(NS_Style.textLight),
                GridLayout.rows( *stripButtons ),
                StaticText()
                .string_("outputs")
                .align_(\center)
                .stringColor_(NS_Style.textLight),
                HLayout( *outButtons )
            ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
        )
    }

    asView { ^view }
}

NS_MatrixRoutingOutView {
    var <view;

    *new { |strip|
        ^super.new.init(strip)
    }

    init { |strip|
        view = View()
        .background_(NS_Style.bGroundDark)
        .layout_(
            VLayout(
                StaticText()
                .string_("strips")
                .align_(\center)
                .stringColor_(NS_Style.textLight),
                GridLayout.rows(
                    *(24.collect({ |i|
                        Button()
                        .maxWidth_(30)
                        .font_(Font(*NS_Style.smallFont))
                        .states_([
                            [
                                "%:%".format((i/4).floor.asInteger, i % 4),
                                NS_Style.textDark,
                                NS_Style.bGroundLight
                            ],
                            [
                                "%:%".format((i/4).floor.asInteger, i % 4),
                                NS_Style.textLight,
                                NS_Style.bGroundDark
                            ]
                        ])
                        .action_({ |but|
                            var pageIndex = (i/4).floor.asInteger;
                            var stripIndex = i % 4;
                            var server = NSFW.servers[strip.stripGroup.server.name];
                            var outBus = server.strips[pageIndex][stripIndex].stripBus;

                            // needs logic to exclude off-limits busses

                            if(but.value == 0,{
                                strip.removeSend(outBus)
                            },{
                                strip.addSend(outBus)
                            })
                            
                        })
                    }).clump(4))
                )
            ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
        )
    }

    asView { ^view }
}
