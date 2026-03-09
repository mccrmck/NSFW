NS_ServerWindow {
    var <win;
    var <stripViews, <outStripViews, <swapGridView;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var savePath = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "saved/servers/";

        var gradient = Color.rand;
        var layout;
        nsServer.window = this;

        win = Window(nsServer.name.asString);
        win.drawFunc = {
            var vBounds = win.view.bounds;
            Pen.addRect(vBounds);
            Pen.fillAxialGradient(
                vBounds.leftTop,
                vBounds.leftBottom,
                NS_Style('bGroundDark'),
                gradient
            );
        };

        stripViews = nsServer.strips.deepCollect(2,{ |strip|
            NS_ChannelStripView(strip)
        }).flop; // groups strips as x:0, x:1, x:2, x:3 

        outStripViews = nsServer.outMixer.collect({ |strip|
            NS_ChannelStripOutView(strip)
        });

        swapGridView = NS_SwapGridView(nsServer.swapGrid);

        win.layout_( 
            HLayout(
                VLayout(
                    NS_ServerInputView(nsServer),
                    swapGridView,
                ),
                VLayout(
                    NS_ContainerView()
                    .layout_(
                        HLayout(
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
                            }),
                        )
                    ),
                    HLayout(
                        NS_ScrollView(510, 1380).layout_( *stripViews[0] ),
                        NS_ScrollView(510, 1380).layout_( *stripViews[1] ),
                        NS_ScrollView(510, 1380).layout_( *stripViews[2] ),
                        NS_ScrollView(510, 1380).layout_( *stripViews[3] ),
                    ),
                    VLayout(
                        StaticText().string_("outputs").align_(\center),
                        NS_HDivider(),
                        HLayout( *outStripViews )
                    ),
                ),
                VLayout(
                    nil,
                    NS_ServerOutMeterView(nsServer)
                )
            )
        );

        win.layout.spacing_(NS_Style('windowSpacing')).margins_(NS_Style('windowMargins'));
        win.onClose_({ NSFW.cleanup; "add more to cleanupFunc".postln });
        win.front;
    }

    free { win.close }
}
