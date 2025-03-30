NS_ServerID {
    classvar <id = 57100;
    *initClass { id = 57100; }
    *next  { ^id = id + 1; }
}

NS_ServerOptions {
    var <inChannels, <outChannels;
    var <blockSize, <sampleRate;
    var <inDevice, <outDevice;
    var <options;

    *new { |inChans, outChans, block, sRate, inDev, outDev|
        ^super.newCopyArgs(
            inChans ? 2, outChans ? 2,
            block ? 64, sRate ? 48000, 
            inDev ? "default", outDev ? "default"
        ).init
    }

    init {
        options = ServerOptions()
        .numInputBusChannels_( inChannels )
        .numOutputBusChannels_( outChannels )
        .maxNodes_( 1024 )     // ServerOptions default
        .maxSynthDefs_( 1024 ) // ServerOptions default
        .blockSize_( blockSize )
        .memSize_( 2 ** 20 )
        .sampleRate_( sampleRate )
        .inDevice_( inDevice )
        .outDevice_( outDevice )
        .recChannels_( outChannels );
    }
}

NS_Server {
    var <name, <server, <id, <options;

    *new { |name, nsOptions|
        ^super.newCopyArgs(name).init(nsOptions)
    }

    init { |nsOptions|
        id = NS_ServerID.next;
        options = nsOptions.options;
        while({ ("lsof -i :" ++ id).unixCmdGetStdOut.size > 0 },{ id = NS_ServerID.next });

        server = Server(name, NetAddr("localhost", id), options);

        this.buildServer(server)
    }
}

NS_MatrixServer : NS_Server {
    var <inGroup, pages, <pageGroups, <mixerGroup;
    var <inputs;
    var <inputBusses, <stripBusses, <strips, <outMixer, <outMixerBusses;
    var <window;

    buildServer { |server|
        server.waitForBoot({
            inGroup    = Group(server);
            pages      = Group(inGroup, \addAfter);
            pageGroups = 6.collect({ Group(pages, \addToTail) });
            mixerGroup = Group(pages, \addAfter);

            inputs     = List.newClear(0);

            server.sync;

            // this is hardcoded to 8 for now, must make dynamic
            // inputBusses = 8.collect({ Bus.audio(server, NSFW.numChans) });

            server.sync;

            // this is hardcoded to 8 for now, must make dynamic
            // inputs = 8.collect({ |inBus| NS_ServerInput(this, inBus) });
            server.sync;

           // outMixer = 4.collect({ |channelIndex|
           //     NS_OutChannelStrip(mixerGroup, channelIndex)
           // });

           outMixer = 4.collect({ |channelIndex| NS_ChannelStrip1(mixerGroup, 4) });

            server.sync;

            outMixerBusses = outMixer.collect({ |strip| strip.stripBus });

          //  strips = pageGroups.collect({ |pageGroup, pageIndex|
          //      4.collect({ |stripIndex|
          //          NS_ChannelStrip(pageGroup, outMixerBusses[0], pageIndex, stripIndex).pause
          //      })
          //  });

          strips = pageGroups.collect({ |pageGroup, pageIndex|
              4.collect({ |stripIndex|
                  NS_ChannelStrip1(pageGroup).pause
              })
          });


          server.sync;

          stripBusses = strips.deepCollect(2,{ |strip| strip.stripBus });

          server.sync;

          window = NS_ServerWindow(this);
      })
  }

  save {
      var saveArray = List.newClear(0);

      saveArray.add(window.save);
        saveArray.add(outMixer.collect({ |strip| strip.save }) );
        saveArray.add(strips.deepCollect(2,{ |strip| strip.save }));
        // saveArray.add(NSFW.controllers.collect({ |ctrl| ctrl.save }));

        ^saveArray;
    }

    load { |loadArray|

        {
            strips.deepDo(2,{ |strp| strp.unpause; strp.free });
            outMixer.do({ |strp| strp.free });

            // load in reverse
            //   loadArray[3].do({ |ctrlArray|
            //       if( NSFW.controllers.notNil and:{NSFW.controllers.includes(ctrlArray[0]) },{
            //           ctrlArray[0].load( ctrlArray[1] )
            //       },{
            //           "attached controller not saved in file".warn
            //       })
            //   });

            loadArray[2].do({ |groupArray, groupIndex|
                groupArray.do({ |strip, stripIndex|
                    strips[groupIndex][stripIndex].load(strip)
                })
            });
            outMixer.do({ |strip,index| strip.load( loadArray[1][index] ) }); // outMixer

            // use CondVar; without the wait, strips get paused before modules get loaded 
            // this means the strips are paused, but the modules aren't...
            2.wait;
            window.load(loadArray[0]);
        }.fork(AppClock)
    }

    free {
        window.free;
        server.freeAll;
        server.quit({"ns_server: % quit".format(name).postln})
    }
}

NS_TimelineServer : NS_Server {

    buildServer {}
}
