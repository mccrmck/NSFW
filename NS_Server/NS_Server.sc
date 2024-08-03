NS_ServerID {
    classvar <id = 57100;
    *initClass { id = 57100; }
    *next  { ^id = id + 1; }
}

NS_Server {
    var <server, <name, <id, <options;
    var <inGroup, pages, <pageGroups, <mixerGroup;
    var <inputBusses, <stripBusses, <strips, <outMixer, <outMixerBusses;
    var <window;

    *new { |name, blockSize = 64, action|
        ^super.new.init(name, blockSize, action)
    }

    init { |nameIn, blocks, action|
        name = nameIn;
        id = NS_ServerID.next;
        while({ ("lsof -i :" ++ id).unixCmdGetStdOut.size > 0 },{ id = NS_ServerID.next });

        options = Server.local.options.copy;
        options.memSize = 2**20;
        options.numWireBufs = 1024;
        options.blockSize = blocks;

        server = Server(nameIn,NetAddr("localhost", id),options);

        server.waitForBoot({
            server.sync;
            inGroup    = Group(server);
            pages      = Group(inGroup,\addAfter);
            pageGroups = 6.collect({ Group.new(pages,\addToTail) });
            mixerGroup = Group(pages,\addAfter);

            server.sync;
            
            inputBusses = NSFW.numInChans.collect({ Bus.audio(server, 2) });

            server.sync;

            outMixer = 4.collect({ |channelNum|
                NS_OutChannelStrip(mixerGroup,0).setLabel("out: %".format(channelNum))
            });

            server.sync;

            outMixerBusses = outMixer.collect({ |strip| strip.stripBus });

            strips = pageGroups.collect({ |pageGroup, pageIndex|
                4.collect({ |stripIndex|
                    NS_ChannelStrip(pageGroup, outMixerBusses[0], pageIndex, stripIndex).pause
                })
            });

            server.sync;

            stripBusses = strips.deepCollect(2,{ |strip| strip.stripBus });

            server.sync;

            window = NS_ServerWindow(this);
            action.value
        });
    }

    save {
        var saveArray = List.newClear(0);

        saveArray.add(window.save);
        saveArray.add(outMixer.collect({ |strip| strip.save }) );
        saveArray.add(strips.deepCollect(2,{ |strip| strip.save }));

        ^saveArray;
    }

    load { |loadArray|

        // something here to clear all strips, outmixer...in case there are modules currently loaded!

        window.load(loadArray[0]);
        outMixer.do({ |strip,index| strip.load(loadArray[1][index] ) }); // outMixer
        loadArray[2].do({ |groupArray, groupIndex|
            groupArray.do({ |strip, stripIndex|
                strips[groupIndex][stripIndex].load(strip)
            })
        });
    }

    free {
        inGroup.free;
        pages.free;
        pageGroups.free;
        mixerGroup.free;
    }
}
