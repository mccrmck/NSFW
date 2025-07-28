NS_StripRegressor : NS_SynthModule { // subclass NS_ControlModule? 
    classvar <isSource = false;
    classvar maxNumCtrls = 36, numModels = 6;
    var inputDS, outputDS, inputBuf, outputBuf;
    var currentMLP = 0, numCtrls = 0, idCount;
    var predicting = false;
    var trainRout;
    var mlps, meters, modelNames, saveButtons, loadButtons, lossView;

    // populate sets the right size for MLP and outputBuf - what happens if MLPmeters are manually filled?
    // maybe every time a meter gets updated it calls .clear? 

    init {
        var cond = CondVar();
        this.initModuleArrays(12);
        idCount = Array.fill(numModels,{ 0 });

        {
            inputDS  = numModels.collect({ FluidDataSet(modGroup.server) });
            outputDS = numModels.collect({ FluidDataSet(modGroup.server) });
            inputBuf = Buffer.alloc(modGroup.server, 4, completionMessage: { cond.signalOne });
            cond.wait { inputBuf.numFrames == 4 };

            mlps = numModels.collect({ FluidMLPRegressor(modGroup.server) });

            meters = Array.fill(maxNumCtrls,{ NS_MLPMeter(this) });

            trainRout = Routine({
                loop {
                    mlps[currentMLP].fit(inputDS[currentMLP], outputDS[currentMLP], { |loss|
                        //"mlp % loss: %".format(currentMLP, loss).postln  
                        { lossView.string_( loss.round(0.00001)) }.defer
                    });
                    0.05.wait;
                }
            });

            4.do({ |index|
                controls[index] = NS_Control(("ctl" ++ index).asSymbol,ControlSpec(0,1,\lin),0.5)
                .addAction(\synth,{ |c| if(predicting,{ this.predict }) });
            });

            controls[4] = NS_Control(\whichMLP,ControlSpec(0,numModels - 1,\lin,1),0)
            .addAction(\synth,{ |c| this.switchMLP( c.value ) },false);
            
            controls[5] = NS_Control(\populate,ControlSpec(0,0,\lin,1),0)
            .addAction(\synth,{ |c| this.clearAll.populate }, false);

            controls[6] = NS_Control(\point,ControlSpec(0,0,\lin,1),0)
            .addAction(\synth,{ |c| this.addPoint }, false);

            controls[7] = NS_Control(\rand,ControlSpec(0,0,\lin,1),0)
            .addAction(\synth,{ |c| this.randPoints }, false);

            controls[8] = NS_Control(\predict,ControlSpec(0,1,\lin,1),0)
            .addAction(\synth,{ |c| predicting = c.value.asBoolean  }, false);

            controls[9] = NS_Control(\train,ControlSpec(0,1,\lin,1),0) 
            .addAction(\synth,{ |c| this.trainMLP( c.value.asBoolean ) }, false);

            controls[10] = NS_Control(\clearMLP,ControlSpec(0,0,\lin,1),0)
            .addAction(\synth,{ |c| this.clearMLP },false);

            controls[11] = NS_Control(\clearAll,ControlSpec(0,0,\lin,1),0)
            .addAction(\synth,{ |c| this.clearAll },false);
            
            modelNames = numModels.collect({ StaticText().background_(Color.white).align_(\center) });

            saveButtons = numModels.collect({ |modelIndex|
                Button()
                .maxWidth_(45)
                .states_([["save"]])
                .action_({
                    Dialog.savePanel(
                        { |path| this.saveModel(path, modelIndex) },
                        nil,
                        PathName(NS_StripRegressor.filenameSymbol.asString).pathOnly +/+ "data/"
                    )
                })
            });

            loadButtons = numModels.collect({ |modelIndex|
                Button()
                .maxWidth_(45)
                .states_([["load"]])
                .action_({
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

            this.makeWindow("StripRegressor", Rect(0,0,720,420));

            win.layout_(
                HLayout(
                    GridLayout.columns( *meters.clump(maxNumCtrls / 4) ),
                    View().maxWidth_(300).layout_(
                        VLayout(
                            NS_ControlFader(controls[0], 0.001),
                            NS_ControlFader(controls[1], 0.001),
                            NS_ControlFader(controls[2], 0.001),
                            NS_ControlFader(controls[3], 0.001),
                            HLayout( 
                                NS_ControlSwitch(controls[4],(0..(numModels - 1))),
                                VLayout( 
                                    *numModels.collect({ |i| 
                                        HLayout( modelNames[i], saveButtons[i], loadButtons[i] )
                                    })
                                )
                            ),
                            HLayout( 
                                NS_ControlButton(controls[5],["populate"]),
                                NS_ControlButton(controls[6],["addPoint"]),
                            ),
                            HLayout(
                                NS_ControlButton(controls[7],["random"]),
                                NS_ControlButton(controls[8],["predict","stop Predict"]),
                            ),
                            HLayout( 
                                lossView,
                                NS_ControlButton(controls[9],["train","stop train"]),
                            ),
                            HLayout( 
                                NS_ControlButton(controls[10],["clearMLP"]),
                                NS_ControlButton(controls[11],["clearAll"]),
                            ),
                        ).margins_(0).spacing_(2)
                    )
                )
            );

            win.layout.spacing_(4).margins_(4)
        }.fork(AppClock)
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

        // should this also be able to accept NS_ControlModules?
        strip.inSink.module.do({ |module|
            if(module.isKindOf(NS_SynthModule),{ assignModule.(module) })
        });

        strip.moduleArray.do({ |module| 
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

    trainMLP { |bool| if(bool,{ trainRout.reset.play },{ trainRout.stop }) }

    predict {
        var inVals = 4.collect({ |i| controls[i].normValue });
        inputBuf.setn(0, inVals);
        mlps[currentMLP].predictPoint(inputBuf, outputBuf, {
            outputBuf.getn(0,numCtrls,{ |values|
                values.do({ |val, index|
                   meters[index].control !? { meters[index].control.normValue_(val) }
                });
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
        {meters.do(_.free)}.defer;
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
