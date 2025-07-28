OpenStageControl : NS_Controller {
    classvar <connected = false, <loaded = false;
    classvar <netAddr, <pid;
    classvar guiLayerSwitch;
    classvar <strips,      <stripFaders, <>stripWidgets;
    classvar <mixerStrips, <mixerFaders, <>mixerStripWidgets;

    // gotta check if the port is available and no other o-s-c processes are running;
    // if they are, kill 'em and boot 
    *connect {
        var ip = "localhost", port = 8080; 
        var path = "NSFW.json".resolveRelative;
        var unixString = "node /Applications/open-stage-control.app/Contents/Resources/app/" + 
        "--send %:%".format(ip, NetAddr.localAddr.port) +
        "--custom-module '%'".format( "nsfwModule.js".resolveRelative ) +
        "--load '%'".format( path );
        
        this.makeInterface( path );

        netAddr = NetAddr(ip, port);
        pid = unixString.unixCmd;
        CmdPeriod.add({ this.cleanup });
        
        OSCFunc({ |msg|
            this.refresh;
            // is this a hack, or is it brilliant?
            netAddr.sendMsg(
                "/EDIT",
                "%".format( guiLayerSwitch.id ),
                "{\"onValue\": \"set(\\\"root\\\",value)\", \"bypass\": true }"
            );
        },'/nsfwGuiLoaded');

        pid !? { connected = true };
    }

    *cleanup {
        pid !? { 
            if(pid.pidRunning, {
                "kill %".format(pid).unixCmd; 
                "bye-bye o-s-c".postln
            })
        };
        connected = false;
    }

    *drawView {
        var webView = WebView();
        var reloadAttempts = 0;
        ^NS_ContainerView().layout_(
            VLayout(
                Button()
                .states_([
                    ["boot o-s-c", NS_Style.textLight, NS_Style.bGroundDark],
                    ["close o-s-c", NS_Style.textLight, NS_Style.bGroundDark]
                ])
                .action_({ |but|
                    if(but.value == 1,{
                        fork{
                            this.connect;
                            { 
                                webView
                                .url_( "%:%".format(netAddr.ip, netAddr.port) )
                                .onLoadFailed_({ |webView|

                                    while {reloadAttempts < 20} { 
                                        webView.reload;
                                        reloadAttempts = reloadAttempts + 1;

                                        "make this better".postln 
                                    }
                                })
                            }.defer
                        }
                    },{
                        this.cleanup
                    })
                }),
                webView
            )
        )
    }

    // would be great to draw the UI upon instantiating a new client
    // this draws all the widgets but does not update their values...
    // do I send *every* control value on refresh?!?!
    *refresh { 
        //stripWidgets.do({})

        mixerStripWidgets.do({ |widgetArray, stripIndex|
            var id = mixerStrips[stripIndex].id;
            this.prRefreshStrip(widgetArray, id)
        });
    }

    *addModuleFragment { |pageIndex, stripIndex, slotIndex, moduleClass|
        this.prUpdateStrip(pageIndex, stripIndex, slotIndex, moduleClass.oscFragment)
    }

    *removeModuleFragment { |pageIndex, stripIndex, slotIndex|
        this.prUpdateStrip(pageIndex, stripIndex, slotIndex, nil)
    }

    *prUpdateStrip { |pageIndex, stripIndex, slotIndex, moduleOrNil|
        var stripId, widgetArray;

        if(pageIndex == $o,{
            stripId     = mixerStrips[stripIndex.asInteger].id;
            widgetArray = mixerStripWidgets[stripIndex.asInteger];
        },{
            stripId     = strips[stripIndex.asInteger].tabArray[pageIndex].id;
            widgetArray = stripWidgets[stripIndex][pageIndex];
        });

        widgetArray[slotIndex] = moduleOrNil;
        this.prRefreshStrip(widgetArray, stripId)
    }

    *prRefreshStrip { |widgetArray, stripId|
        widgetArray = widgetArray.select({ |w| w.notNil });
        widgetArray = "%".ccatList("%"!(widgetArray.size - 1)).format(*widgetArray);
        netAddr.sendMsg(
            "/EDIT","%".format(stripId), "{\"widgets\": [%]}".format(widgetArray)
        )
    }

    *switchStripPage { |pageIndex, stripIndex|
        var stripId    = this.strips[stripIndex].id;
        var stripCtlId = this.stripFaders[stripIndex].id;
        this.netAddr.sendBundle(nil,
            ["/%".format(stripId),    pageIndex],
            ["/%".format(stripCtlId), pageIndex]
        );
    }

    *makeInterface { |path|
        var swapGrid, controlArray;
        var numIns        = 8; // 8 inputs...for now
        var numPages      = NS_MatrixServer.numPages;
        var numStrips     = NS_MatrixServer.numStrips;
        var numOutStrips  = 4; // 4 outputs...for now
        var faderMute     = {
            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height:"20%")
            ])
        };

        guiLayerSwitch    = OpenStageSwitch(3, 3, 'tap', height: "10%");
        swapGrid          = { OpenStageSwitch(numPages, 1, 'slide') } ! numStrips;

        stripFaders       = { OpenStagePanel(tabArray: faderMute ! numPages) } ! numStrips;
        mixerFaders       = faderMute ! numOutStrips; 

        controlArray      = [
            guiLayerSwitch,
            OpenStagePanel(swapGrid,    columns: numStrips),
            OpenStagePanel(stripFaders, columns: numStrips),
            OpenStagePanel(mixerFaders, columns: numOutStrips)
        ];

        strips            = { OpenStagePanel(tabArray: { OpenStagePanel() } ! numPages) } ! numStrips;
        mixerStrips       = { OpenStagePanel() } ! numOutStrips;
        stripWidgets      = { {List.newClear(6)} ! numPages } ! numStrips; // hardcoded to 6 for now
        mixerStripWidgets = { List.newClear(4) } ! numOutStrips;           // hardcoded to 4 for now

        OpenStageRoot(tabArray: [
            // panel 0 - strip modules
            OpenStagePanel([ 
                OpenStagePanel(strips, columns: numStrips),
                OpenStagePanel(controlArray, width: "20%") 
            ], columns: 2),
            // panel 1 - mixer modules
            OpenStagePanel([
                OpenStagePanel(mixerStrips, columns: numOutStrips),
                OpenStagePanel(controlArray, width: "20%")
            ], columns: 2),
            // panel 2 - serverHub controls
            OpenStagePanel([
                OpenStagePanel((faderMute ! numIns).flat, columns: numIns),
                OpenStagePanel(controlArray, width: "20%"), 
            ], columns: 2)
        ]).write(path);
    }

    *save { 
        var saveArray = List.newClear(0);
        var stripArray = stripWidgets.deepCollect(3,{ |widgetString| 
            if(widgetString.notNil,{ widgetString.clump(8000) })
        });
        var mixerArray = mixerStripWidgets.deepCollect(2,{ |widgetString|
            if(widgetString.notNil,{ widgetString.clump(8000) })
        });
        var idArray = OpenStageID.subclasses.collect({ |i| i.id });

        saveArray.add(idArray);
        saveArray.add(stripArray);
        saveArray.add(mixerArray);
        ^saveArray
    }

    *load { |loadArray, cond, action|
        loaded = false;

        OpenStageID.subclasses.do({ |id, index| id.setID(loadArray[0][index]) });

        loadArray[1].do({ |stripArray, stripIndex|
            stripArray.do({ |pageArray, pageIndex|
                var stripId = strips[stripIndex].tabArray[pageIndex].id;
                var widgetArray = stripWidgets[stripIndex][pageIndex];
                pageArray.do({ |widgetString, slotIndex|
                    if(widgetString.size > 0,{ widgetString = widgetString.join });
                    widgetArray[slotIndex] = widgetString;
                    // cond.wait { }
                });

                this.prRefreshStrip(widgetArray, stripId)
            });
        });

        loadArray[2].do({ |mixerStripArray, outMixerIndex|
            var stripId = mixerStrips[outMixerIndex].id;
            var widgetArray = mixerStripWidgets[outMixerIndex];

            mixerStripArray.do({ |widgetString, slotIndex|
                if(widgetString.size > 0,{ widgetString = widgetString.join });
                widgetArray[slotIndex] = widgetString;
                // cond.wait {}
            });

            this.prRefreshStrip(widgetArray, stripId)
        });

        loaded = true;
        action.value;
    }
}
