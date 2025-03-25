OpenStageControl {
    classvar <modSinkLetter, <modSinkColor;
    classvar <netAddr, <pid, view;
    classvar <guiLayerSwitch, <strips, <stripFaders, <>stripWidgets, <mixerStrips, <mixerFaders, <>mixerStripWidgets;

    *initClass {
        ShutDown.add({ this.cleanup });
        modSinkLetter = "O";
        modSinkColor = [ Color.white, Color.fromHexString("#6daffd") ];
    }

    // gotta check if the port is available and no other o-s-c processes are running;
    // if they are, kill 'em and boot 
    // add a CmdPeriod thing!!
    *boot { |ip = "localhost", port = 8080| // I'm not sure these are the right arguments to pass
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
            netAddr.sendMsg(
                "/EDIT",
                "%".format( guiLayerSwitch.id ),
                "{\"onValue\": \"set(\\\"root\\\",value)\", \"bypass\": true }" // this seems like a hack, no?
            );
        },'/nsfwGuiLoaded');
    }

    *cleanup {
        pid !? { if(pid.pidRunning,{"kill %".format(pid).unixCmd; "bye-bye o-s-c".postln}) }
    }

    // this was it's own class, I could then make separate instances, now just one..is this what we want?
    *makeWindow {
        view = Window("o-s-c").front; 
        view.layout_( HLayout( WebView().url_( "%:%".format(netAddr.ip, netAddr.port) ) ));
        view.layout.margins_(0).spacing_(0);
    }

    *closeWindow { view.close }

     // this draws all the widgets but does not update their values...do I send *every* control value on refresh?!?!
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

        if(pageIndex == -1,{
            stripId = this.mixerStrips[stripIndex].id;
            widgetArray = mixerStripWidgets[stripIndex];
        },{
            stripId = this.strips[stripIndex].tabArray[pageIndex].id;
            widgetArray = stripWidgets[stripIndex][pageIndex];
        });

        widgetArray[slotIndex] = moduleOrNil;
        this.prRefreshStrip(widgetArray, stripId)
      //  widgetArray = widgetArray.select({ |w| w.notNil });

      //  widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);

      //  netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(widgetArray))
    }

    *prRefreshStrip { |widgetArray, stripId|
        widgetArray = widgetArray.select({ |w| w.notNil });

        widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);

        netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(widgetArray))

    }

    *switchStripPage { |pageIndex, stripIndex|
        var stripId = this.strips[stripIndex].id;
        var stripCtlId = this.stripFaders[stripIndex].id;
        // this can/should be sent as a bundle, gotta double check the syntax...
        this.netAddr.sendMsg("/%".format(stripId),pageIndex);
        this.netAddr.sendMsg("/%".format(stripCtlId),pageIndex);
    }

    *makeInterface { |path|
        var swapGrid, controlArray;
        var numIns = NSFW.numInBusses;
        var numPages  = 6;
        var numStrips = 4;
        var numOutStrips = 4; // 4 outputs...for now
        var faderMute = { OSC_Panel([ OSC_Fader(false, false), OSC_Button(height:"20%") ]) };

        guiLayerSwitch = OSC_Switch(3, 3, 'tap', height: "10%");
        swapGrid  = { OSC_Switch(numPages, 1, 'slide') }! numStrips;

        stripFaders = { OSC_Panel(tabArray: faderMute ! numPages) } ! numStrips;
        mixerFaders = faderMute ! numOutStrips; 

        controlArray = [
            guiLayerSwitch,
            OSC_Panel(swapGrid,    columns: numStrips),
            OSC_Panel(stripFaders, columns: numStrips),
            OSC_Panel(mixerFaders, columns: numOutStrips)
        ];

        strips            = { OSC_Panel(tabArray: { OSC_Panel() } ! numPages) } ! numStrips;
        mixerStrips       = { OSC_Panel() } ! numOutStrips;
        stripWidgets      = { {List.newClear(6)} ! numPages } ! numStrips; // 5 modules + 1 inModule due to screen real estate
        mixerStripWidgets = { List.newClear(5) } ! numOutStrips; // 4 modules per outMixerChannel... + 1 for the phantom inModule -> FIX THIS

        OSC_Root(tabArray: [
            // panel 0 - strip modules
            OSC_Panel([ 
                OSC_Panel(strips, columns: numStrips),
                OSC_Panel(controlArray, width: "20%") 
            ], columns: 2),
            // panel 1 - mixer modules
            OSC_Panel([
                OSC_Panel(mixerStrips, columns: numOutStrips),
                OSC_Panel(controlArray, width: "20%")
            ], columns: 2),
            // panel 2 - serverHub controls
            OSC_Panel([
                OSC_Panel((faderMute ! numIns).flat, columns: numIns),
                OSC_Panel(controlArray, width: "20%"), 
            ], columns: 2)
        ]).write(path);
    }

    *save { 
        var saveArray = List.newClear(0);
        var stripArray = stripWidgets.deepCollect(3,{ |widgetString| if(widgetString.notNil,{ widgetString.clump(8000) }) });
        var mixerArray = mixerStripWidgets.deepCollect(2,{ |widgetString| if(widgetString.notNil,{ widgetString.clump(8000) }) });
        var idArray = OSC_WidgetID.subclasses.collect({ |i| i.id });
        saveArray.add( this );
        saveArray.add( [idArray, stripArray, mixerArray] );
        ^saveArray
    }

    *load { |loadArray|

        OSC_WidgetID.subclasses.do({ |id, index| id.setID( loadArray[0][index] ) });

        loadArray[1].do({ |stripArray, stripIndex|
            stripArray.do({ |pageArray, pageIndex|
                var stripId = this.strips[stripIndex].tabArray[pageIndex].id;
                var widgetArray = stripWidgets[stripIndex][pageIndex];
                pageArray.do({ |widgetString, slotIndex|
                    if(widgetString.size > 0,{ widgetString = widgetString.join });
                    widgetArray[slotIndex] = widgetString;
                });
                widgetArray = widgetArray.select({ |w| w.notNil });
                widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);

                netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(*widgetArray))
            });
        });

        loadArray[2].do({ |mixerStripArray, outMixerIndex|
            var stripId = this.mixerStrips[outMixerIndex].id;
            var widgetArray = mixerStripWidgets[outMixerIndex];

            mixerStripArray.do({ |widgetString, slotIndex|
                if(widgetString.size > 0,{ widgetString = widgetString.join });
                widgetArray[slotIndex] = widgetString;
            });
            widgetArray = widgetArray.select({ |w| w.notNil });
            widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);

            netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(*widgetArray))
        });
    }
}
