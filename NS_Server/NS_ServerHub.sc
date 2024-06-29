NS_ServerHub {
    classvar <servers;
    classvar <inModules;
    classvar win;
    classvar buttons, inModuleViews, inView;

    *initClass {
        servers = Dictionary();
    }

    *boot { |blockSizeArray|
        var cond = CondVar();
        fork{
            { this.makeWindow }.defer;
            blockSizeArray.do({ |blockSize, index|
                var name = ("nsfw_%".format(index) ).asSymbol;
                servers.put(name, NS_Server(name,blockSize,{cond.signalOne}));
                cond.wait({ servers[name].options.blockSize == blockSize });
            });
        };
    }

    *makeWindow {
        var numIns = NSFW.numInChans;
        var visModuleIndex = 0;
        var bounds = Window.availableBounds;
        var gradient = Color.rand; /*Color.fromHexString("#7b14ba")*/

        buttons = View()
        .maxWidth_(460)
        .maxHeight_(60)
        .layout_(
            VLayout(
                HLayout(
                    Button()
                    .states_([["show module list"],["show module list"]])
                    .action_({ |but| 
                        if(but.value == 0,{
                            NS_ModuleList.close
                        },{
                            NS_ModuleList.open      
                        })
                    }),
                    Button().states_([["save setup"]]),
                    Button().states_([["load setup"]]),
                    Button().states_([["recording maybe?"]]),
                ),
                HLayout( NS_Switch(["nsfw_0","nsfw_1","active","servers"],{ |switch| },'horz'), NS_AssignButton().maxWidth_(60) )
            ).margins_(0)
        );

        inModules = numIns.collect({ NS_InputModule( ) });

        inModuleViews = numIns.collect({ |moduleIndex| 
            View()
            .background_(Color.white.alpha_(0.15))
            .maxWidth_(220)
            .maxHeight_(240)
            .layout_(
                VLayout(
                    HLayout(
                        StaticText().string_("inBus:").stringColor_(Color.white),
                        DragSource()
                        .align_(\center)
                        .object_([moduleIndex])
                        .string_(moduleIndex)
                        .dragLabel_("inBus: %".format(moduleIndex))
                        .background_(Color.white)
                        .maxWidth_(45), 
                        Button()
                        .maxWidth_(45)
                        .states_([["▶",Color.green,Color.black],["X", Color.black, Color.red]])
                        .action_({ |but|
                            if(but.value == 1,{
                                // if stereo checkbox...
                                servers.do({ |server|

                                    inModules[moduleIndex].addMonoInput(moduleIndex, server)


                                })
                            },{
                                inModules[moduleIndex].free
                            })
                        }),
                        StaticText()
                        .string_("stereo:")
                        .stringColor_(Color.white),
                        CheckBox()
                    ),
                    inModules[moduleIndex]
                )
            ).visible_( false )
        });

        inView = View()
        .maxWidth_(460)
        .maxHeight_(240)
        .layout_(
            HLayout(
                GridLayout.rows(
                    *inModuleViews.clump(2)
                ),
                VLayout(
                    Button()
                    .maxWidth_(15)
                    .maxHeight_(120)
                    .states_([["↑"]])
                    .action_({ |but|
                        inModuleViews[visModuleIndex * 2].visible_(false);
                        inModuleViews[visModuleIndex * 2 + 1].visible_(false);
                        visModuleIndex = (visModuleIndex - 1).clip(0, (numIns / 2) - 1);
                        inModuleViews[visModuleIndex * 2].visible_(true);
                        inModuleViews[visModuleIndex * 2 + 1].visible_(true);
                    })
                    .valueAction_(0),
                    Button()
                    .maxWidth_(15)
                    .maxHeight_(120)
                    .states_([["↓"]])
                    .action_({ |but|
                        inModuleViews[visModuleIndex * 2].visible_(false);
                        inModuleViews[visModuleIndex * 2 + 1].visible_(false);
                        visModuleIndex = (visModuleIndex + 1).clip(0, (numIns / 2) - 1);
                        inModuleViews[visModuleIndex * 2].visible_(true);
                        inModuleViews[visModuleIndex * 2 + 1].visible_(true);
                    }),
                )
            ).margins_(0)
        );

        win = Window("NSFW Server Hub",Rect(bounds.width-480,bounds.height-240,480,315));
        win.layout_(
            VLayout(
                buttons,
                inView
            )
        );

        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        win.view.maxWidth_(480).maxHeight_(315);

        win.alwaysOnTop_(true);
        win.front;

        win.onClose_({
            // free all modules
            // free all servers 
            // close all windows
            // thisProcess.recompile
        })
    }
}

NS_InputModule : NS_SynthModule {
    var <view;
    var <rms, localResponder;

    *initClass {
        StartUp.add{
            SynthDef(\ns_inputMono,{
                var sig = SoundIn.ar(\inBus.kr());

                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\inAmp.kr(0));

                sig = Squish.ar(sig,sig,\dbThresh.kr(-12), \compAtk.kr(0.01), \compRls.kr(0.1), \ratio.kr(2), \knee.kr(0.01),\dbMakeUp.kr(0));
                SendPeakRMS.ar(sig,10,3,'/inSynth',0);

                Out.ar(\outBus.kr, sig!2 )
            }).add;

            SynthDef(\ns_inputStereo,{
                var inBus = \inBus.kr();
                var sig = SoundIn.ar([inBus,inBus + 1]);

                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\inAmp.kr(0));

                sig = Squish.ar(sig,sig.sum * -3.dbamp,\dbThresh.kr(-12), \compAtk.kr(0.01), \compRls.kr(0.1), \ratio.kr(2), \knee.kr(0.01),\dbMakeUp.kr(0));
                SendPeakRMS.ar(sig.sum * -3.dbamp,10,3,'/inSynth',0);

                Out.ar(\outBus.kr, sig )
            }).add;
        }
    }

    init {
        this.initModuleArrays(7);

        // consider using busses here to allow for setting params before synths are created...
        // could also help if switching from mono to stereo synth after creation

        controls.add(
            NS_Fader("inAmp",\db,{ |f|  this.setSynths(\inAmp, f.value.dbamp) },'horz',-inf).round_(0.1).stringColor_(Color.white).maxWidth_(200)
        );
        assignButtons[0] = NS_AssignButton().maxWidth_(42);

        // compressor section
        controls.add(
            NS_Fader("thresh",\db,{ |f| this.setSynths(\dbThresh, f.value) },'horz',initVal: -12).round_(0.1).stringColor_(Color.white).maxWidth_(200)
        );
        assignButtons[1] = NS_AssignButton().maxWidth_(42);

        controls.add(
            NS_Knob("atk",ControlSpec(0.001,0.25),{ |k| this.setSynths(\compAtk, k.value) },false,0.01).round_(0.01).maxWidth_(45).stringColor_(Color.white)
        );
        assignButtons[2] = NS_AssignButton().maxWidth_(45);

        controls.add(
            NS_Knob("rls",ControlSpec(0.001,0.25),{ |k| this.setSynths(\compRls, k.value) },false,0.1).round_(0.01).maxWidth_(45).stringColor_(Color.white)
        );
        assignButtons[3] = NS_AssignButton().maxWidth_(45);

        controls.add(
            NS_Knob("ratio",ControlSpec(1,8,\lin),{ |k| this.setSynths(\ratio, k.value) },false,2).round_(0.1).maxWidth_(45).stringColor_(Color.white)
        );
        assignButtons[4] = NS_AssignButton().maxWidth_(45);

        controls.add(
            NS_Knob("knee",ControlSpec(0,1,\lin),{ |k| this.setSynths(\knee, k.value) },false,0.01).round_(0.01).maxWidth_(45).stringColor_(Color.white)
        );
        assignButtons[5] = NS_AssignButton().maxWidth_(45);

        controls.add(
            NS_Fader("trim",\boostcut,{ |k| this.setSynths(\dbMakeUp, k.value) },'horz').round_(0.1).stringColor_(Color.white).maxWidth_(220)
        );
        assignButtons[6] = NS_AssignButton().maxWidth_(42);

        rms = 2.collect({
            LevelIndicator().minWidth_(15).maxWidth_(20).style_(\led).numTicks_(11).numMajorTicks_(3)
            .stepWidth_(2).drawsPeak_(true).warning_(0.9).critical_(1.0)
        });

        view = View()
        .layout_(
            HLayout(
                VLayout( 
                    HLayout( controls[0], assignButtons[0] ),
                    HLayout( controls[1], assignButtons[1] ),
                    HLayout( controls[2], controls[3], controls[4], controls[5] ),
                    HLayout( assignButtons[2], assignButtons[3], assignButtons[4], assignButtons[5] ),
                    HLayout( controls[6], assignButtons[6] ),
                ),
                rms[0],
            )
        );

        view.fixedWidth_(200).fixedHeight_(190);
        view.layout.spacing_(2).margins_(0);
    }

    addMonoInput { |inBus, nsServer|

        synths.add( Synth(\ns_inputMono,[\inBus,inBus,\outBus,nsServer.inputBusses[inBus]],nsServer.inGroup) );
        localResponder.free;
        localResponder = OSCFunc({ |msg|

            if( msg[2] == 0,{
                { 
                    rms[0].value = msg[4].ampdb.linlin(-80, 0, 0, 1);
                    rms[0].peakLevel = msg[3].ampdb.linlin(-80, 0, 0, 1,\min)
                }.defer
            })
        }, '/inSynth', argTemplate: [synths[0].nodeID]);
    }   

    addStereoInput { |inBus, nsServer| 
        synths.add( Synth(\ns_inputStereo,[\inBus,inBus,\outBus,nsServer.inputBusses[inBus]],nsServer.inGroup) );
        localResponder.free;
        localResponder = OSCFunc({ |msg|

            if( msg[2] == 0,{
                { 
                    rms[0].value = msg[4].ampdb.linlin(-80, 0, 0, 1);
                    rms[0].peakLevel = msg[3].ampdb.linlin(-80, 0, 0, 1,\min)
                }.defer
            })
        }, '/inSynth', argTemplate: [synths[0].nodeID]);
    } 

    setSynths { |key, value|
        synths.do({ |syn| syn.set(key, value) })
    }

    free {
        localResponder.free;
        oscFuncs.do({ |func| func.free });
        synths.do({ |synth| synth.set(\gate,0) }); 
    }

    asView { ^view }

}
