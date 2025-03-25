NS_Repper : NS_SynthModule {
    classvar <isSource = false;
    var tapGroup, repGroup;
    var sendBus;
    var dTimeBus, atkBus, rlsBus, curveBus, envBus, mixBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_repperTap,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr,numChans);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                Out.ar(\sendBus.kr,sig.sum * numChans.reciprocal.sqrt );
                ReplaceOut.ar(\bus.kr,sig * (1 - \mix.kr(0.5) ));
            }).add;

            SynthDef(\ns_repper,{
                var numChans = NSFW.numChans;
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
                sig = NS_Pan(sig, numChans, \pan.kr(0), numChans/4);
                sig = sig * Env.perc(atk,rls,1,\curve.kr(-2)).ar(2);

                Out.ar(\outBus.kr,sig * \mix.kr(0.5) )
            }).add
        }
    }

    init {
        this.initModuleArrays(7);
        strip.inSynthGate_(1);
        this.makeWindow("Repper",Rect(0,0,270,120));

        tapGroup = Group(modGroup);
        repGroup = Group(tapGroup,\addAfter);

        sendBus = Bus.audio(modGroup.server,1); // sumBus

        dTimeBus = Bus.control(modGroup.server,1).set(0.1);
        atkBus = Bus.control(modGroup.server,1).set(0.01);
        rlsBus = Bus.control(modGroup.server,1).set(2);
        curveBus = Bus.control(modGroup.server,1).set(0);
        envBus = Bus.control(modGroup.server,1).set(0);
        mixBus = Bus.control(modGroup.server,1).set(0.5);

        synths.add( Synth(\ns_repperTap,[\bus,bus,\sendBus,sendBus,\mix, mixBus.asMap],tapGroup) );

        controls[0] = NS_Control(\dTime,ControlSpec(0.02,1,\exp),0.1)
        .addAction(\synth,{ |c| dTimeBus.set( c.value ) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\synth,ControlSpec(0,2,\lin,1),0)
        .addAction(\synth,{ |c|
            var flatDownUp = c.value;
            Synth(\ns_repper,[
                \inBus, sendBus,
                \outBus, bus,
                \dTime, dTimeBus.getSynchronous,
                \which, flatDownUp,
                \atk, atkBus.getSynchronous,
                \rls, rlsBus.getSynchronous,
                \curve, curveBus.getSynchronous,
                \pan, 1.0.rand2,
                \mix, mixBus.asMap
            ],repGroup)
        },false);
        assignButtons[1] = NS_AssignButton(this, 1, \switch).maxWidth_(30);

        controls[2] = NS_Control(\envDur,ControlSpec(2,8,\exp),2)
        .addAction(\synth,{ |c| 
            var envDur = c.value;
            envBus.set(envDur);
            if(controls[3].value == 0,{  // decay/swell
                atkBus.value_(0.01);
                rlsBus.value_(envDur);
                curveBus.value_(4.neg)
            },{
                atkBus.value_(envDur);
                rlsBus.value_(0.01);
                curveBus.value_(4)
            })
        });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\envDur, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c|  
            var envDur = envBus.getSynchronous;
            if(c.value == 0,{
                atkBus.value_(0.01);
                rlsBus.value_(envDur);
                curveBus.value_(4.neg)
            },{
                atkBus.value_(envDur);
                rlsBus.value_(0.01);
                curveBus.value_(4)
            })
        });
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),0.5)
        .addAction(\synth,{ |c| mixBus.set( c.value ) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                        , assignButtons[0] ),
                HLayout( NS_ControlSwitch(controls[1],["flat","down","up"],3), assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                        , assignButtons[2] ),
                HLayout( NS_ControlButton(controls[3], ["decay","swell"],2)  , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4])                        , assignButtons[4] ),
            ),
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        strip.inSynthGate_(0);
        tapGroup.free;
        repGroup.free;
        sendBus.free;
        dTimeBus.free;
        atkBus.free;
        rlsBus.free;
        curveBus.free;
        envBus.free;
        mixBus.free;
    }

    // this needs a rewrite
    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "33%")], columns: 2),
            OSC_Switch(3, 3, height: "30%"),
            OSC_Fader(),
        ],randCol: true).oscString("Repper")
    }
}
