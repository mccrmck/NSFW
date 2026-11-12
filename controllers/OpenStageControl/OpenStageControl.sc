OpenStageControl : NS_Controller {
    classvar <connected = false, <loaded = false;
    classvar <netAddr, <pid;
    classvar <strips,    <stripFaders,    <>stripWidgets;
    classvar <outStrips, <outStripFaders, <>outStripWidgets;

    // gotta check if the port is available and no other o-s-c processes are running;
    // if they are, kill 'em and boot 
    *connect {
        var ip = "localhost", port = 8080; 
        var path = "NSFW.json".resolveRelative;
        var unixString = "node /Applications/open-stage-control.app/Contents/Resources/app/" + 
        "--send %:%".format(ip, NetAddr.localAddr.port) +
        "--custom-module '%'".format( "nsfwModule.js".resolveRelative ) +
        "--load '%'".format(path);

        this.makeInterface(path);

        netAddr = NetAddr(ip, port);
        pid = unixString.unixCmd;
        CmdPeriod.add({ this.cleanUp });

        // maybe we need the OSCFunc here again to call .refresh when new clients connect
        // could also be a callback that signals when to draw WebView

        // look at variable names - widgetArray should maybe be fragmentArray or something?

        pid !? { connected = true };
    }

    *cleanUp {
        pid !? { 
            if(pid.pidRunning) {
                "kill %".format(pid).unixCmd; 
                "bye-bye o-s-c".postln
            }
        };
        connected = false;
    }

    *drawView {
        var webView = WebView();
        var reloadAttempts = 0;
        ^NS_ContainerView().layout_(
            VLayout(
                NS_Button([
                    ["boot o-s-c", NS_Style('textLight'), NS_Style('bGroundDark')],
                    ["close o-s-c", NS_Style('textLight'), NS_Style('bGroundDark')]
                ])
                .maxHeight_(20)
                .addLeftClickAction({ |but|
                    if(but.value == 1) {
                        fork{
                            this.connect;
                            { 
                                webView
                                .url_( "%:%".format(netAddr.ip, netAddr.port) )
                                .onLoadFailed_({ |webView|

                                    while { reloadAttempts < 20 } { 
                                        webView.reload;
                                        reloadAttempts = reloadAttempts + 1;

                                        "make this better".postln 
                                    }
                                })
                            }.defer
                        }
                    } 
                    { this.cleanUp }
                }),
                webView
            )
        )
    }

    // would be great to draw the UI upon instantiating a new client
    // this draws all the widgets but does not update their values...
    // do I send *every* control value on refresh?!?!
    *refresh {
        stripWidgets.do { |allPages, stripIndex| 
            allPages.do { |widgetArray, pageIndex| 
                var id = strips[stripIndex].tabs[pageIndex].id;
                this.prRefreshStrip(widgetArray, id);
            }
        };

        outStripWidgets.do { |widgetArray, stripIndex|
            var id = outStrips[stripIndex].id;
            this.prRefreshStrip(widgetArray, id)
        };
    }

    *addModuleFragment { |pageIndex, stripIndex, slotIndex, moduleClass|
        this.prUpdateStrip(pageIndex, stripIndex, slotIndex, moduleClass.oscFragment)
    }

    *removeModuleFragment { |pageIndex, stripIndex, slotIndex|
        this.prUpdateStrip(pageIndex, stripIndex, slotIndex, nil)
    }

    *prUpdateStrip { |pageIndex, stripIndex, slotIndex, moduleOrNil|
        var stripId, stringArray;

        case
        { pageIndex == $O } {
            stripId     = outStrips[stripIndex.asInteger].id;
            stringArray = outStripWidgets[stripIndex.asInteger];
        }
        { pageIndex == $I } { /* nothing for now */}
        {
            stripId     = strips[stripIndex.asInteger].tabs[pageIndex].id;
            stringArray = stripWidgets[stripIndex][pageIndex];
        };

        if(pageIndex != $I) {
            stringArray[slotIndex] = moduleOrNil !? { moduleOrNil.oscString };
            this.prRefreshStrip(stringArray, stripId)
        }
    }

    *prRefreshStrip { |stringArray, stripId|
        var stripString = stringArray.select(_.notNil);
        var widgetString = "";
        if(stripString.size > 0) {
            widgetString = stripString[0].ccatList(stripString[1..]);
        };
        netAddr.sendMsg(
            "/EDIT", "%".format(stripId), "{\"widgets\": [%]}".format(widgetString)
        )
    }

    *prCollectOSCStrings { |inArray|
        var array = inArray.select(_.notNil).collect(_.oscString);
        array = "%".ccatList("%" ! (array.size - 1)).format(*array);
        ^array
    }

    *switchStripPage { |pageIndex, stripIndex|
        var stripId    = strips[stripIndex].id;
        var stripCtlId = stripFaders[stripIndex].id;
        netAddr.sendBundle(nil,
            ["/%".format(stripId),    pageIndex],
            ["/%".format(stripCtlId), pageIndex],
        );
    }

    *makeInterface { |path|
        var swapGrid, controlArray;
        var controlPanel, stripPanel, outStripPanel;
        var numIns        = NS_Server.numInStrips;
        var numPages      = NS_Server.numPages;
        var numStrips     = NS_Server.numStrips;
        var numOutStrips  = NS_Server.numOutStrips;
        var faderMute     = {
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false).vertical,
                OpenStageButton().height_("20%")
            ])
        };

        swapGrid          = { OpenStageSwitch().numPads_(numPages) } ! numStrips;

        stripFaders       = { OpenStagePanel().tabArray_(faderMute ! numPages) } ! numStrips;
        outStripFaders    = faderMute ! numOutStrips; 

        controlArray      = [
            OpenStagePanel().widgetArray_(swapGrid).columns_(numStrips),
            OpenStagePanel().widgetArray_(stripFaders).columns_(numStrips),
            OpenStagePanel().widgetArray_(outStripFaders).columns_(numOutStrips),
        ];
        controlPanel      = OpenStagePanel().widgetArray_(controlArray).width_("16%");

        strips            = { OpenStagePanel().tabArray_({ OpenStagePanel() } ! numPages) } ! numStrips;
        stripPanel        = OpenStagePanel().widgetArray_(strips).columns_(numStrips);

        outStrips         = { OpenStagePanel() } ! numOutStrips;
        outStripPanel     = OpenStagePanel().widgetArray_(outStrips).columns_(numOutStrips);

        // these should maybe move up to the connect function?
        stripWidgets      = { { Array.newClear(NS_ChannelStrip.numSlots) } ! numPages } ! numStrips;
        outStripWidgets   = { Array.newClear(NS_OutStrip.numSlots) } ! numOutStrips;

        OpenStageRoot().tabArray_([
            // panel 0 - strip modules
            OpenStagePanel().widgetArray_([stripPanel, controlPanel]).columns_(2),
            // panel 1 - outStrip modules
            OpenStagePanel().widgetArray_([outStripPanel, controlPanel]).columns_(2),
        ]).write(path);
    }

    *save { 
        ^[OpenStageID.save, stripWidgets.deepCopy, outStripWidgets.deepCopy]
    }

    *load { |loadArray, cond, action|
        loaded = false;

        OpenStageID.load(loadArray[0]);

        this.stripWidgets = loadArray[1].deepCopy;
        this.outStripWidgets = loadArray[2].deepCopy;
        this.refresh;

        loaded = true;
        action.value;
    }
}
