NS_MatrixServerHubView : NS_Widget {

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
                    NS_Style('viewMargins')[1] + // top margin
                    20 + 2 + 20 +                // label + divider + button
                    NS_Style('viewMargins')[3]   // bottom margin
                )
                .layout_(
                    VLayout(
                        StaticText().align_(\center).string_(nsServer.name)
                        .stringColor_( NS_Style('textDark') ),
                        NS_HDivider(),
                        HLayout(
                            NS_Button([
                                ["show", NS_Style('textLight'), NS_Style('bGroundDark')],
                                ["hide", NS_Style('textLight'), NS_Style('bGroundDark')]
                            ])
                            .addLeftClickAction({ |b|
                                serverWindow.win.visible_(b.value.asBoolean)
                            }),
                            NS_Button([
                                ["save", NS_Style('textLight'), NS_Style('bGroundDark')]
                            ])
                            .addLeftClickAction({
                                Dialog.savePanel(
                                    { |path| 
                                        var saveArray = nsServer.save; 
                                        saveArray.writeArchive(path);
                                        "% saved to: %".format(nsServer.name, path).postln;
                                    }, 
                                    nil,
                                    savePath
                                )
                            }),
                            NS_Button([
                                ["load", NS_Style('textLight'), NS_Style('bGroundDark')]
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
                    ).spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'))
                ),
                NS_ServerInputView(nsServer),
                NS_ServerOutMeterView(nsServer)
            ).spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'));
        )
    }
}
