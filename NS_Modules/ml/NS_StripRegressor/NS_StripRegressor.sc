NS_StripRegressor : NS_SynthModule { // subclass NS_ControlModule? 
    classvar maxNumCtrls = 36, numModels = 6;
    var inputDS, outputDS, inputBuf, outputBuf;
    var currentMLP = 0, numCtrls = 0, idCount;
    var predicting = false;
    var trainRout;
    var mlps, meters; 
    var modSlots;
    var meterViews, lossView;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];

        this.initModuleArrays(4 + numModels + 2);
        idCount = Array.fill(numModels, { 0 });

        {
            inputDS  = numModels.collect({ FluidDataSet(modGroup.server) });
            outputDS = numModels.collect({ FluidDataSet(modGroup.server) });
            inputBuf = Buffer.alloc(modGroup.server, 4, completionMessage: { nsServer.cond.signalOne });
            nsServer.cond.wait { inputBuf.numFrames == 4 };

            mlps = numModels.collect({ 
                FluidMLPRegressor(
                    modGroup.server,
                    hiddenLayers:     [8],
                    activation:       FluidMLPRegressor.sigmoid,
                    outputActivation: FluidMLPRegressor.sigmoid,
                    learnRate:        0.1,
                    momentum:         0.9
                )
            });

            modSlots = (0..(strip.slots.size - 1)).reject({ |i| 
                i == strip.slots.indexOf(this)
            });

            meters = Array.newClear(modSlots.size);

            trainRout = Routine({
                loop {
                    mlps[currentMLP].fit(inputDS[currentMLP], outputDS[currentMLP], { |loss|
                        { 
                            lossView.string_( "loss: %".format(loss.round(0.00001)) )
                        }.defer
                    });
                    0.03.wait;
                }
            });

            4.do({ |index|
                controls[index] = NS_Control("ctl" ++ index, ControlSpec(0, 1, \lin), 0.5)
                .addAction(\synth,{ |c| this.predict });
            });

            numModels.do({ |index|
                controls[4 + index] = NS_Control("mlp" ++ index, \string, "")
                // .addAction(\synth, { |c| })
            });

            controls[4 + numModels] = NS_Control(\whichMLP, ControlSpec(0, numModels - 1, \lin, 1), 0)
            .addAction(\synth,{ |c| 
                mlps[c.value].size({ |sz|
                    if(sz > 0, { this.switchMLP(c.value) })
                })
            }, false);

            controls[4 + numModels + 1] = NS_Control(\predict, ControlSpec(0, 1, \lin, 1), 0)
            .addAction(\synth,{ |c| predicting = c.value.asBoolean  }, false);

            { this.makeModuleWindow }.defer;
            loaded = true;
        }.fork(AppClock)
    }

    makeModuleWindow {
        var savePath = PathName(NS_StripRegressor.filenameSymbol.asString).pathOnly +/+ "data/";

        var saveButs = numModels.collect({ |modelIndex|
            NS_Button(["save"])
            .maxHeight_(30)
            .maxWidth_(45)
            .addLeftClickAction({
                Dialog.savePanel(
                    { |path| this.saveModel(path, modelIndex) },
                    nil, savePath 
                )
            })
        });

        var loadButs = numModels.collect({ |modelIndex|
            NS_Button(["load"])
            .maxHeight_(30)
            .maxWidth_(45)
            .addLeftClickAction({
                FileDialog(
                    { |path| this.loadModel(path, modelIndex) },
                    nil, 2, 0, true, savePath
                )
            })
        });

        meterViews = modSlots.collect({ View() });

        lossView = NS_Text("");

        this.makeWindow("StripRegressor", Rect(0, 0, 690, 390));

        win.layout_(
            VLayout(
                // control panel
                HLayout(
                    NS_ControlSwitch(controls[4 + numModels], (0..(numModels - 1)))
                    .fixedWidth_(30),
                    GridLayout.rows(
                        *numModels.collect({ |i| 
                            [
                                NS_ControlText(controls[4 + i])
                                .minWidth_(150)
                                .addRightClickAction({}), // disable editing text
                                saveButs[i],
                                loadButs[i]
                            ]
                        })
                    ),
                    GridLayout.rows(
                        [[ NS_ControlFader(controls[0], 0.001), columns: 2 ]],
                        [[ NS_ControlFader(controls[1], 0.001), columns: 2 ]],
                        [[ NS_ControlFader(controls[2], 0.001), columns: 2 ]],
                        [[ NS_ControlFader(controls[3], 0.001), columns: 2 ]],
                        [
                            NS_Button(["populate"])
                            .addLeftClickAction({ this.resetModule.populate }),
                            NS_Button(["add point"])
                            .addLeftClickAction({ this.addPoint })
                        ],
                        [
                            NS_Button(["rand points"])
                            .addLeftClickAction({ this.randPoints }),
                            NS_ControlButton(controls[4 + numModels + 1], ["predict", "stop predict"]),
                        ],
                        [
                            lossView,
                            NS_Button(["train", "stop train"])
                            .addLeftClickAction({ |b| this.trainMLP( b.value.asBoolean ) })
                        ],
                        [
                            NS_Button(["clear MLP"])
                            .addLeftClickAction({ this.clearMLP(currentMLP) }),
                            NS_Button(["clear all"])
                            .addLeftClickAction({ this.resetModule }),
                        ]
                    )
                ),
                // module panel
                NS_ContainerView()
                .layout_(
                    GridLayout.columns(
                        *modSlots.collect({ |slotIndex, viewIndex|
                            [
                                NS_Button(["add module"])
                                .minWidth_(120)
                                .addLeftClickAction({
                                    this.clearModuleControls(viewIndex);
                                    this.addModuleControls(slotIndex, viewIndex)
                                }),
                                meterViews[viewIndex],
                            ]
                        })
                    )
                )
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'));
    }

    switchMLP { |index| 
        currentMLP = index.asInteger;
        this.predict
    }

    randPoints {
        if(predicting, {
            controls[0..3].do(_.normValue_(1.0.rand))
        },{
            meters.deepDo(2, { |meter| 
                meter !? { meter.control.normValue_(1.0.rand) }
            })
        })
    }

    // needs a way to clear the MLP, maybe everytime a Meter clears?

    addPoint {
        var inVals  = 4.collect({ |i| controls[i].normValue });
        var outVals = List.newClear(0);

        meters.flat.collect({ |meter|
            meter !? { outVals.add(meter.control.normValue) }
        });

        inputBuf.setn(0, inVals);
        outputBuf.setn(0, outVals);

        // add a point cluster via some noise
        fork{
            3.do({
                var rand = { 0.0003.rand2 } ! 4;

                inputDS[currentMLP].addPoint(idCount[currentMLP],  inputBuf);
                outputDS[currentMLP].addPoint(idCount[currentMLP], outputBuf);
                idCount[currentMLP] = idCount[currentMLP] + 1;

                inputBuf.setn(0, (inVals + rand).clip(0,1) );
                outputBuf.setn(0, (outVals + rand).clip(0,1) );
                modGroup.server.sync;
            });

            inputDS[currentMLP].print;
            "\n".postln;
            outputDS[currentMLP].print;
            "\n".postln;
        };
    }

    populate {
        modSlots.do({ |slotIndex, viewIndex| 
            this.addModuleControls(slotIndex, viewIndex)
        });
    }

    addModuleControls { |slotIndex, viewIndex|
        var module = strip.slots[slotIndex];

        if(module.notNil and: { module.isKindOf(this.class).not }, {
            var modString = module.class.asString.split($_)[1];
            var tempMeters = List.newClear(0);

            module.controls.do({ |ctrl, ctrlIndex|
                if(ctrl.label != "bypass" and: { ctrl.spec != 'string' },{
                    var meter = NS_MLPMeter(ctrl);
                    tempMeters.add(meter);
                    numCtrls = numCtrls + 1;
                })
            });

            meters[viewIndex] = tempMeters;

            if(tempMeters.size < 13,{
                meterViews[viewIndex].layout_(
                    VLayout(
                        *[StaticText().align_(\center).string_(modString)] ++
                        tempMeters ++
                        [
                            nil,
                            NS_Button(["clear module"])
                            .addLeftClickAction({
                                this.clearModuleControls(viewIndex)
                            })
                        ]
                    ).spacing_(0).margins_(0)
                )
            },{
                meterViews[viewIndex].layout_(
                    VLayout(
                        StaticText().align_(\center).string_(modString), 
                        GridLayout.columns(
                            *tempMeters.clump((tempMeters.size / 2).ceil)
                        ),
                        nil,
                        NS_Button(["clear module"])
                        .addLeftClickAction({
                            this.clearModuleControls(viewIndex)
                        }),
                    ).spacing_(0).margins_(0)
                )
            })
        });

        // when numCtrls increases:
        // - datasets must be cleared
        // - mlps must be cleared
        // - outputBuf must be reallocated

        this.resetMLP(currentMLP);

        mlps.do({ |mlp|
            mlp.hiddenLayers_([((numCtrls - 4) / 2).asInteger.max(8)])
        });

        outputBuf.free;
        outputBuf = Buffer.loadCollection(modGroup.server, 0 ! numCtrls);
    }

    // remove module controls from module panel, update mlps and datasets
    clearModuleControls { |viewIndex|
        meterViews[viewIndex].children.do(_.free);
        meterViews[viewIndex].removeAll;
        meters[viewIndex] = nil;
        // reset numCtrls, datasets, and mlps

        // when a module is removed:
        // - datasets must be cleared
        // - mlps must be cleared
        // - outputBuf must be reallocated
        // - numCtrls must equal the number of remaining ctrls

    }

    // mlp could also use .clear to erase learning without resizing
    resetMLP { |mlpIndex|
        idCount[mlpIndex] = 0;
        inputDS[mlpIndex].clear;
        outputDS[mlpIndex].clear;
        mlps[mlpIndex].hiddenLayers_([((numCtrls - 4) / 2).asInteger.max(8)])
    }

    // what is the difference between reset and clear?
    clearMLP { |mlpIndex|
        idCount[mlpIndex] = 0;
        inputDS[mlpIndex].clear;
        outputDS[mlpIndex].clear;
        mlps[mlpIndex].clear
    }

    // could also be clearModule, be consistent!
    resetModule {
        numCtrls = 0;
        modSlots.do({ |slotIndex, viewIndex| this.clearModuleControls(viewIndex) });
        numModels.do({ |i| this.clearMLP(i) });
        controls[0..3].do(_.normValue_(0.5));
        outputBuf !? { outputBuf.free; outputBuf = nil };
    }

    trainMLP { |bool| if(bool, { trainRout.reset.play }, { trainRout.stop }) }

    predict {
        if(predicting, {
            var inVals = 4.collect({ |i| controls[i].normValue });
            inputBuf.setn(0, inVals);
            mlps[currentMLP].predictPoint(inputBuf, outputBuf, {
                outputBuf.getn(0, numCtrls, { |values|
                    values.do({ |val, index|
                        meters.flat[index].control.normValue_(val)
                    });
                })
            })
        })
    }

    freeExtra {
        inputDS.do(_.free);
        outputDS.do(_.free);
        inputBuf.free;
        outputBuf.free;
        mlps.do(_.free);
    }

    // saveModel { |path, index|
    //     File.mkdir(path);

    //     inputDS[index].write(path +/+ "inDataSet%.json".format(index));
    //     outputDS[index].write(path +/+ "outDataSet%.json".format(index));
    //     mlps[index].write(path +/+ "model%.json".format(index));

    //     controls[4 + index].value_( PathName(path).fileNameWithoutExtension );
    // }

    // loadModel { |path, index|
    //     inputDS[index].read(path +/+ "inDataSet%.json".format(index));
    //     outputDS[index].read(path +/+ "outDataSet%.json".format(index));
    //     mlps[index].read(path +/+ "model%.json".format(index));

    //     inputDS[index].size({ |size| idCount[currentMLP] = size });

    //     mlps[index].dump({ |dict|
    //         numCtrls = dict["layers"].last["cols"];
    //         outputBuf = Buffer.loadCollection(modGroup.server, 0 ! numCtrls); 
    //     });

    //     controls[4 + index].value_( PathName(path).fileNameWithoutExtension );
    // }

    // saveExtra { |saveArray|

    // save checklist:
    // - save controls (duh)
    // - save models, give default name if not already saved?
    // 

    //     ^saveArray
    // }

    // loadExtra { |loadArray| 

    // load checklist:
    // load controls (duh)
    // load saved models (might happen automatically with saved control string)
    // 

    //}


    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageSwitch(numModels, width: "15%"),
            OpenStageXY(),
        ], columns: 3, randCol:true).oscString("StripRegressor")
    }
}
