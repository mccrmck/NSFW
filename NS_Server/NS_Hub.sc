NS_Hub {
    classvar instance;
    classvar win;
    classvar serverList, serverStackArray, serverStack;
    classvar serverListView, <serverStackView;
    classvar <servers, currentServer;

    *initClass {
        servers = Dictionary()
    }

    *new {
        ^instance ?? { ^super.new.init }
    }

    init {
        ^instance = this.class;
    }

    *makeWindow {
        var bounds = Window.availableBounds;
        var width = 400, height = 300;
        var gradient = Color.rand;

        win = Window(
            "NSFW",
            Rect(bounds.width - width, bounds.height - height, width, height)
        ).drawFunc_({
            var vBounds = win.view.bounds;
            Pen.addRect(vBounds);
            Pen.fillAxialGradient(
                vBounds.leftTop, vBounds.rightBottom, 
                NS_Style.bGroundDark, gradient
            );
        });

        serverList = ListView()
        .items_([])        // must be an empty array so I can add entries later
        .stringColor_(NS_Style.textLight)
        .background_(NS_Style.transparent)
        .action_({ |lv| 
            var val = lv.value;
            currentServer = lv.items[val];
            serverStack.index_(val)
        });

        serverListView = View()
        .maxWidth_(60)
        .layout_(
            VLayout(
                serverList,
                Button().states_([[
                    "delete\nServer",
                    NS_Style.muteRed,
                    NS_Style.bGroundDark
                ]])
                .action_({ |but|
                    if(serverStack.count > 0,{
                        var index = serverList.value;
                        var serverString = serverList.items[index];
                        var size = serverStackArray.size;

                        serverStackArray.removeAt(index).remove;

                        serverStack = StackLayout(*serverStackArray);
                        serverStackView.layout_(serverStack);
                        serverList.items_(
                            serverList.items.reject({ |i| i == serverString })
                        );

                        serverString ?? 
                        { "server not booted".postln } !? 
                        { servers[serverString].free };

                        servers.put(serverString, "");
                    })
                })
            ).spacing_(0).margins_(0)
        );

        serverStack = StackLayout().mode_(0);
        serverStackView = View()
        .background_(NS_Style.transparent)
        .layout_( serverStack );

        win.layout_(
            VLayout(
                HLayout(
                    Button()
                    .states_([[
                        "add Matrix",
                        NS_Style.textLight,
                        NS_Style.bGroundDark
                    ]])
                    .action_({
                        this.matrixServerSetup
                    }),
                    Button()
                    .states_([[
                        "add TimeLine",
                        NS_Style.textLight,
                        NS_Style.bGroundDark
                    ]])
                    .action_({
                        // TODO: timeline stuff
                    }),
                    Button()
                    .states_([[
                        "module List",
                        NS_Style.textLight,
                        NS_Style.bGroundDark
                    ]])
                ),
                HLayout(
                    [serverListView,  s:  1],
                    [serverStackView, s: 10],
                )
            )
        );

        win.front
    }

    // matrix interface
    *matrixServerSetup {
        var inChans, outChans;
        var blockSize, sampleRate;
        var inDevice, outDevice; 

        var stTemplate = { |string|
            StaticText()
            .string_(string)
            .align_(\center)
            .stringColor_(NS_Style.textDark)
        };
        var listTemplate = { |items, actionFunc|
            ListView()
            .stringColor_(NS_Style.textDark)
            .background_(NS_Style.transparent)
            .items_(items)
            .action_(actionFunc)
        };

        var serverName = "nsfw_" ++ servers.size;
        servers.put(serverName, "");

        serverList.items_(serverList.items ++ [serverName]);
        serverStackArray = serverStackArray.add(
            UserView().drawFunc_({ |v|
                var w = v.bounds.width;
                var h = v.bounds.height;
                var rect = Rect(0,0,w,h);

                Pen.fillColor_( NS_Style.highlight );
                Pen.addRoundedRect(rect, NS_Style.radius, NS_Style.radius );
                Pen.fill;
            })
            .layout_(
                VLayout(
                    stTemplate.(serverName),
                    GridLayout.rows(
                        [[
                            VLayout(
                                stTemplate.("inDevice"),
                                listTemplate.( 
                                    ServerOptions.inDevices,
                                    { |lv|
                                        inDevice = ServerOptions.inDevices[lv.value]
                                    }
                                )
                            ),
                            columns: 2 ],
                            VLayout(
                                stTemplate.("inChans"),
                                listTemplate.( 
                                    [2,4,8,12,16,24],
                                    { |lv|
                                        inChans = lv.items[lv.value]
                                    }
                                )
                            )
                        ],
                        [[
                            VLayout(
                                stTemplate.("outDevice"),
                                listTemplate.( 
                                    ServerOptions.outDevices,
                                    { |lv|
                                        outDevice = ServerOptions.outDevices[lv.value]
                                    }
                                )
                            ),
                            columns: 2],
                            VLayout(
                                stTemplate.("numChans"),
                                listTemplate.(
                                    [2,4,8,12,16,24],
                                    { |lv|
                                        // numChans goes here
                                    }
                                ).value_(0)
                            )
                        ],
                        [
                            VLayout(
                                stTemplate.("blockSize"),
                                listTemplate.(
                                    (0..9).collect({ |i|
                                        (2 ** i).asInteger.asString
                                    }),
                                    { |lv|
                                        blockSize = lv.items[lv.value]
                                    }
                                )
                                .value_(6)
                            ),
                            VLayout(
                                stTemplate.("sampleRate"),
                                listTemplate.(
                                    ["44100","48000","88200", "96000"],
                                    { |lv|
                                        sampleRate = lv.items[lv.value]
                                    }
                                )
                                .value_(1)
                            ),
                            VLayout(
                                stTemplate.("outChans"),   // add keys for ambisonics?
                                listTemplate.( 
                                    [2,4,8,12,16,24],
                                    { |lv|
                                        outChans = lv.items[lv.value]
                                    }
                                )
                            )
                        ]
                    ).setRowStretch(0,2),
                    Button().states_([[
                        "boot Server",
                        NS_Style.playGreen,
                        NS_Style.bGroundDark
                    ]])
                    .action_({
                        var options = NS_ServerOptions(
                            inChans, outChans, blockSize, 
                            sampleRate, inDevice, outDevice
                        );

                        this.bootMatrixServer(serverName, options)
                    })
                )
            )
        );

        serverStack = StackLayout(*serverStackArray);
        serverStackView.layout_(serverStack)
    }

    *bootMatrixServer { |serverName, serverOptions|
        var index = serverList.value;

        serverStackArray.removeAt(index).remove;
        serverStackArray = serverStackArray.insert(index, StaticText().string_(serverName));

        serverStack = StackLayout(*serverStackArray);
        serverStackView.layout_(serverStack);
        serverStack.index_(index);
        servers.put(serverName, NS_Server(serverName, serverOptions));
    }

    // timeline interface
}
