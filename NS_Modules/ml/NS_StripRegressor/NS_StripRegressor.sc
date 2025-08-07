NS_StripRegressor : NS_SynthModule { // subclass NS_ControlModule? 
    classvar maxNumCtrls = 36, numModels = 6;
    var inputDS, outputDS, inputBuf, outputBuf;
    var currentMLP = 0, numCtrls = 0, idCount;
    var predicting = false;
    var trainRout;
    var mlps, meters, modelNames, saveButtons, loadButtons, lossView;

    // populate sets the right size for MLP and outputBuf:
    // what happens if MLPmeters are manually filled?
    // maybe every time a meter gets updated it calls .clear? 

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];

        var cond = CondVar(); //nsServer.cond
        this.initModuleArrays(7);
        idCount = Array.fill(numModels,{ 0 });

        {
            inputDS  = numModels.collect({ FluidDataSet(modGroup.server) });
            outputDS = numModels.collect({ FluidDataSet(modGroup.server) });
            inputBuf = Buffer.alloc(modGroup.server, 4, completionMessage: { cond.signalOne });
            cond.wait { inputBuf.numFrames == 4 };

            mlps = numModels.collect({ FluidMLPRegressor(modGroup.server) });

            meters = Array.fill(maxNumCtrls,{ NS_MLPMeter() });

            trainRout = Routine({
                loop {
                    mlps[currentMLP].fit(inputDS[currentMLP], outputDS[currentMLP], { |loss|
                        //"mlp % loss: %".format(currentMLP, loss).postln  
                        { lossView.string_( loss.round(0.00001)) }.defer
                    });
                    0.03.wait;
                }
            });

            4.do({ |index|
                controls[index] = NS_Control(("ctl" ++ index).asSymbol, ControlSpec(0, 1, \lin), 0.5)
                .addAction(\synth,{ |c| this.predict });
            });

            controls[4] = NS_Control(\whichMLP, ControlSpec(0, numModels - 1, \lin, 1), 0)
            .addAction(\synth,{ |c| 
                mlps[c.value].size({ |sz|
                    if(sz > 0, { this.switchMLP(c.value) })
                })
            }, false);

            controls[5] = NS_Control(\predict, ControlSpec(0, 1, \lin, 1), 0)
            .addAction(\synth,{ |c| predicting = c.value.asBoolean  }, false);

            controls[6] = NS_Control(\train, ControlSpec(0, 1, \lin, 1), 0) 
            .addAction(\synth,{ |c| this.trainMLP( c.value.asBoolean ) }, false);

            modelNames = numModels.collect({ StaticText().background_(Color.white).align_(\center) });

            saveButtons = numModels.collect({ |modelIndex|
                NS_Button([
                    ["save", NS_Style('textDark'), NS_Style('bGroundLight')]
                ])
                .maxWidth_(45)
                .addLeftClickAction({
                    Dialog.savePanel(
                        { |path| this.saveModel(path, modelIndex) },
                        nil,
                        PathName(NS_StripRegressor.filenameSymbol.asString).pathOnly +/+ "data/"
                    )
                })
            });

            loadButtons = numModels.collect({ |modelIndex|
                NS_Button([
                    ["load", NS_Style('textDark'), NS_Style('bGroundLight')]
                ])
                .maxWidth_(45)
                .addLeftClickAction({
                    FileDialog(
                        { |path| this.loadModel(path, modelIndex) },
                        nil,
                        2,
                        0,
                        true,
                        PathName(NS_StripRegressor.filenameSymbol.asString).pathOnly +/+ "data/"
                    )
                })
            });

            lossView = StaticText().background_(Color.white).align_(\center);

            { this.makeModuleWindow }.defer;
            loaded = true;
        }.fork(AppClock)
    }

    makeModuleWindow {
        var meterViews, loadControls;
        var modSlots = (0..(strip.slots.size - 1));
        var meterStream = Pseq(meters).asStream;
        modSlots.removeAt(strip.slots.indexOf(this)); 

        this.makeWindow("StripRegressor", Rect(0,0,690,390));

        meterViews = modSlots.collect({ VLayout(nil) });

        loadControls = { |slot, view|
            var module = strip.slots[slot];
            module.controls.reverseDo({ |ctrl, ctrlIndex|
                var meter = meterStream.next;

                meter !?
                {
                    meter.assignControl(module, ctrl);
                    meterViews[view].insert(meter, 0)
                } ?? 
                {
                    "maxNumCtrls reached".warn
                }
            })
        };

        win.layout_(
            HLayout(
                NS_ContainerView()
                .layout_(
                    GridLayout.columns(
                        *modSlots.collect({ |slotIndex, viewIndex|
                            [
                                NS_Button([
                                    ["add controls", NS_Style('textDark'), NS_Style('bGroundLight')]
                                ])
                                .minWidth_(120)
                                .addLeftClickAction({
                                    var module = strip.slots[slotIndex];
                                    if(module.notNil and: { module.isKindOf(this.class).not }, {
                                        loadControls.(slotIndex, viewIndex)
                                    });
                                }),
                                meterViews[viewIndex],
                                NS_Button([
                                    ["clear controls", NS_Style('textDark'), NS_Style('bGroundLight')]
                                ])
                            ]
                        })
                    ).spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins')),
                ),
                View()
                .maxWidth_(300)
                .layout_(
                    VLayout(
                        NS_ControlFader(controls[0], 0.001),
                        NS_ControlFader(controls[1], 0.001),
                        NS_ControlFader(controls[2], 0.001),
                        NS_ControlFader(controls[3], 0.001),
                        HLayout( 
                            NS_ControlSwitch(controls[4],(0..(numModels - 1)))
                            .maxWidth_(30),
                            GridLayout.rows(
                                *numModels.collect({ |i| 
                                    [modelNames[i], saveButtons[i], loadButtons[i]]
                                })
                            )
                        ),
                        GridLayout.rows(
                            [
                                NS_Button([
                                    ["populate", NS_Style('textDark'), NS_Style('bGroundLight')]
                                ])
                                .addLeftClickAction({ this.clearAll.populate }),
                                NS_Button([
                                    ["add point", NS_Style('textDark'), NS_Style('bGroundLight')]
                                ])
                                .addLeftClickAction({ this.addPoint })
                            ],
                            [
                                // make this a controlButton
                                NS_Button([
                                    ["rand points", NS_Style('textDark'), NS_Style('bGroundLight')]
                                ])
                                .addLeftClickAction({ this.randPoints }),
                                NS_ControlButton(controls[5],["predict", "stop Predict"]),
                            ],
                            [
                                lossView,
                                NS_ControlButton(controls[6],["train", "stop train"]),
                            ],
                            [
                                NS_Button([
                                    ["clear MLP", NS_Style('textDark'), NS_Style('bGroundLight')]
                                ])
                                .addLeftClickAction({ this.clearMLP }),
                                NS_Button([
                                    ["clear all", NS_Style('textDark'), NS_Style('bGroundLight')]
                                ])
                                .addLeftClickAction({ this.clearAll }),
                            ]
                        )
                    ).spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'))
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
        if(predicting,{
            controls[0..3].do(_.normValue_(1.0.rand))
        },{
            meters.do({ |meter| meter.control !? { meter.control.normValue_(1.0.rand) } })
        })
    }

    // needs a way to clear the MLP, maybe everytime a Meter clears?

    addPoint {
        var inVals  = 4.collect({ |i| controls[i].normValue });
        var outVals = meters.collect({ |meter| meter.control !? { meter.control.normValue } ?? 0 });

        inputBuf.setn(0, inVals);
        outputBuf.setn(0, outVals);

        // add a point cluster via some noise
        fork{
            3.do({
                var rand = { 0.0003.rand2 } ! 4;

                inputDS[currentMLP].addPoint(idCount[currentMLP],inputBuf);
                outputDS[currentMLP].addPoint(idCount[currentMLP],outputBuf);
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
        var meterIndex = 0;
        var assignModule = { |mod|
            mod.controls.do({ |ctrl, ctrlIndex|
                if(meterIndex < maxNumCtrls and: {ctrl.label != "bypass"},{
                    { meters[meterIndex].assignControl(mod, mod.slotIndex, ctrlIndex) }.defer;
                    meterIndex = meterIndex + 1;
                })
            })
        };

        strip.slots.do({ |module| 
            if(module.notNil and: {module.isKindOf( this.class ).not},{ assignModule.(module) })
        });

        numCtrls = meterIndex;

        mlps = numModels.collect({
            FluidMLPRegressor(
                modGroup.server,
                [((numCtrls - 4) / 2).asInteger.max(8)], // hidden layers
                FluidMLPRegressor.sigmoid, // activation between neurons, sigmoid == 0 < x < 1
                FluidMLPRegressor.sigmoid // outputActivation...how this is different than above?
            ).learnRate_(0.1).momentum_(0.9)
        });

        outputBuf = Buffer.loadCollection(modGroup.server, 0 ! numCtrls);
    }

    trainMLP { |bool| if(bool, { trainRout.reset.play }, { trainRout.stop }) }

    predict {
        if(predicting, {
            var inVals = 4.collect({ |i| controls[i].normValue });
            inputBuf.setn(0, inVals);
            mlps[currentMLP].predictPoint(inputBuf, outputBuf, {
                outputBuf.getn(0,numCtrls,{ |values|
                    values.do({ |val, index|
                        meters[index].control !? { meters[index].control.normValue_(val) }
                    });
                })
            })
        })
    }

    clearMLP {
        idCount[currentMLP] = 0;
        inputDS[currentMLP].clear;
        outputDS[currentMLP].clear;
        mlps[currentMLP].clear
    }

    clearAll { 
        idCount = Array.fill(numModels,{ 0 });
        controls[0..3].do(_.normValue_(0.5));
        inputDS.do(_.clear);
        outputDS.do(_.clear);
        mlps.do(_.clear);
        outputBuf !? { outputBuf.free; outputBuf = nil};
        { meters.do(_.free) }.defer;
    }

    freeExtra {
        inputDS.do(_.free);
        outputDS.do(_.free);
        mlps.do(_.free);
        inputBuf.free;
        outputBuf.free;
    }

    saveModel { |path, index|
        File.mkdir(path);

        inputDS[index].write(path +/+ "inDataSet%.json".format(index));
        outputDS[index].write(path +/+ "outDataSet%.json".format(index));
        mlps[index].write(path +/+ "model%.json".format(index));

        modelNames[index].string_(PathName(path).fileNameWithoutExtension)
    }

    loadModel { |path, index|
        inputDS[index].read(path +/+ "inDataSet%.json".format(index));
        outputDS[index].read(path +/+ "outDataSet%.json".format(index));
        mlps[index].read(path +/+ "model%.json".format(index));

        inputDS[index].size({ |size| idCount[currentMLP] = size });

        mlps[index].dump({ |dict|
            numCtrls = dict["layers"].last["cols"];
            outputBuf = Buffer.loadCollection(modGroup.server, 0 ! numCtrls); 
        });

        modelNames[index].string_( PathName(path).fileNameWithoutExtension );
    }

    saveExtra { |saveArray|

        // collect meter slotIndexes
        // collect paths for loaded/saved models?

        ^saveArray
    }

    loadExtra { |loadArray| /* see above */ }


    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageSwitch(numModels, width: "15%"),
            OpenStageXY(),
        ], columns: 3, randCol:true).oscString("StripRegressor")
    }
}
