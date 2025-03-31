NSFW {
    classvar instance;
    classvar win, moduleList;
    classvar serverList, serverStackArray, serverStack;
    classvar serverListView, <serverStackView;
    classvar <servers, <currentServer; // for future multi-server setups 

    classvar controllerView; // not so fancy atm, only o-s-c implememted

    classvar hubStack;

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

        moduleList = NS_ModuleList();

        serverList = ListView()
        .items_([])        // must be an empty array so I can add entries later
        .stringColor_(NS_Style.textLight)
        .selectedStringColor_(NS_Style.textDark)
        .hiliteColor_(NS_Style.highlight)
        .background_(NS_Style.transparent)
        .action_({ |lv| 
            var val = lv.value;
            currentServer = lv.items[val];
            serverStack.index_(val)
        });

        serverListView = View()
        .maxWidth_(90)
        .layout_(
            VLayout(
                Button()
                .states_([[
                    "+ Matrix",
                    NS_Style.textLight,
                    NS_Style.bGroundDark
                ]])
                .action_({ this.newMatrixServerSetup }),
                Button()
                .states_([[
                    "+ TimeLine",
                    NS_Style.textLight,
                    NS_Style.bGroundDark
                ]])
                .action_({
                    // TODO: timeline stuff
                }),
                serverList,
                Button().states_([[
                    "delete\nserver",
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

                        servers.put(serverString, nil);
                    })
                })
            ).margins_(0)
        );

        serverStack = StackLayout().mode_(0);
        serverStackView = View()
        .background_(NS_Style.transparent)
        .layout_( serverStack );

        hubStack = StackLayout().mode_(0);

        win.layout_(
            VLayout(
                HLayout(
                    Button().states_([["servers"]])
                    .action_({ hubStack.index_(0) }),
                    Button().states_([["controllers"]])
                    .action_({ hubStack.index_(1) }),
                    Button().states_([["moduleList"]])
                    .action_({ moduleList.toggleVisible }),
                ),
                hubStack
                .add(
                    View().layout_(
                        HLayout(
                            serverListView,
                            serverStackView
                        ).margins_(0)
                    )
                )
                .add(
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
                            Button().states_([[
                                "boot o-s-c",
                                NS_Style.textLight,
                                NS_Style.bGroundDark
                            ]]),
                            Button().states_([
                                [
                                    "show o-s-c window",
                                    NS_Style.textLight,
                                    NS_Style.bGroundDark
                                ],[
                                    "hide o-s-c window",
                                    NS_Style.textLight,
                                    NS_Style.bGroundDark
                                ]
                            ])
                            .action_({ |but|
                                var val = but.value;
                                case
                                { val == 1 }{ OpenStageControl.makeWindow }
                                { val == 0 }{ OpenStageControl.closeWindow }
                            }),
                        )
                    )
                )
            )
        );

        win.onClose({ this.cleanup });
        win.front
    }

    *cleanup {
        Window.closeAll;
       // controllers.do(_.cleanup);
        thisProcess.recompile
    }
    
    // this is kind of hacky, can I do better?
    *numChans { |server|
        var srv = NSFW.servers[server];
        var numChans = srv !? { srv.options.numChans } ?? { 2 }; 
        ^numChans
    }

    /*===================== server interface =====================*/

    *savePanel { |serverName|
        var savePath = PathName(
            NSFW.filenameSymbol.asString
        ).pathOnly +/+ "saved/servers/";

        ^UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var rect = Rect(0,0,w,h);

            Pen.fillColor_( NS_Style.highlight );
            Pen.addRoundedRect(rect, NS_Style.radius, NS_Style.radius);
            Pen.fill;
        })
        .layout_(
            VLayout(
                StaticText()
                .string_(serverName)
                .align_(\center)
                .stringColor_(NS_Style.textDark),
                HLayout(
                    
                    Button()
                    .states_([[
                        "save Server",
                        NS_Style.textLight,
                        NS_Style.bGroundDark
                    ]])
                    .action_({
                        Dialog.savePanel(
                            { |path| 
                                var saveArray = servers[serverName].save; 
                                "% saved to %".format(serverName, path).postln;
                                saveArray.writeArchive(path);
                            }, 
                            nil,
                            savePath
                        )
                    }),
                    Button()
                    .states_([[
                        "load Server",
                        NS_Style.textLight,
                        NS_Style.bGroundDark
                    ]])
                    .action_({
                        Dialog.openPanel(
                            { |path| 
                                var loadArray = Object.readArchive(path); 
                                servers[serverName].load(loadArray);
                            }, 
                            nil,
                            false,
                            savePath
                        )
                    }),
                )
            )
        )
    }

    /*===================== matrix interface =====================*/

    *newMatrixServerSetup {
        var numChans;
        var inChans, outChans, blockSize, sampleRate, inDevice, outDevice; 

        var stTemplate = { |string|
            StaticText()
            .string_(string)
            .align_(\center)
            .stringColor_(NS_Style.textDark)
        };

        var listTemplate = { |items, actionFunc|
            ListView()
            .stringColor_(NS_Style.textDark)
            .selectedStringColor_(NS_Style.textDark)
            .hiliteColor_(NS_Style.highlight)
            .background_(NS_Style.transparent)
            .items_(items)
            .action_(actionFunc)
        };

        var serverName = ("nsfw_" ++ servers.size).asSymbol;
        // must increment the size of servers even if server is not booted:
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
                                       numChans = lv.items[lv.value] 
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
                                        outChans = lv.items[lv.value];
                                    }
                                )
                            )
                        ]
                    ),
                    Button().states_([[
                        "boot server",
                        NS_Style.playGreen,
                        NS_Style.bGroundDark
                    ]])
                    .action_({
                        var options = NS_ServerOptions(
                            numChans,
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
        var serverView;

        servers.put(serverName, NS_MatrixServer(serverName, serverOptions));

        serverView = this.drawMatrixServerHub(serverName, serverOptions);
        
        serverStackArray.removeAt(index).remove;
        serverStackArray = serverStackArray.insert(index, serverView);

        serverStack = StackLayout(*serverStackArray);
        serverStackView.layout_(serverStack);
        serverStack.index_(index);
    }

    *drawMatrixServerHub { |serverName, serverOptions|

        ^View().layout_(
            VLayout(
                this.savePanel(serverName),
                UserView()
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var rect = Rect(0,0,w,h);
                    var rad = NS_Style.radius;

                    Pen.fillColor_( NS_Style.highlight );
                    Pen.addRoundedRect(rect, rad, rad);
                    Pen.fill;
                })
                .layout_(
                    VLayout(
                        StaticText()
                        .string_("inputs")
                        .align_(\center)
                        .stringColor_(NS_Style.textDark),
                        HLayout(
                            Button()
                            .states_([[ 
                                "add input",
                                NS_Style.textLight,
                                NS_Style.bGroundDark
                            ]])
                            .action_({ }),
                            Button()
                            .states_([[ 
                                "remove input",
                                NS_Style.textLight,
                                NS_Style.bGroundDark
                            ]])
                            .action_({ })
                        ),
                        NS_LevelMeter(0),
                        // NS_ChannelStripView(NS_ChannelStrip1(Group(), 3)),
                    )
                ),
                NS_ServerOutMeter(serverOptions.outChannels)
            ).spacing_(NS_Style.windowSpacing).margins_(NS_Style.windowMargins);
        )
    }

    /*===================== timeline interface =====================*/


    *newTimelineServerSetup {}

    *bootTimelineServer {}

}
