NS_ServerID {
    classvar <id = 57100;
    *initClass { id = 57100; }
    *next  { ^id = id + 1; }
}

NS_Server {
  var <server, <id;
  var <inGroup, pages, <pageGroups, <mixerGroup;
  var <inBussesMono, <inBussesStereo, <stripBusses, <strips, <outMixer, <outMixerBusses;
  var <window;

  *new { |name|
    ^super.new.init(name)
  }

  init { |name|
    id = NS_ServerID.next;
    while( {("lsof -i :"++id).unixCmdGetStdOut.size > 0},{id = ModularServer_ID.next});

    // ServerOptions stuff here...
    //
    // 
    server = Server(name,NetAddr("localhost", id),Server.local.options);

    server.waitForBoot({
      inGroup    = Group(server);
      pages      = Group(inGroup,\addAfter);
      pageGroups = 6.collect({ Group.new(pages,\addToTail) });
      mixerGroup = Group(pages,\addAfter);

      server.sync;

      inBussesMono   = Array.fill(8,{ Bus.audio(server,2) });
      // just make this subBusses of above!!
      inBussesStereo = Array.fill(4,{ Bus.audio(server,2) });

      server.sync;

      outMixer = 4.collect({ |channelNum|
        NS_OutChannelStrip(mixerGroup,0).setLabel("out: %".format(channelNum))
      });

      outMixerBusses = outMixer.collect({ |strip| strip.stripBus });

      strips = pageGroups.collect({ |pageGroup|
        4.collect({ |stripNum|
          NS_ChannelStrip(pageGroup,outMixerBusses[0])
        })
      });

      server.sync;

      stripBusses = strips.deepCollect(2,{ |strip| strip.stripBus });

      server.sync;

      // pause all strips, let NS_SwapGrid activate the first page

      window = NS_MainWindow(this)
    });

  }
}
