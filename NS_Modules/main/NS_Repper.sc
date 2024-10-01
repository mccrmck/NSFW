NS_Repper : NS_SynthModule {
    classvar <isSource = false;
    var sendBus;
    var dTimeBus, atkBus, rlsBus, curveBus, envBus, mixBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_repperTap,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr,numChans);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                Out.ar(\sendBus.kr,sig.sum * numChans.reciprocal );
                ReplaceOut.ar(\bus.kr,sig * (1 - \mix.kr(0.5) ));
            }).add;

            SynthDef(\ns_repper,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr,1) * 3.dbamp; // sum bus, only needs one channel
                var dTime = \dTime.kr(0.2) * Rand(0.75,1);
                var atk = \atk.kr(0.01);
                var rls = \rls.kr(2);
                var dur = (atk + rls) * Rand(0.75,1);
                var lineDown = XLine.kr(dTime, dTime * 2, dur );
                var lineUp = XLine.kr(dTime, dTime / 2,dur);
                var direction = Select.kr(\which.kr(0),[dTime, lineDown, lineUp]);
                sig = sig * Env([0,1,1,0],[0.01,0.98,0.01]).ar(gate:1,timeScale: dTime );

                sig = CombC.ar(sig,1,direction,inf);
                sig = LeakDC.ar(sig);
                sig = sig.tanh;
                sig = NS_Pan(sig, numChans, \pan.kr(0), 2);
                sig = sig * Env.perc(atk,rls,1,\curve.kr(-2)).ar(2);

                Out.ar(\outBus.kr,sig * \mix.kr(0.5) )
            }).add
        }
    }

    init {
        this.initModuleArrays(7);
        strip.inSynthGate_(1);
        this.makeWindow("Repper",Rect(0,0,330,120));

        sendBus = Bus.audio(modGroup.server,1); // sumBus

        dTimeBus = Bus.control(modGroup.server,1).set(0.1);
        atkBus = Bus.control(modGroup.server,1).set(0.01);
        rlsBus = Bus.control(modGroup.server,1).set(2);
        curveBus = Bus.control(modGroup.server,1).set(0);
        envBus = Bus.control(modGroup.server,1).set(0);
        mixBus = Bus.control(modGroup.server,1).set(0.5);

        synths.add( Synth(\ns_repperTap,[\bus,bus,\sendBus,sendBus],modGroup,\addToHead).map(\mix, mixBus) );

        controls.add(
            NS_Fader("dTime",ControlSpec(0.02,1,\exp),{ |sl|
                var val = sl.value;
                dTimeBus.value_( val );
            },'horz',0.1)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["flat",Color.black,Color.white],["flat",Color.white,Color.black]])
            .action_({ |but|
                if(but.value == 1,{
                    Synth(\ns_repper,[
                        \inBus,sendBus,
                        \outBus,bus,
                        \dTime, dTimeBus.getSynchronous,
                        \which, 0,
                        \atk,atkBus.getSynchronous,
                        \rls,rlsBus.getSynchronous,
                        \curve,curveBus.getSynchronous,
                        \pan,0.8.rand2,
                    ],modGroup,\addToTail).map(\mix,mixBus)
                })
            })
        );
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(45);

        controls.add(
            Button()
            .states_([["down",Color.black,Color.white],["down",Color.white,Color.black]])
            .action_({ |but|
                if(but.value == 1,{
                    Synth(\ns_repper,[
                        \inBus,sendBus,
                        \outBus,bus,
                        \dTime, dTimeBus.getSynchronous,
                        \which, 1,
                        \atk,atkBus.getSynchronous,
                        \rls,rlsBus.getSynchronous,
                        \curve,curveBus.getSynchronous,
                        \pan,0.8.rand2,
                    ],modGroup,\addToTail).map(\mix,mixBus)
                })
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        controls.add(
            Button()
            .states_([["up",Color.black,Color.white],["up",Color.white,Color.black]])
            .action_({ |but|
                if(but.value == 1,{
                    Synth(\ns_repper,[
                        \inBus,sendBus,
                        \outBus,bus,
                        \dTime, dTimeBus.getSynchronous,
                        \which, 2,
                        \atk,atkBus.getSynchronous,
                        \rls,rlsBus.getSynchronous,
                        \curve,curveBus.getSynchronous,
                        \pan,0.8.rand2,
                    ],modGroup,\addToTail).map(\mix,mixBus)
                })
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        controls.add(
            NS_Fader("envDur",ControlSpec(2,8,\exp),{ |f|
                var envDur = f.value;
                envBus.set(envDur);
                if(controls[5].value == 0,{
                    atkBus.value_(0.01);
                    rlsBus.value_(envDur);
                    curveBus.value_(4.neg)
                },{
                    atkBus.value_(envDur);
                    rlsBus.value_(0.01);
                    curveBus.value_(4)
                })
            },'horz',2)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["decay",Color.black,Color.white],["swell",Color.white,Color.black]])
            .action_({ |but|
                var envDur = envBus.getSynchronous;
                if(but.value == 0,{
                    atkBus.value_(0.01);
                    rlsBus.value_(envDur);
                    curveBus.value_(4.neg)
                },{
                    atkBus.value_(envDur);
                    rlsBus.value_(0.01);
                    curveBus.value_(4)
                })
            })
        );
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| mixBus.value_(f.value) },'horz',initVal:0.5)
        );
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1], controls[2], assignButtons[2], controls[3], assignButtons[3] ),
                HLayout( controls[4], assignButtons[4] ),
                HLayout( controls[5], assignButtons[5], controls[6], assignButtons[6], ),
            ),
        );
        
        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        strip.inSynthGate_(0);
        sendBus.free;
        dTimeBus.free;
        atkBus.free;
        rlsBus.free;
        curveBus.free;
        envBus.free;
        mixBus.free;
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(horizontal: true, snap:true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true, snap:true),
                OSC_Button(width: "33%")
            ]),
            OSC_Fader(horizontal: true),
            OSC_Panel(height: "30%",widgetArray:[
                OSC_Button(mode: 'push'),
                OSC_Button(mode: 'push'),
                OSC_Button(mode: 'push'),
            ])
        ],randCol: true).oscString("Repper")
    }
}
