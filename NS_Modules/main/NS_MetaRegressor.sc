NS_MetaRegressor : NS_SynthModule {
    classvar <isSource = false;
    classvar numCtrls = 24;
    var inputDS, outputDS, inputBuf, outputBuf;
    var predicting = false, idCount = 0;
    var mlp, meters;

    *initClass { ServerBoot.add{ } }

    init {
        var cond = CondVar();
        this.initModuleArrays(9);
        this.makeWindow("MetaRegressor", Rect(0,0,480,420));

        // synths.add( Synth(\ns_metaRegressor,[\bus,bus],modGroup) );

        {
            // do I always need fresh dataSets? Or can I just .read the state into the MLP?
            inputDS = FluidDataSet();
            outputDS = FluidDataSet();
            inputBuf = Buffer.alloc(modGroup.server, 4, completionMessage: { cond.signalOne });  // input dimensions
            cond.wait { inputBuf.numFrames == 4 };
            outputBuf = Buffer.loadCollection(modGroup.server, 0!24, action: { cond.signalOne }); // if MLPMeter.control == nil, buffer input is zero
            cond.wait { outputBuf.numFrames == 24 };

            // this should eventually be an Array, no?
            mlp = FluidMLPRegressor(
                modGroup.server,
                [8,16],                     // hidden layers
                FluidMLPRegressor.sigmoid, // activation (between neurons), simoid == 0 < x < 1
                FluidMLPRegressor.sigmoid, // outputActivation...not sure how this is different than above
            ).learnRate_(0.1).momentum_(0.9); 

            meters = Array.fill(numCtrls,{ NS_MLPMeter() }); // need to make these update the outputBuf

            4.do({ |index|
                controls[index] = NS_Control(("ctl" ++ index).asSymbol,ControlSpec(0,1,\lin),0.5)
                .addAction(\synth,{ |c| inputBuf.set(index, c.value); if(predicting,{ this.predict }) });
                assignButtons[index] = NS_AssignButton(this, index, \fader).maxWidth_(30);
            });

            controls[4] = NS_Control(\whichServer,ControlSpec(0,7,\lin,1),0)
            .addAction(\synth,{ |c| this.switchServer( c.value ) },false);
            assignButtons[4] = NS_AssignButton(this, 4, \switch).maxWidth_(30).maxHeight_(30);

            controls[5] = NS_Control(\rand,ControlSpec(0,1,\lin,1),0)        // make into button w/ 1 state
            .addAction(\synth,{ |c| this.randPoints }, false);
            assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

            controls[6] = NS_Control(\point,ControlSpec(0,1,\lin,1),0)            // make into button w/ 1 state
            .addAction(\synth,{ |c|  this.addPoint  }, false);
            assignButtons[6] = NS_AssignButton(this, 6, \button).maxWidth_(30);

            controls[7] = NS_Control(\train,ControlSpec(0,1,\lin,1),0)            // make into button w/ 1 state
            .addAction(\synth,{ |c|  this.trainMLP }, false);
            assignButtons[7] = NS_AssignButton(this, 7, \button).maxWidth_(30);

            controls[8] = NS_Control(\predict,ControlSpec(0,1,\lin,1),0)
            .addAction(\synth,{ |c| predicting = c.value.asBoolean  }, false);
            assignButtons[8] = NS_AssignButton(this, 8, \button).maxWidth_(30);






            win.layout_(
                VLayout(
                    GridLayout.columns( *meters.clump(numCtrls/3) ),
                    HLayout( NS_ControlSwitch(controls[4],(0..7),8), assignButtons[4] ),
                    HLayout( NS_ControlFader(controls[0]).round_(0.001), assignButtons[0] ),
                    HLayout( NS_ControlFader(controls[1]).round_(0.001), assignButtons[1] ),
                    HLayout( NS_ControlFader(controls[2]).round_(0.001), assignButtons[2] ),
                    HLayout( NS_ControlFader(controls[3]).round_(0.001), assignButtons[3] ),

                    HLayout( 
                        NS_ControlButton(controls[5],["random","random"]), assignButtons[5],
                        NS_ControlButton(controls[6],["addPoint","addPoint"]), assignButtons[6],
                        Button().states_([["populate"]])
                    ),
                    HLayout( 
                        NS_ControlButton(controls[7],["train","train"]), assignButtons[7],
                        NS_ControlButton(controls[8],["predict","notPredict"]), assignButtons[8],
                        Button().states_([["clear"]]).action_({ meters.do(_.clear) })
                    ),
                )
            );

            win.layout.spacing_(4).margins_(4)
        }.fork(AppClock)
    }

    // to test:
    // is it more efficient/faster to have 8 mlp regressors loaded, then query (.predictPoint) the one that's selected?
    // OR could it make sense to .load/.read a new state into the MLP when switching?
    // my guess is that the former is a better solution...

    switchServer { |index| "server: %".format(index).postln }

    randPoints {
        meters.do({ |meter|
            meter.control !? { meter.control.normValue_(1.0.rand) }
        })
    }

    // needs a way to clear the MLP, maaybe everytime a Meter clears?

    addPoint {
       var vals = meters.collect({ |meter, index|
            meter.control !? { meter.control.normValue } ?? 0
        });
        vals.postln;                  // for testing
        outputBuf.setn(0,vals);
        inputDS.addPoint(idCount,inputBuf);
        outputDS.addPoint(idCount,outputBuf);
        outputBuf.getn(0,numCtrls,{ |b| b.postln });   // for testing
        idCount = idCount + 1;

        inputDS.print;
        "\n".postln;
        outputDS.print;
        "\n".postln;
    }

    assignStripControls {
        // moduleSink.notNil
       // strip.moduleSinks.do({ |modSink|
      //      modSink !? modSink.module
      //  }) // nil Check
       // modSink.module  // nil Check
       // module.controls.do({ |control, index| meters[meterIndex].assignControl(module, index) })

    }

    assignStripControlsRand {}

    trainMLP {
        mlp.fit(inputDS,outputDS,{ |loss|
            "loss: %".format(loss).postln  // eventually add a StaticText() to display loss?
        })
    }

    predict {
        mlp.predictPoint(inputBuf, outputBuf, {
            outputBuf.getn(0,numCtrls,{ |values|
                values.postln;
                meters.do({ |meter, index|
                    if(meter.control.notNil,{
                        meter.control.normValue_( values[index] )
                    })
                })
            })
        })
    }

    // maybe make a method that auto-assigns all args from active modules in a ChannelStrip?
    // lots of nil checks, maybe certain kinds of args are excluded (like bypass args?)

    // *oscFragment {       
    //     ^OSC_Panel(widgetArray:[
    //         OSC_XY(snap:true),
    //         OSC_Fader("15%",snap:true),
    //         OSC_Panel("15%",horizontal:false,widgetArray: [
    //             OSC_Fader(),
    //             OSC_Button(height:"20%")
    //         ])
    //     ],randCol:true).oscString("RingMod")
    // }
}
