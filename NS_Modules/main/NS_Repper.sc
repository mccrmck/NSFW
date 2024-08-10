NS_Repper : NS_SynthModule {
    classvar <isSource = false;
    var sendBus;
    var dTimeBus, directionBus, atkBus, rlsBus, curveBus, envBus, mixBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_repperTap,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr,numChans);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                Out.ar(\sendBus.kr,sig.sum * -12.dbamp );  // needs numChan-dependent amplitude compensation
                ReplaceOut.ar(\bus.kr,sig * (1 - \mix.kr(0.5)) )
            }).add;

            SynthDef(\ns_repper,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\inBus.kr,1); // sum bus, only needs one channel
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

                Out.ar(\outBus.kr,sig * \mix.kr(0.5))
            }).add
        }
    }

    init {
        this.initModuleArrays(6);

        this.makeWindow("Repper",Rect(0,0,330,180));

        sendBus = Bus.audio(modGroup.server,1); // sumBus

        dTimeBus = Bus.control(modGroup.server,1).set(0.1);
        directionBus = Bus.control(modGroup.server,1).set(0);
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
        assignButtons[0] = NS_AssignButton().maxWidth_(45).setAction(this, 0, \fader);

        controls.add(
            NS_Switch(["flat","down","up"],{ |switch| directionBus.value_(switch.value) },'horz')
        );
        assignButtons[1] = NS_AssignButton().maxWidth_(45).setAction(this, 1, \switch);

        controls.add(
            NS_Fader("envDur",ControlSpec(2,8,\exp),{ |f|
                var val = f.value;
                if(envBus.getSynchronous == 0,{
                    atkBus.value_(0.01);
                    rlsBus.value_(val);
                    curveBus.value_(val.neg)
                },{
                    atkBus.value_(val);
                    rlsBus.value_(0.01);
                    curveBus.value_(val)
                })
            },'horz',2)
        );
        assignButtons[2] = NS_AssignButton().maxWidth_(45).setAction(this, 2, \fader);

        controls.add(
            Button()
            .states_([["decay",Color.black,Color.white],["swell",Color.white,Color.black]])
            .action_({ |but|
                var decSwell = but.value.asInteger;
                var val = controls[2].value;
                envBus.value_(decSwell);
                if(decSwell == 0,{
                    atkBus.value_(0.01);
                    rlsBus.value_(val);
                    curveBus.value_(val.neg)
                },{
                    atkBus.value_(val);
                    rlsBus.value_(0.01);
                    curveBus.value_(val)
                })
            })
        );
        assignButtons[3] = NS_AssignButton().maxWidth_(45).setAction(this, 3, \button);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| mixBus.value_(f.value) },'horz',initVal:0.5)
        );
        assignButtons[4] = NS_AssignButton().maxWidth_(45).setAction(this, 4, \fader);

        controls.add(
            Button()
            .states_([["trig",Color.black,Color.white],["trig",Color.white,Color.black]])
            .action_({ |but|
                if(but.value == 0,{
                    Synth(\ns_repper,[
                        \inBus,sendBus,
                        \outBus,bus,
                        \dTime, dTimeBus.getSynchronous,
                        \which,directionBus.getSynchronous,
                        \atk,atkBus.getSynchronous,
                        \rls,rlsBus.getSynchronous,
                        \curve,curveBus.getSynchronous,
                        \pan,0.8.rand2,
                    ],modGroup,\addToTail).map(\mix,mixBus)
                })
            })
        );
        assignButtons[5] = NS_AssignButton().maxWidth_(45).setAction(this, 5, \button);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3], controls[5], assignButtons[5], ),
                HLayout( controls[4], assignButtons[4] )
            ),
        )
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(horizontal: true, snap:true),
            OSC_Switch(mode:'slide',numPads:3),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true, snap:true),
                OSC_Button(width: "33%")
            ]),
            OSC_Fader(horizontal: true),
            OSC_Button(height: "30%",mode:'push')
        ],randCol: true).oscString("Repper")
    }

    freeExtra {
        sendBus.free;
        dTimeBus.free;
        directionBus.free;
        atkBus.free;
        rlsBus.free;
        curveBus.free;
        envBus.free;
        mixBus.free;
    }
}
