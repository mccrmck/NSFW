NS_MatrixServerHubView : NS_Widget {
    var <serverWindow;
    var <view;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var savePath = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "saved/servers/";
        serverWindow = NS_MatrixServerWindow(nsServer);

        view = View().layout_(
            VLayout(
                NS_ContainerView()
                .fixedHeight_(75)
                .layout_(
                    VLayout(
                        StaticText().align_(\center).string_(nsServer.name)
                        .stringColor_(NS_Style.textDark),
                        HLayout(
                            Button().states_([
                                ["show", NS_Style.textLight, NS_Style.bGroundDark],
                                ["hide", NS_Style.bGroundLight, NS_Style.textDark]
                            ])
                            .action_({ |but|
                                serverWindow.win.visible_(but.value.asBoolean)
                            }),
                            Button().states_([
                                ["save Server", NS_Style.textLight, NS_Style.bGroundDark]
                            ])
                            .action_({
                                Dialog.savePanel(
                                    { |path| 
                                        var saveArray = nsServer.save; 
                                        "% saved to %".format(nsServer.name, path).postln;
                                        saveArray.writeArchive(path);
                                    }, 
                                    nil,
                                    savePath
                                )
                            }),
                            Button().states_([
                                ["load Server", NS_Style.textLight, NS_Style.bGroundDark]
                            ])
                            .action_({
                                Dialog.openPanel(
                                    { |path| 
                                        var loadArray = Object.readArchive(path); 
                                        nsServer.load(loadArray);
                                    }, 
                                    nil,
                                    false,
                                    savePath
                                )
                            }),
                        )
                    )
                ),
                NS_ContainerView().layout_(
                    VLayout(
                        StaticText()
                        .string_("inputs")
                        .align_(\center)
                        .stringColor_(NS_Style.textDark),
                        VLayout(
                            *nsServer.inputs.collect({ |input|
                                NS_ChannelStripInView(input)
                            })
                        ).margins_(0).spacing_(0)
                    )
                ),
                NS_ServerOutMeterView(nsServer)
            ).spacing_(NS_Style.windowSpacing).margins_(NS_Style.windowMargins);
        );
    }
}
