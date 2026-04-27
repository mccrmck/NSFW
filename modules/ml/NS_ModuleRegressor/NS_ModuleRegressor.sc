NS_ModuleRegressor : NS_SynthModule {
    classvar numModels = 6, numModules = 6;
    var inputDS, outputDS, inputBuf, outputBuf;
    var currentMLP = 0, idCount;
    var ctrlArray, numCtrls = 0;
    var predicting = false;
    var trainRout;
    var mlps;
    var lossView;


    var modules, modGroups;
    var moduleViews;


    // this needs a complete rework:
    // this module should host 4-6 other modules, collect their controlDicts etc.

    buildSynthModule {

        modules = Array.newClear(numModules);
        modGroups = numModules.collect { Group(modGroup, \addToTail) };
        idCount = Array.fill(numModels, { 0 });

        {
            inputDS  = numModels.collect { FluidDataSet(nsServer.server) };
            outputDS = numModels.collect { FluidDataSet(nsServer.server) };
            inputBuf = Buffer.alloc(nsServer.server, 4, completionMessage: { 
                nsServer.cond.signalOne
            });

            nsServer.cond.wait { inputBuf.numFrames == 4 };

            mlps = numModels.collect { 
                FluidMLPRegressor(
                    nsServer.server,
                    hiddenLayers:     [8],
                    activation:       FluidMLPRegressor.sigmoid,
                    outputActivation: FluidMLPRegressor.sigmoid,
                    learnRate:        0.1,
                    momentum:         0.9
                )
            };

            trainRout = Routine {
                loop {
                    mlps[currentMLP].fit(inputDS[currentMLP], outputDS[currentMLP], { |loss|
                        { 
                            lossView.string_( 
                                "mlp % loss: %".format(
                                    currentMLP, loss.round(1e-5)
                                )
                            )
                        }.defer
                    });
                    0.03.wait;
                }
            };

            numModules.do { |modIndex|
                controlDict.add(
                    NS_ControlString("module" ++ modIndex, "")
                    .addAction(\module, { |c| 
                        if(c.value.size > 0) {
                            var className = ("NS_" ++ c.value).asSymbol.asClass;
                            this.addModule(className, modIndex);
                        } { 
                            this.freeModule(modIndex) 
                        }
                    }, false)
                )
            };

            4.do { |index|
                controlDict.add(
                    NS_ControlFloat("ctl" ++ index, ControlSpec(0, 1), 0.5)
                    .addAction(\synth, { |c| this.predict })
                )
            };

            numModels.do { |index|
                controlDict.add(
                    NS_ControlString("mlp" ++ index, "")
                    .addAction(\synth, { |c| 
                        // load mlps[index] here
                    })
                )
            };

            controlDict.addAll(
                NS_ControlInt(\whichMLP, 0, numModels - 1, 0)
                .addAction(\synth, { |c| this.switchMLP(c.value) }, false),

                NS_ControlInt(\predict, 0, 1, 0)
                .addAction(\synth, { |c| predicting = c.value.asBoolean }, false),
            );

            loaded = true;
        }.fork(AppClock)
    }

    addModule { |className, modIndex| 
        forkIfNeeded {
            modules[modIndex].free;
            modules[modIndex] = className.new(modGroups[modIndex], modBus);
            this.resize;
            if(modView.isClosed.notNil) { 
                { moduleViews[modIndex].refresh }.defer
            };
            //if(this.paused) { modules[modIndex].pause };
            //nsServer.cond.wait { modules[modIndex].loaded }
        }
    }

    freeModule { |modIndex|
        modules[modIndex].free;
        modules[modIndex] = nil;
        this.resize;
    }

    resize {
        this.clearAllMLPs;
        this.collectControls;
        this.resizeOutputBuf;
        numModels.do { |mlpIndex| this.resizeMLP(mlpIndex) }
    }

    collectControls {
        var tmpArray;
        modules.do { |mod|
            mod !? { 
                tmpArray = tmpArray.add( 
                    mod.controlDict.atAll(mod.controlDict.order)
                )
            }
        };
        ctrlArray = tmpArray !? { tmpArray.flat };
        numCtrls = ctrlArray !? { ctrlArray.size } ?? { 0 }
    }

    resizeOutputBuf { 
        fork {
            outputBuf.free;
            outputBuf = Buffer.loadCollection(
                nsServer.server, 0 ! numCtrls, 1, { nsServer.cond.signalOne }
            );
            nsServer.cond.wait { outputBuf.numFrames == numCtrls }
        }
    }
    
    resizeMLP { |mlpIndex|
        mlps[mlpIndex].hiddenLayers_([ ((numCtrls - 4) / 2).asInteger.max(8) ])
    }

    // needs to be overloaded when called by other modules on the strip
    gateBool {
        var mods = modules.reject { |m| m == nil };
        ^mods.collect { |m| m.gateBool }.reduce('or')
    }

    switchMLP { |index| 
        currentMLP = index.asInteger;
        this.predict
    }

    randPoints {
        if(predicting) {
            4.do { |i| controlDict[("ctl" ++ i).asSymbol].normValue_(1.0.rand) }
        } {
            ctrlArray.do { |c| c.normValue_(1.0.rand) }
        }
    }

    addPoint {
        var inVals = 4.collect { |i| controlDict[("ctl" ++ i).asSymbol].normValue };
        var outVals = ctrlArray.collect { |c| c.normValue };

        inputBuf.setn(0,  inVals);
        outputBuf.setn(0, outVals);

        // consider adding a cluster of points
        fork {
            inputDS[currentMLP].addPoint(idCount[currentMLP],  inputBuf);
            outputDS[currentMLP].addPoint(idCount[currentMLP], outputBuf);
            idCount[currentMLP] = idCount[currentMLP] + 1;

            inputBuf.setn(0, inVals);
            outputBuf.setn(0, outVals);
            nsServer.server.sync;

            96.do { "=".post };
            "\n".postln;
            inputDS[currentMLP].print;
            outputDS[currentMLP].print;
        };
    }

    clearAllMLPs { |resize(false)|
        numModels.do { |i| this.clearMLP(i, resize) }
    }

    clearMLP { |mlpIndex, resize(false)|
        idCount[mlpIndex] = 0;
        inputDS[mlpIndex].clear;
        outputDS[mlpIndex].clear;
        // unload any saved mlps:
        controlDict[("mlp" ++ mlpIndex).asSymbol].resetValue; 

        if(resize) { this.resizeMLP[mlpIndex] } { mlps[mlpIndex].clear }
    }

    resetModule {
        controlDict.controls.do(_.resetValue);
        outputBuf !? { outputBuf.free; outputBuf = nil };
    }

    trainMLP { |bool| 
        if(bool) { trainRout.reset.play } { trainRout.stop }
    }

    predict {
        if(predicting) {
            var inVals = 4.collect { |i| controlDict[("ctl" ++ i).asSymbol].normValue };
            inputBuf.setn(0, inVals);
            mlps[currentMLP].predictPoint(inputBuf, outputBuf, {
                outputBuf.getn(0, numCtrls, { |values|
                    ctrlArray.do { |c, i| c.normValue_(values[i]) }
                })
            })
        }
    }

    nsModuleLayout {
        var savePath = PathName(this.class.filenameSymbol.asString).pathOnly +/+ "data/";

        var saveButs = numModels.collect { |modelIndex|
            NS_Button(["save"]).maxHeight_(30).maxWidth_(45)
            .addLeftClickAction({
                FileDialog(
                    { |path| this.saveModel(path, modelIndex) },
                    nil, 2, 1, true, savePath 
                )
            })
        };

        var loadButs = numModels.collect { |modelIndex|
            NS_Button(["load"]).maxHeight_(30).maxWidth_(45)
            .addLeftClickAction({
                FileDialog(
                    { |path| this.loadModel(path, modelIndex) },
                    nil, 2, 0, true, savePath
                )
            })
        };

        moduleViews = numModules.collect { |i|
            NS_ContainerView().layout_(
                VLayout(
                    NS_ModuleSlotView(controlDict[("module" ++ i).asSymbol])
                    .maxHeight_(20),
                    modules[i] !? { modules[i].nsModuleLayout }
                ).nsMarginsSpacing('inner')
            )
        };

        lossView = NS_Text("");

        ^HLayout(
            VLayout(
                HLayout(
                    NS_Button(["clear current MLP"])
                    .addLeftClickAction({ this.clearMLP(currentMLP) }),
                    NS_Button(["reset module"])
                    .addLeftClickAction({ this.resetModule }),
                ),
                HLayout(
                    NS_ControlSwitch(controlDict['whichMLP'], (0..(numModels - 1)))
                    .fixedWidth_(20),
                    GridLayout.rows(
                        *numModels.collect { |i| 
                            [
                                NS_ControlText(controlDict[("mlp" ++ i).asSymbol])
                                .minWidth_(150)
                                .addRightClickAction({}), // disable editing text
                                saveButs[i],
                                loadButs[i]
                            ]
                        }
                    )
                ),
                NS_Button(["rand points"])
                .addLeftClickAction({ this.randPoints }), 
                NS_Button(["add point"])
                .addLeftClickAction({ this.addPoint }),
                NS_Button(["train", "stop train"])
                .addLeftClickAction({ |b| this.trainMLP(b.value.asBoolean) }),
                lossView,
                NS_ControlFader(controlDict['ctl0'], 0.001),
                NS_ControlFader(controlDict['ctl1'], 0.001),
                NS_ControlFader(controlDict['ctl2'], 0.001),
                NS_ControlFader(controlDict['ctl3'], 0.001),
                NS_ControlButton(controlDict['predict'], ["predict", "stop predict"]),
            ),
            VLayout(*moduleViews)
        )
    }

    freeExtra {
        inputDS.do(_.free);
        outputDS.do(_.free);
        inputBuf.free;
        outputBuf.free;
        mlps.do(_.free);
    }

    //// using the index in the file name means the mlps are confined to a specific index
    //// also, can we add a description to the name so I know which modules it's associated with?
    //// 
    //saveModel { |path, index|
    //    File.mkdir(path);
    //
    //    path.postln;
    //
    //    inputDS[index].write(path +/+ "inDataSet%.json".format(index));
    //    outputDS[index].write(path +/+ "outDataSet%.json".format(index));
    //    mlps[index].write(path +/+ "model%.json".format(index));
    //
    //    controls[4 + index].value_( path );
    //}
    //
    //loadModel { |path, index|
    //
    //    path.postln;
    //    inputDS[index].read(path +/+ "inDataSet%.json".format(index));
    //    outputDS[index].read(path +/+ "outDataSet%.json".format(index));
    //    mlps[index].read(path +/+ "model%.json".format(index));
    //
    //    inputDS[index].size({ |size| idCount[currentMLP] = size });
    //
    //    mlps[index].dump({ |dict|
    //        dict.postln;
    //        numCtrls = dict["layers"].last["cols"];
    //        this.resizeOutputBuf;
    //    });
    //
    //    controls[4 + index].value_( path );
    //}
    //
    //saveExtra { |saveArray|
    //    // add a default value for mlps that have data but aren't saved?
    //    // save strip.slots.collect({ |mod| mod.notNil.if{mod.class}{nil} })
    //
    //    ^saveArray.add([nil])
    //}
    //
    //loadExtra { |loadArray, cond, action| 
    //
    //    // obviously this is garbage
    //    // cond.wait { some condition here }
    //
    //    numModels.do({ |index|
    //        var folderName = controls[4 + index].value;
    //        folderName.postln;
    //        if(folderName.size > 0,{
    //            this.loadModel(folderName, index)
    //        })
    //    });
    //
    //    action.value
    //}

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageSwitch(numModels, width: "15%"),
            OpenStageXY(),
        ], columns: 3, randCol:true).oscString("StripRegressor")
    }
}
