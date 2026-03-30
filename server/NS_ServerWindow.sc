NS_ServerWindow {
    var <win;
    var <stripViews, <outStripViews, <swapGridView;
    var saveButton, loadButton;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var savePath = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "saved/servers/";

        nsServer.window = this;

        win = Window(nsServer.name.asString);
        win.drawFunc = {
            var mainCol = NS_Style('mainColor');
            var bgCol = NS_Style('bGroundDark');
            var v = win.view.bounds;
            var w = v.width;
            var h = v.height;

            Pen.addRect(Rect(v.left, v.top, w / 2, h / 2) );
            Pen.fillAxialGradient(v.leftTop, v.rightBottom, bgCol, mainCol);
            Pen.addRect(Rect(v.left, v.top + (h / 2), w / 2, h / 2));
            Pen.fillAxialGradient(v.leftBottom, v.rightTop, bgCol, mainCol);

            Pen.addRect(Rect(v.left + (w / 2), v.top, w / 2, h / 2));
            Pen.fillAxialGradient(v.rightTop, v.leftBottom, bgCol, mainCol);
            Pen.addRect(Rect(v.left + (w / 2), v.top + (h / 2), w / 2, h / 2));
            Pen.fillAxialGradient(v.rightBottom, v.leftTop, bgCol, mainCol);
        };

        saveButton = NS_Button([
            ["save", NS_Style('textLight'), NS_Style('bGroundDark')]
        ])
        .addLeftClickAction({
            Dialog.savePanel(
                { |path| 
                    nsServer.save.writeArchive(path);
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
                { |path| nsServer.load(Object.readArchive(path)) }, 
                nil, false, savePath
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
                    NS_ContainerView().layout_(
                        VLayout(
                            saveButton, 
                            loadButton, 
                            NS_Button(["config"]),
                            NS_Button(["controllers"])
                        ).nsMarginsSpacing('view')
                    ),
                    NS_ServerInputView(nsServer),
                    swapGridView,
                ).nsMarginsSpacing('view'),
                VLayout(
                    HLayout(
                        *stripViews.collect { |sv|
                            NS_ScrollView(510, 1500).layout_( *sv )
                        }
                    ).nsMarginsSpacing('inner'),
                    NS_ContainerView().layout_(
                        VLayout(
                            NS_Header("outputs"),
                            NS_HDivider(),
                            HLayout( *outStripViews ).nsMarginsSpacing('inner'),
                            NS_ServerOutMeterView(nsServer)
                        ).nsMarginsSpacing('view')
                    )
                ).nsMarginsSpacing('view'),
            ).nsMarginsSpacing('window')
        );

        win.onClose_({ NSFW.cleanUp; "add more to cleanupFunc".postln });
        win.front;
    }

    free { win.close }
}
