NS_ServerID {
    classvar <id = 57100;
    *initClass { id = 57100; }
    *next  { ^id = id + 1; }
}

NS_Server {
    var <server, <id, <options;
    var <inGroup, pages, <pageGroups, <mixerGroup;
    var <inputBusses, <stripBusses, <strips, <outMixer, <outMixerBusses;
    var <window;

    *new { |name, blockSize = 64, action|
        ^super.new.init(name, blockSize, action)
    }

    init { |name, blocks, action|
        id = NS_ServerID.next;
        while({ ("lsof -i :" ++ id).unixCmdGetStdOut.size > 0 },{ id = NS_ServerID.next });

        options = Server.local.options.copy;
        options.memSize = 2**20;
        options.numWireBufs = 1024;
        options.blockSize = blocks;

        server = Server(name,NetAddr("localhost", id),options);

        server.waitForBoot({
            server.sync;
            inGroup    = Group(server);
            pages      = Group(inGroup,\addAfter);
            pageGroups = 6.collect({ Group.new(pages,\addToTail) });
            mixerGroup = Group(pages,\addAfter);

            server.sync;
            
            inputBusses = Bus.audio(server,NSFW.numInChans * 2);

            server.sync;

            outMixer = 4.collect({ |channelNum|
                NS_OutChannelStrip(mixerGroup,0).setLabel("out: %".format(channelNum))
            });

            server.sync;

            outMixerBusses = outMixer.collect({ |strip| strip.stripBus });

            strips = pageGroups.collect({ |pageGroup|
                4.collect({ |stripNum|
                    NS_ChannelStrip(pageGroup,outMixerBusses[0]).pause
                })
            });

            server.sync;

            stripBusses = strips.deepCollect(2,{ |strip| strip.stripBus });

            server.sync;

            window = NS_ServerWindow(this);
            action.value
        });
    }

    save { |path|
        var saveArray = Array.newClear(3);

        saveArray.put(0,window.save);
        saveArray.put(1,outMixer.collect({ |strip| strip.save }) );
        saveArray.put(2,strips.deepCollect(2,{ |strip| strip.save }));

        saveArray.writeArchive(path);
    }

    load { |path|
        var loadArray = Object.readArchive(path);

        // something here to clear all strips, outmixer...

        window.load(loadArray[0]);
        outMixer.do({ |strip,index| strip.load(loadArray[1][index] ) }); // outMixer
        loadArray[2].do({ |groupArray, groupIndex|
            groupArray.do({ |strip, stripIndex|
                strips[groupIndex][stripIndex].load(strip)
            })
        });
    }
}
