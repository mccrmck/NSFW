NS_ServerID {
    classvar <id = 57100;
    *initClass { id = 57100; }
    *next  { ^id = id + 1; }
}

NS_Server {
    var <name, <server, <id, <options;
    var <inGroup, pages, <pageGroups, <mixerGroup;
    var <inputBusses, <stripBusses, <strips, <outMixer, <outMixerBusses;
    var <window;

    *new { |name, blockSize = 64, action|
        ^super.newCopyArgs(name).init(blockSize, action)
    }

    init { |blocks, action|
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
            
            inputBusses = NSFW.numInBusses.collect({ Bus.audio(server, NSFW.numChans) });

            server.sync;

            outMixer = 4.collect({ |channelNum|
                NS_OutChannelStrip(mixerGroup,channelNum)
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
        saveArray.add(NSFW.controllers.collect({ |ctrl| ctrl.save }));

        ^saveArray;
    }

    load { |loadArray|

        {
            strips.deepDo(2,{ |strp| strp.unpause; strp.free });
            outMixer.do({ |strp| strp.free });

            // load in reverse
            loadArray[3].do({ |ctrlArray|
                if( NSFW.controllers.notNil and:{NSFW.controllers.includes(ctrlArray[0]) },{
                    ctrlArray[0].load( ctrlArray[1] )
                },{
                    "attached controller not saved in file".warn
                })
            });

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
        inGroup.free;
        pages.free;
        pageGroups.free;
        mixerGroup.free;
    }
}
