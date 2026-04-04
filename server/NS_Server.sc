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

    *new { |numChans = 2, inChans = 2, outChans = 4, 
        block = 64, sRate = 48000, inDev = "default", outDev = "default"|
        ^super.newCopyArgs(
            numChans, inChans, outChans, block, sRate, inDev, outDev
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

    save {
        ^[
            numChans, inChannels, outChannels, 
            blockSize, sampleRate, inDevice, outDevice
        ]
    }

    load {}
}

NS_Server {
    const <numInStrips  = 8; // 8 inputs, busses assigned by user
    const <numPages     = 6; // how many pages on a server
    const <numStrips    = 4; // how many strips on a page
    const <numOutStrips = 4; // how many outStrips

    var <name, <server, <id, <options;
    var <cond;
    var <synthLib;

    var <inGroup, pages, <pageGroups, <mixerGroup;
    var <inStrips, <strips, <outStrips, <swapGrid;
    var <>window;
    var <outMeter;

    *new { |name, nsOptions, action|
        ^super.newCopyArgs(name).init(nsOptions, action)
    }

    init { |nsOptions, action|
        id = NS_ServerID.next;
        options = nsOptions;
        cond = CondVar();
        while
        { ("lsof -i :" ++ id).unixCmdGetStdOut.size > 0 } 
        { id = NS_ServerID.next };

        server = Server(name, NetAddr("localhost", id), options.options);
        synthLib = SynthDescLib(name, server);
        this.buildServer(server, action)
    }

    buildServer { |server, action|
        server.waitForBoot({
            inGroup    = Group(server);
            pages      = Group(inGroup, \addAfter);
            pageGroups = numPages.collect { Group(pages, \addToTail) };
            mixerGroup = Group(pages, \addAfter);

            outStrips  = numOutStrips.collect { |channelIndex|
                var id = "O:%".format(channelIndex);
                NS_ChannelStripOut(id, mixerGroup)
            };

            strips     = pageGroups.collect { |pageGroup, pageIndex|
                numStrips.collect { |stripIndex|
                    var id = "%:%".format(pageIndex, stripIndex);
                    NS_ChannelStrip(id, pageGroup).pause
                }
            };

            inStrips   = numInStrips.collect { |channelIndex|
                var id = "I:%".format(channelIndex);
                NS_ChannelStripIn(id, inGroup).pause
            };

            swapGrid   = NS_SwapGrid(this);

            outMeter   = NS_ServerOutMeter(this);
            server.sync;
            action.value(this)
        })
    }

    buildServerFromSavedFile {}

    addSynthDef { |synthName, ugenGraph, action|
        SynthDef(synthName.asSymbol, ugenGraph).add(name, action)
    }

    addSynthDefCreateSynth { |group, synthName, ugenGraph, args, action|
        var synth;
        if(synthLib.at(synthName).notNil,{
            synth = Synth(synthName, args.asArray, group, \addToTail);
            action.value(synth)
        },{
            forkIfNeeded {
                this.addSynthDef(synthName, ugenGraph, { cond.signalOne });
                cond.wait { synthLib.at(synthName).notNil };
                synth = Synth.basicNew(synthName, server);
                synth.register;
                OSCFunc(
                    { cond.signalOne }, '/n_go', server.addr, nil, [synth.nodeID]
                ).oneShot;
                server.sendBundle(
                    server.latency, synth.addToTailMsg(group, args.asArray)
                );
                cond.wait { synth.isPlaying };
                action.value(synth)
            }
        })
    }

    printStats {
        "~SERVER STATS~\nCPUpeak   : %\nCPUavg    : %\nnumSynths : %\nnumUgens  : %\n"
        .format(
            server.peakCPU,
            server.avgCPU,
            server.numSynths,
            server.numUGens
        ).postln
    }

    save {
        var saveArray = List.newClear(0);
        var ctrlDict  = Dictionary();
        NS_Controller.allActive.do({ |ctrl| 
            ctrlDict.put(ctrl.asSymbol, ctrl.save)
        });

        saveArray.add( options.save );
        saveArray.add( swapGrid.save );
        saveArray.add( outStrips.collect({ |strip| strip.save }) );
        saveArray.add( strips.deepCollect(2, { |strip| strip.save }) );
        saveArray.add( inStrips.collect({ |strip| strip.save }) );
        saveArray.add( ctrlDict );

        ^saveArray;
    }

    loadCheck { |nsOptions|
        var currentOptions = options.save;
        var savedOptions   = nsOptions.select({ |o, i| o != currentOptions[i] });
        var optionNames    = NS_ServerOptions.instVarNames;
        var passArray      = Array.fill(currentOptions.size, { false });

        nsOptions.do({ |option, index|

            if(option != currentOptions[index], {
                passArray[index] = optionNames[index]
            },{
                passArray[index] = true
            });
        });

        passArray = passArray.select({ |p| p.class == Symbol });

        if(passArray.size > 0, {
            if(passArray.asString.contains("Device"), { 
                "saved device different from current device, confirm compatibility".warn
            },{
                "NS_ServerOptions % are incompatible with saved file".format(passArray).warn
                ^false
            });
        });

        ^true
    }

    load { |loadArray|

        if(this.loadCheck(loadArray[0])) {

            strips.deepDo(2, { |strp| strp.free });
            outStrips.do({ |strp| strp.free });

            {
                // load controllers
                loadArray[5].keysValuesDo({ |ctrl, ctrlArray|
                    if(NS_Controller.allActive.includes(ctrl.asClass),{
                        ctrl.asClass.load(ctrlArray, cond, { cond.signalOne });
                        cond.wait { ctrl.asClass.loaded }
                    });
                });

                // load inputStrips
                loadArray[4].do({ |inStrip, index| 
                    var strip = inStrips[index];
                    strip.load(inStrip, cond, { cond.signalOne });
                    cond.wait { strip.loaded }
                });

                // load strips
                loadArray[3].do({ |pageArray, pageIndex|
                    pageArray.do({ |stripArray, stripIndex|
                        var strip = strips[pageIndex][stripIndex];
                        strip.load(stripArray, cond, { cond.signalOne });
                        cond.wait { strip.loaded }
                    })
                });

                // load outStrips
                loadArray[2].do({ |outStrip, index| 
                    var strip = outStrips[index];
                    strip.load(outStrip, cond, { cond.signalOne });
                    cond.wait { strip.loaded }
                });

                // load swapGrid
                swapGrid.load(loadArray[1], cond, { cond.signalOne });
            }.fork
        }
    }

    free {
        window.free;
        server.freeAll; // free all nodes
        server.quit({"ns_server: % quit".format(name).postln})
    }
}
