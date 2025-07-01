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
    var <cond;
    var <synthLib;

    *new { |name, nsOptions, action|
        ^super.newCopyArgs(name).init(nsOptions, action)
    }

    init { |nsOptions, action|
        id = NS_ServerID.next;
        options = nsOptions;
        cond = CondVar();
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
                OSCFunc({
                    cond.signalOne
                }, '/n_go', server.addr, nil, [synth.nodeID]).oneShot;
                server.sendBundle(
                    server.latency, synth.addToTailMsg(group, args.asArray)
                );
                cond.wait { synth.isPlaying };
                action.value(synth)
            }
        })
    }

    printStats {
        "CPUpeak   : %\nCPUavg    : %\nnumSynths : %\nnumUgens  : %\n".format(
            server.peakCPU,
            server.avgCPU,
            server.numSynths,
            server.numUGens
        ).postln
    }
}

NS_MatrixServer : NS_Server {
    const <numPages  = 6;
    const <numStrips = 4;
    var <inGroup, pages, <pageGroups, <mixerGroup;
    var <inputs;
    var <strips, <outMixer, <swapGrid;

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

            swapGrid   = NS_MatrixSwapGrid(this);

            server.sync;
            action.value(this)
        })
    }

    save {
        var saveArray = List.newClear(0);
        var ctrlDict  = Dictionary();
        NS_Controller.allActive.do({ |ctrl| 
            ctrlDict.put(ctrl.asSymbol, ctrl.save)
        });

        saveArray.add( swapGrid.save );
        saveArray.add( outMixer.collect({ |strip| strip.save }) );
        saveArray.add( strips.deepCollect(2,{ |strip| strip.save }) );
        //saveArray.add( inputStrips.collect({ |strip| strip.save}) );
        saveArray.add([]); // dummy until I sort out the instrips
        saveArray.add(ctrlDict);

        ^saveArray;
    }

    load { |loadArray|
        {
            strips.deepDo(2,{ |strp| strp.free });
            outMixer.do({ |strp| strp.free });

            // load in reverse:
            loadArray[4].keysValuesDo({ |ctrl, ctrlArray|
                if(NS_Controller.allActive.includes(ctrl.asClass),{
                    ctrl.asClass.load(ctrlArray, cond, { cond.signalOne });
                    cond.wait { ctrl.asClass.loaded }
                });
            });

            loadArray[3].do({ |inStrip, index| 
                // cond.wait { strip.loaded == true }
            });
            
            loadArray[2].do({ |pageArray, pageIndex|
                pageArray.do({ |stripArray, stripIndex|
                    var strip = strips[pageIndex][stripIndex];
                    strip.load(stripArray, cond, { cond.signalOne });
                    cond.wait { strip.loaded }
                })
            });

            loadArray[1].do({ |outStrip, index| 
                var strip = outMixer[index];
                strip.load(outStrip, cond, { cond.signalOne });
                cond.wait { strip.loaded }
            });

            swapGrid.load(loadArray[0], cond, { cond.signalOne });
            
        }.fork
    }

    free {
        server.freeAll; // free all nodes
        server.quit({"ns_server: % quit".format(name).postln})
    }
}

NS_TimelineServer : NS_Server {

    buildServer {}
}
