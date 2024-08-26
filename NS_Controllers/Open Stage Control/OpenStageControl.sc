OpenStageControl {
  classvar <modSinkLetter, <modSinkColor;
  classvar <netAddr;
  classvar <strips, <stripCtls, <>stripWidgets;

  *initClass {
      modSinkLetter = "O";
      modSinkColor = [ Color.white, Color.fromHexString("#6daffd")];
  }

  *boot { |ip = "localhost", port = 8080|
      var path = "NSFW.json".resolveRelative;
      var unixString = "open /Applications/open-stage-control.app --args " ++
      "--send %:% ".format(ip, NetAddr.localAddr.port ) ++
      "--load '%'".format( path );

      this.makeInterface( path );

      netAddr = NetAddr(ip,port);
      unixString.unixCmd;
  }

  *addModuleFragment { |pageIndex, stripIndex, slotIndex, moduleClass|
      this.prUpdateStrip(pageIndex, stripIndex, slotIndex, moduleClass.oscFragment)
  }

  *removeModuleFragment { |pageIndex, stripIndex, slotIndex|
      this.prUpdateStrip(pageIndex, stripIndex, slotIndex, nil)
  }

  *prUpdateStrip { |pageIndex, stripIndex, slotIndex, moduleOrNil|
      var stripId = this.strips[stripIndex].tabArray[pageIndex].id;
      var widgetArray = stripWidgets[stripIndex][pageIndex];
      widgetArray[slotIndex] = moduleOrNil;
      widgetArray = widgetArray.select({ |w| w.notNil });

      widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);

      this.netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(widgetArray))
  }

  *switchStripPage { |pageIndex, stripIndex|
      var stripId = this.strips[stripIndex].id;
      var stripCtlId = this.stripCtls[stripIndex].id;
      // this can/should be sent as a bundle, gotta double check the syntax...
      this.netAddr.sendMsg("/%".format(stripId),pageIndex);
      this.netAddr.sendMsg("/%".format(stripCtlId),pageIndex);
  }

  *makeInterface { |path|
      var swapGrid, mixerCtls, controlArray;
      var numStrips = 4;
      var numPages  = 6;

      swapGrid  = { OSC_Switch(horizontal: false,mode: 'slide', numPads: numPages) }! numStrips;
      stripCtls = { OSC_Panel(horizontal: false, tabArray: { OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height:"20%") ] ) } ! numPages ) } ! numStrips;
      mixerCtls = { OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height:"20%") ]) } ! 4; // 4 outputs for the time being
      controlArray = [
          OSC_Panel(height: "30%", widgetArray: swapGrid ),
          OSC_Panel(widgetArray: stripCtls),
          OSC_Panel(widgetArray: mixerCtls)
      ];

      strips = { OSC_Panel(horizontal:false, tabArray: { OSC_Panel(horizontal:false) }!numPages ) }!numStrips;
      stripWidgets = Array.fill(numStrips,{ Array.fill(numPages,{ List.newClear(6) })  }); // this is hardcoded to 4 modules + 1 inModule due to screen real estate

      OSC_Root(true,[
          OSC_Panel(widgetArray: strips), // strips
          OSC_Panel("20%", horizontal: false, widgetArray: controlArray ), // controlPanel
      ]).write(path);
  }

  *save { 
      var saveArray = List.newClear(0);
      var stripArray = stripWidgets.deepCollect(3,{ |widgetString| if(widgetString.notNil,{ widgetString.clump(8000) }) });
      var idArray = OSC_WidgetID.subclasses.collect({ |i| i.id });
      saveArray.add( this );
      saveArray.add( [idArray, stripArray] );
      ^saveArray
  }

  *load { |loadArray|

      OSC_WidgetID.subclasses.do({ |id, index| id.setID( loadArray[0][index] ) });

      loadArray[1].do({ |stripArray, stripIndex|
          stripArray.do({ |pageArray, pageIndex|
              var stripId = this.strips[stripIndex].tabArray[pageIndex].id;
              var widgetArray = stripWidgets[stripIndex][pageIndex];
              pageArray.do({ |widgetString, slotIndex|
                  if(widgetString.size > 0,{
                      widgetString = widgetString.join;
                  });
                  widgetArray[slotIndex] = widgetString;
              });
              widgetArray = widgetArray.select({ |w| w.notNil });
              widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);

              this.netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(*widgetArray))
          });
      });
  }
}
