NS_ServerWindow {
    var <win;
    var <stripViews, <outStripViews, <swapGridView;
    var saveButton, loadButton;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var savePath = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "saved/servers/";

        var gradient = Color(105/255, 50/255, 161/255);
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

        saveButton = NS_Button([
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
        });

        loadButton = NS_Button([
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
        });

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
                    NS_ContainerView()
                    .maxHeight_(75)
                    .layout_(
                        VLayout(saveButton, loadButton, NS_Button()) // controllers
                        .nsMarginsSpacing('view')
                    ),
                    NS_ServerInputView(nsServer),
                    swapGridView,
                ).nsMarginsSpacing('view'),
                VLayout(
                    HLayout(
                        *stripViews.collect { |sv|
                            NS_ScrollView(510, 1440).layout_( *sv )
                        }
                    ).nsMarginsSpacing('view'),
                    NS_ContainerView().layout_(
                        VLayout(
                            StaticText().string_("outputs").align_(\center),
                            NS_HDivider(),
                            HLayout( *outStripViews ).nsMarginsSpacing(0)
                        ).nsMarginsSpacing(0)
                    )
                ).nsMarginsSpacing('view'),
                VLayout(
                    nil,
                    NS_ServerOutMeterView(nsServer)
                ).nsMarginsSpacing('view')
            ).nsMarginsSpacing('window')
        );

        win.onClose_({ NSFW.cleanUp; "add more to cleanupFunc".postln });
        win.front;
    }

    free { win.close }
}
