NSFW {
    classvar instance;
    classvar win;
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
                vBounds.leftTop, vBounds.leftBottom, 
                NS_Style.bGroundDark, gradient
            );
        });

        serverList = ListView()
        .items_([]) // must be an empty array so I can add entries later, I think?
        .stringColor_(NS_Style.textLight)
        .selectedStringColor_(NS_Style.textDark)
        .hiliteColor_(NS_Style.highlight)
        .background_(NS_Style.transparent)
        .action_({ |lv| 
            currentServer = lv.items[lv.value];
            serverStack.index_(lv.value)
        });

        serverListView = View()
        .maxWidth_(90)
        .layout_(
            VLayout(
                NS_Button([
                    ["+ Matrix", NS_Style.textLight, NS_Style.bGroundDark]
                ])
                .addLeftClickAction({ this.newMatrixServerSetup }),
                NS_Button([
                    ["+ Timeline", NS_Style.textLight, NS_Style.bGroundDark]
                ])
                .addLeftClickAction({ /* TODO: timeline stuff */ }),
                serverList,
                NS_Button([
                    ["delete\nserver", NS_Style.red, NS_Style.bGroundDark]
                ])
                .fixedHeight_(40)
                .addLeftClickAction({ |but|
                    if(serverStack.count > 0,{
                        var index = serverList.value;
                        var serverString = serverList.items[index];
                        var size = serverStackArray.size;

                        serverString ?? 
                        { "server not booted".postln } !? 
                        { servers[serverString].free };

                        servers.put(serverString, nil);
                        serverList.items_(
                            serverList.items.reject({ |i| i == serverString })
                        );

                        serverStackArray.removeAt(index).view.remove;

                        serverStack = StackLayout(*serverStackArray);
                        serverStackView.layout_(serverStack);
                        
                    })
                })
            ).margins_(0)
        );

        serverStack = StackLayout().mode_(\stackOne);
        serverStackView = View()
        .background_(NS_Style.transparent)
        .layout_( serverStack );

        hubStack = StackLayout().mode_(\stackOne);

        win.layout_(
            VLayout(
                HLayout(
                    NS_Button([
                        ["servers", NS_Style.textLight, NS_Style.bGroundDark]
                    ])
                    .addLeftClickAction({ hubStack.index_(0) }),
                    NS_Button([
                        ["controllers", NS_Style.textLight, NS_Style.bGroundDark]
                    ])
                    .addLeftClickAction({ hubStack.index_(1) }),
                ),
                hubStack
                .add(
                    View().layout_(
                        HLayout(serverListView, serverStackView).margins_(0)
                    )
                )
                .add( OpenStageControl.drawView )
            )
        );

        win.onClose_({ this.cleanup; "add more to cleanupFunc".postln });
        win.front
    }

    *cleanup {
        Window.closeAll;
        NS_Controller.cleanupAll;
        thisProcess.recompile
    }

    // it seems I still need this...can I make it better?
    *numChans { |server|
        var srv = NSFW.servers[server];
        var numChans = srv !? { srv.options.numChans } ?? { 2 }; 
        ^numChans
    }

    /*===================== matrix interface =====================*/

    *newMatrixServerSetup {
        var numChanArray = [2,4,8,12,16,24], numChans = 2;
        var inChanArray  = [2,4,8,12,16,24], inChans = 2;
        var outChanArray = [2,4,8,12,16,24], outChans = 4;
        var blockArray   = (0..9).collect(2.pow(_).asInteger), blockSize = 64;
        var sRateArray   = [44100,48000,88200, 96000], sampleRate = 48000;
        var inDevArray   = ServerOptions.inDevices,  inDevice  = "default";
        var outDevArray  = ServerOptions.outDevices, outDevice = "default";

        var stringListTemplate = { |string, items, actionFunc, default|
            VLayout(
                StaticText().string_(string).align_(\center)
                .stringColor_(NS_Style.textDark),
                ListView()
                .items_(items)
                .action_(actionFunc)
                .stringColor_(NS_Style.textDark)
                .hiliteColor_(NS_Style.highlight)
                .background_(NS_Style.transparent)
                .selectedStringColor_(NS_Style.textDark)
                .value_(default ? 0)
            )
        };

        var serverName = ("nsfw_" ++ servers.size).asSymbol;
        // must increment the size of servers even if server is not booted:
        servers.put(serverName, "");

        serverList.items_(serverList.items ++ [serverName]);
        serverStackArray = serverStackArray.add(
            NS_ContainerView().layout_(
                VLayout(
                    StaticText().string_(serverName).align_(\center)
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
                    GridLayout.rows(
                        [
                            [
                                stringListTemplate.(
                                    "inDevice",
                                    inDevArray,
                                    { |lv| inDevice = lv.items[lv.value] }
                                ),
                                columns: 2 
                            ],
                            stringListTemplate.(
                                "inChans",
                                inChanArray,
                                { |lv| inChans = lv.items[lv.value] },
                                inChanArray.indexOf(inChans)
                            )
                        ],
                        [
                            [
                                stringListTemplate.(
                                    "outDevice",
                                    outDevArray,
                                    { |lv| outDevice = lv.items[lv.value] }
                                ),
                                columns: 2
                            ],
                            stringListTemplate.(
                                "numChans",
                                numChanArray,
                                { |lv| numChans = lv.items[lv.value] },
                                numChanArray.indexOf(numChans)
                            )
                        ],
                        [
                            stringListTemplate.(
                                "blockSize",
                                blockArray,
                                { |lv| blockSize = lv.items[lv.value] },
                                blockArray.indexOf(blockSize)
                            ),
                            stringListTemplate.(
                                "sampleRate",
                                sRateArray,
                                { |lv| sampleRate = lv.items[lv.value] },
                                sRateArray.indexOf(sampleRate)
                            ),
                            stringListTemplate.(
                                "outChans",        // add keys for ambisonics?
                                outChanArray,
                                { |lv| outChans = lv.items[lv.value] },
                                outChanArray.indexOf(outChans)
                            )
                        ]
                    ),
                    NS_Button([
                        ["boot server", NS_Style.green, NS_Style.bGroundDark]
                    ])
                    .addLeftClickAction({
                        var options = NS_ServerOptions(
                            numChans,
                            inChans, outChans, blockSize, 
                            sampleRate, inDevice, outDevice
                        );

                        if(numChans > outChans,{
                            "numChans > outChans; please adjust".warn;
                        },{
                            this.bootMatrixServer(serverName, options)
                        })
                    })
                )
            )
        );

        serverStack = StackLayout(*serverStackArray);
        serverStackView.layout_(serverStack)
    }

    *bootMatrixServer { |serverName, serverOptions|
        var cond = CondVar();
        var index = serverList.value;
        var serverView;

        fork{
            var nsServer = NS_MatrixServer(serverName, serverOptions, { cond.signalOne });

            servers.put(serverName, nsServer);

            cond.wait { nsServer.server.serverRunning };

            {
                serverView = NS_MatrixServerHubView(nsServer);
                serverStackArray.removeAt(index).remove;
                serverStackArray = serverStackArray.insert(index, serverView);

                serverStack = StackLayout(*serverStackArray);
                serverStackView.layout_(serverStack);
                serverStack.index_(index);
            }.defer
        }
    }

    /*===================== timeline interface =====================*/

    *newTimelineServerSetup {}

    *bootTimelineServer {}

}
