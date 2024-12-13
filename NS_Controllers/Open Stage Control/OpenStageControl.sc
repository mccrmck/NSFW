OpenStageControl {
    classvar <modSinkLetter, <modSinkColor;
    classvar <netAddr;
    classvar <guiLayerSwitch, <strips, <stripFaders, <>stripWidgets, <mixerStrips, <mixerFaders, <>mixerStripWidgets;

    *initClass {
        modSinkLetter = "O";
        modSinkColor = [ Color.white, Color.fromHexString("#6daffd") ];
    }

    *boot { |ip = "localhost", port = 8080|
        var path = "NSFW.json".resolveRelative;
        var unixString = "open /Applications/open-stage-control.app --args " ++
        "--send %:% ".format(ip, NetAddr.localAddr.port ) ++
        "--load '%'".format( path );

        this.makeInterface( path );

        netAddr = NetAddr(ip,port);
        unixString.unixCmd;

        OSCFunc({ |msg|
            netAddr.sendMsg(
                "/EDIT",
                "%".format( guiLayerSwitch.id ),
                "{\"onValue\": \"set(\\\"root\\\",value)\", \"bypass\": true }"
            );
        },'/nsfwGuiLoaded');
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
        widgetArray = widgetArray.select({ |w| w.notNil });

        widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);

        this.netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(widgetArray))
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
        var numStrips = 4;
        var numPages  = 6;
        var numOutStrips = 4;

        guiLayerSwitch = OSC_Switch(height: "10%",columns: 3, numPads: 3);
        swapGrid  = { OSC_Switch(mode: 'slide', numPads: numPages) }! numStrips;

        stripFaders = { OSC_Panel(horizontal: false, tabArray: { OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height:"20%") ]) } ! numPages ) } ! numStrips;
        mixerFaders = { OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height:"20%") ]) } ! 4; // 4 outputs for the time being

        controlArray = [
            guiLayerSwitch,
            OSC_Panel(widgetArray: swapGrid ),
            OSC_Panel(widgetArray: stripFaders),
            OSC_Panel(widgetArray: mixerFaders)
        ];

        strips = { OSC_Panel(horizontal:false, tabArray: { OSC_Panel(horizontal:false) } ! numPages ) } ! numStrips;
        stripWidgets = { { List.newClear(6) } ! numPages } ! numStrips; // this is hardcoded to 5 modules + 1 inModule due to screen real estate

        mixerStrips = { OSC_Panel(horizontal:false) } ! numOutStrips;
        mixerStripWidgets = { List.newClear(5) } ! numOutStrips; // 4 modules per outMixerChannel... + 1 for the phantom inModule -> FIX THIS

        OSC_Root(true,tabArray: [
            // panel 0 - strip modules
            OSC_Panel(widgetArray: [
                OSC_Panel(widgetArray: strips), // strips
                OSC_Panel("20%", horizontal: false, widgetArray: controlArray ), // controlPanel
            ]),
            // panel 1 - mixer modules
            OSC_Panel(widgetArray: [
                OSC_Panel(widgetArray: mixerStrips ), // mixerStrips
                OSC_Panel("20%", horizontal: false, widgetArray: controlArray ), // controlPanel
            ]),
            // panel 2 - serverHub controls
            OSC_Panel(widgetArray:[
                OSC_Panel(widgetArray: ({ OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height: "20%") ]) } ! NSFW.numInBusses).flat ),
                OSC_Panel("20%", horizontal: false, widgetArray: controlArray ), // controlPanel
            ])
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

                this.netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(*widgetArray))
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

            this.netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(*widgetArray))
        });
    }
}
