OpenStageControl {
  classvar <netAddr;
  classvar <strips, <>stripWidgets;

  *boot { |ip = "localhost", port = 8080|
      var fileName = "NSFW";
      var path = "%.json".format(fileName).resolveRelative;
      var unixString = "open /Applications/open-stage-control.app --args " ++
      "--send %:% ".format(ip, NetAddr.localAddr.port ) ++
      "--load '%'".format( path );

      this.makeInterface( path );

      netAddr = NetAddr(ip,port);
      unixString.unixCmd;
  }

  *addModuleFragment { |pageIndex, stripIndex, slotIndex, moduleClass|
      var stripId = this.strips[stripIndex].tabArray[pageIndex].id;
      var widgetArray = stripWidgets[stripIndex][pageIndex];
      widgetArray[slotIndex] = moduleClass.oscFragment;
      widgetArray = widgetArray.select({ |w| w.notNil });

      widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);
      widgetArray.asCompileString.postln;

      this.netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(widgetArray))
  }

  *removeModuleFragment { |pageIndex, stripIndex, slotIndex|
      var stripId = this.strips[stripIndex].tabArray[pageIndex].id;
      var widgetArray = stripWidgets[stripIndex][pageIndex];
      widgetArray[slotIndex] = nil;
      widgetArray = widgetArray.select({ |w| w.notNil });

      widgetArray = "%".ccatList("%"!(widgetArray.size-1)).format(*widgetArray);

      this.netAddr.sendMsg("/EDIT","%".format(stripId),"{\"widgets\": [%]}".format(widgetArray))
  }

  *switchStripPage { |pageIndex, stripIndex|
      var id = this.strips[stripIndex].id;
      this.netAddr.sendMsg("/%".format(id),pageIndex)
  }

  *makeInterface { |path|
      var numStrips = 4;
      var numPages = 6;
      var swapGrid  = { OSC_Switch(horizontal: false,mode: 'slide', numPads: numPages) }!numStrips;
      var stripCtls = { OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height:"20%") ])  }!numStrips;
      var mixerCtls = { OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height:"20%") ])  }!numStrips;

      var controlArray = [
          OSC_Panel(height: "30%", widgetArray: swapGrid ),
          OSC_Panel(widgetArray: stripCtls),
          OSC_Panel(widgetArray: mixerCtls)
      ];

      strips = { OSC_Panel(horizontal:false, tabArray: { OSC_Panel(horizontal:false) }!numPages ) }!numStrips;
      stripWidgets = Array.fill(numStrips,{ Array.fill(numPages,{ List.newClear(5) })  }); // this is hardcoded to 4 modules + 1 inModule due to screen real estate

      OSC_Root(true,[
          OSC_Panel(widgetArray: strips), // strips
          OSC_Panel("20%", horizontal: false, widgetArray: controlArray ), // controlPanel
      ]).write(path);
  }

  *write { |path| this.netAddr.sendMsg('/SESSION/SAVE', path ++ "_oscGUI.json") } 

  *read { |path| this.netAddr.sendMsg('/SESSION/OPEN', path ++ "_oscGUI.json") }

  *save { 
      var saveArray = List.newClear(0);
      saveArray.add( this );
      
      //saveArray.add( stripWidgets );
      ^saveArray
  }

  *load { |loadArray|
      loadArray.asCompileString.postln;
  }

}
