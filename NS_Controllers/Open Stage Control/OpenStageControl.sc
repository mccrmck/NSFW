OpenStageControl {
  classvar <netAddr;
  classvar <strips;

  *boot { |ip = "localhost", port = 8080|
      var fileName = "NSFW";
      var unixString = "open /Applications/open-stage-control.app --args " ++
      "--send %:% ".format(ip, NetAddr.localAddr.port ) ++
      "--load '%'".format("%.json".format(fileName).resolveRelative );

      this.makeInterface(fileName);

      netAddr = NetAddr(ip,port);
      unixString.unixCmd;
  }

  updateStrip { |stripIndex, slotIndex, fileName|
      this.strips[stripIndex].widgetArray[slotIndex].loadFragment()
  }

  *makeInterface { |path|
      var swapGrid, stripCtls, mixerCtls, controlArray, stripArray;

      swapGrid  = { OSC_Switch(horizontal: false,mode: 'slide', numPads: 6) }!4;
      stripCtls = { OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height:"20%") ])  }!4;
      mixerCtls = { OSC_Panel(horizontal: false, widgetArray: [ OSC_Fader(), OSC_Button(height:"20%") ])  }!4;

      controlArray = [
          OSC_Panel(height: "30%", widgetArray: swapGrid ),
          OSC_Panel(widgetArray: stripCtls),
          OSC_Panel(widgetArray: mixerCtls)
      ];

      strips = { OSC_Panel(horizontal:false, widgetArray: { OSC_Fragment() }!5 ) }!4;

      OSC_Root(true,[
          OSC_Panel("20%", horizontal: false, widgetArray: controlArray ), // controlPanel
          OSC_Panel(widgetArray: strips), // strips
      ]).write(path)
  }
}

