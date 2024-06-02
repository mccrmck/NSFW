NS_ServerID {
	classvar <id = 57100;
	*initClass { id = 57100; }
	*next  { ^id = id + 1; }
}

NS_Server {
  var <server, <id;
  var <inGroup, pages, <pageGroups, <mixerGroup;
  var <inBussesMono, <inBussesStereo, <stripBusses, <strips, <outMixer, <outMixerBusses;

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

      outMixer = 4.collect({ |stripNum|
        NS_ChannelStrip(mixerGroup,0,4).addInSynth
      });

      outMixerBusses = outMixer.collect({ |strip| strip.stripBus });

      strips = pageGroups.collect({ |pageGroup, pageNum|
        4.collect({ |stripNum|
          NS_ChannelStrip(pageGroup,outMixerBusses[0],5)
        })
      });

      server.sync;

      stripBusses = strips.deepCollect(2,{ |strip| strip.stripBus });

      server.sync;

      NS_MainWindow(this)
    });

  }
}
