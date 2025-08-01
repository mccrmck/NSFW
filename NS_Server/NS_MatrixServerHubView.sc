NS_MatrixServerHubView : NS_Widget {
    var <view;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var savePath = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "saved/servers/";
        var serverWindow = NS_MatrixServerWindow(nsServer);

        view = View().layout_(
            VLayout(
                NS_ContainerView()
                .maxHeight_(
                    NS_Style.viewMargins[1] + // top margin
                    20 + 2 + 20 +             // label + divider + button
                    NS_Style.viewMargins[3]   // bottom margin
                )
                .layout_(
                    VLayout(
                        StaticText().align_(\center).string_(nsServer.name)
                        .stringColor_(NS_Style.textDark),
                        UserView()
                        .fixedHeight_(2)
                        .drawFunc_({ |v|
                            var w = v.bounds.width;
                            var h = v.bounds.height;
                            var rect = Rect(0,0,w,h);
                            var rad = NS_Style.radius;

                            Pen.fillColor_( NS_Style.bGroundDark );
                            Pen.addRoundedRect(rect, rad, rad);
                            Pen.fill;
                        }),
                        HLayout(
                            NS_Button([
                                ["show", NS_Style.textLight, NS_Style.bGroundDark],
                                ["hide", NS_Style.bGroundLight, NS_Style.textDark]
                            ])
                            .addLeftClickAction({ |b|
                                serverWindow.win.visible_(b.value.asBoolean)
                            }),
                            NS_Button([
                                ["save Server", NS_Style.textLight, NS_Style.bGroundDark]
                            ])
                            .addLeftClickAction({
                                Dialog.savePanel(
                                    { |path| 
                                        var saveArray = nsServer.save; 
                                        "% saved to %".format(nsServer.name, path).postln;
                                        saveArray.writeArchive(path);
                                    }, 
                                    nil,
                                )
                            }),
                            NS_Button([
                                ["load Server", NS_Style.textLight, NS_Style.bGroundDark]
                            ])
                            .addLeftClickAction({
                                Dialog.openPanel(
                                    { |path| 
                                        var loadArray = Object.readArchive(path); 
                                        nsServer.load(loadArray);
                                    }, 
                                    nil,
                                    false,
                                    savePath
                                )
                            })
                        )
                    ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
                ),
                NS_ContainerView().layout_(
                    VLayout(
                        StaticText()
                        .string_("inputs")
                        .align_(\center)
                        .stringColor_(NS_Style.textDark),
                        UserView()
                        .fixedHeight_(2)
                        .drawFunc_({ |v|
                            var w = v.bounds.width;
                            var h = v.bounds.height;
                            var rect = Rect(0,0,w,h);
                            var rad = NS_Style.radius;

                            Pen.fillColor_( NS_Style.bGroundDark );
                            Pen.addRoundedRect(rect, rad, rad);
                            Pen.fill;
                        }),
                        VLayout(
                            *nsServer.inputs.collect({ |input|
                                NS_ChannelStripInView(input)
                            })
                        ).margins_(0).spacing_(0)
                    ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
                ),
                NS_ServerOutMeterView(nsServer)
            ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins);
        )
    }
}
