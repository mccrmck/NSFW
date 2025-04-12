NS_ServerID {
    classvar <id = 57100;
    *initClass { id = 57100; }
    *next  { ^id = id + 1; }
}

NS_ServerOptions {
    var <numChans;
    var <inChannels, <outChannels;
    var <blockSize, <sampleRate;
    var <inDevice, <outDevice;
    var <options;

    *new { |numChans, inChans, outChans, block, sRate, inDev, outDev|
        ^super.newCopyArgs(
            numChans ? 2,
            inChans ? 2, outChans ? 2,
            block ? 64, sRate ? 48000, 
            inDev ? "default", outDev ? "default"
        ).init
    }

    init {
        options = ServerOptions()
        .numInputBusChannels_( inChannels )
        .numOutputBusChannels_( outChannels )
        .maxNodes_( 1024 )     // ServerOptions default, consider
        .maxSynthDefs_( 1024 ) // ServerOptions default, consider
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
    var <synthLib;

    *new { |name, nsOptions, action|
        ^super.newCopyArgs(name).init(nsOptions, action)
    }

    init { |nsOptions, action|
        id = NS_ServerID.next;
        options = nsOptions;
        while({ 
            ("lsof -i :" ++ id).unixCmdGetStdOut.size > 0 
        },{ 
            id = NS_ServerID.next
        });

        server = Server(name, NetAddr("localhost", id), options.options);
        synthLib = SynthDescLib(name, server);
        this.buildServer(server, action)
    }

    addSynthDef { |synthName, ugenGraph, action|
        ^SynthDef(synthName.asSymbol, ugenGraph).add(name, action)
    }

    addSynthDefCreateSynth { |group, synthName, ugenGraph, args, action|
        var synth;
        if(synthLib.at(synthName).notNil,{
            synth = Synth(synthName, args.asArray, group, \addToTail);
            action.value(synth)
        },{
            this.addSynthDef(synthName, ugenGraph, {
                var cond = CondVar();                     // consider moving this to the Server instance

                fork{
                    synth = Synth.basicNew(synthName, server);
                    synth.register;
                    OSCFunc({
                        cond.signalOne;
                    }, '/n_go', server.addr, nil, [synth.nodeID]).oneShot;
                    server.sendBundle(
                        server.latency, synth.addToTailMsg(group, args.asArray)
                    );
                    cond.wait { synth.isPlaying };
                    action.value(synth)
                }
            })
        })
    }
}

NS_MatrixServer : NS_Server {
    const <numPages  = 6;
    const <numStrips = 4;
    var <inGroup, pages, <pageGroups, <mixerGroup;
    var <inputs;
    var <strips, <outMixer;

    buildServer { |server, action|
        server.waitForBoot({
            inGroup    = Group(server);
            pages      = Group(inGroup, \addAfter);
            pageGroups = numPages.collect({ Group(pages, \addToTail) });
            mixerGroup = Group(pages, \addAfter);

            inputs     = IdentityDictionary();

            outMixer   = 4.collect({ |channelIndex|
                var id = "o:%".format(channelIndex);
                NS_ChannelStripOut(id, mixerGroup)
            });

            strips     = pageGroups.collect({ |pageGroup, pageIndex|
                numStrips.collect({ |stripIndex|
                    var id = "%:%".format(pageIndex, stripIndex);
                    NS_ChannelStripMatrix(id, pageGroup).pause
                })
            });

            server.sync;
            action.value(this)
        })
    }

    addInput { }

    save {
        var saveArray = List.newClear(0);

        // saveArray.add(window.save);
        //saveArray.add(outMixer.collect({ |strip| strip.save }) );
        //saveArray.add(strips.deepCollect(2,{ |strip| strip.save }));
        // saveArray.add(NSFW.controllers.collect({ |ctrl| ctrl.save }));

        ^saveArray;
    }

    load { |loadArray|

      //  {
      //      strips.deepDo(2,{ |strp| strp.unpause; strp.free });
      //      outMixer.do({ |strp| strp.free });

      //      // load in reverse
      //      //   loadArray[3].do({ |ctrlArray|
      //      //       if( NSFW.controllers.notNil and:{NSFW.controllers.includes(ctrlArray[0]) },{
      //      //           ctrlArray[0].load( ctrlArray[1] )
      //      //       },{
      //      //           "attached controller not saved in file".warn
      //      //       })
      //      //   });

      //      loadArray[2].do({ |groupArray, groupIndex|
      //          groupArray.do({ |strip, stripIndex|
      //              strips[groupIndex][stripIndex].load(strip)
      //          })
      //      });
      //      outMixer.do({ |strip,index| strip.load( loadArray[1][index] ) }); // outMixer

      //      // use CondVar; without the wait, strips get paused before modules get loaded 
      //      // this means the strips are paused, but the modules aren't...
      //      2.wait;
      //      window.load(loadArray[0]);
      //  }.fork(AppClock)
    }

    free {
        server.freeAll; // free all nodes
        server.quit({"ns_server: % quit".format(name).postln})
    }
}

NS_TimelineServer : NS_Server {

    buildServer {}
}
