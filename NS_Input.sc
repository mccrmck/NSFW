NS_Input : NS_SynthModule {
    classvar <isSource = true;
    var <rms, localResponder;

    *initClass {
        StartUp.add{
            SynthDef(\ns_monoInput,{
                var sig = SoundIn.ar(\inBus.kr);

                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\inAmp.kr(0));

                SendPeakRMS.ar(sig,10,3,'/inSynth',0);
                sig = Squish.ar(sig,sig,\dbThresh.kr(-12), \compAtk.kr(0.01), \compRls.kr(0.1), \ratio.kr(2), \knee.kr(0.01),\dbMakeUp.kr(0));
                SendPeakRMS.ar(sig,10,3,'/inSynth',1);

                Out.ar(\outBus.kr, sig!2 )
            }).add;

            SynthDef(\ns_stereoInput,{
                var inBus = \inBus.kr;
                var sig = SoundIn.ar([inBus,inBus + 1]);

                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(0));

                SendPeakRMS.ar(sig.sum * -3.dbamp,10,3,'/inSynth',0);
                sig = Squish.ar(sig,sig,\dbThresh.kr(-12), \compAtk.kr(0.01), \compRls.kr(0.1), \ratio.kr(2), \knee.kr(0.01),\dbMakeUp.kr(0));
                SendPeakRMS.ar(sig.sum * -3.dbamp,10,3,'/inSynth',1);

                Out.ar(\outBus.kr, sig )
            }).add;

            SynthDef(\ns_inSend,{
                var sig = In.ar(\inBus.kr,2);
                sig = sig * \amp.kr(1);
                Out.ar(\outBus.kr,sig)
            }).add
        }
    }

    *new { |group, bus, inChans = 0|
        ^super.new(group, bus, inChans.asArray)
    }

    init { |inChans|
        this.initModuleArrays(7);

        this.makeWindow("Input: %".format(inChans),Rect(50,250,375,240));

        switch(inChans.size,
            1, { synths.add( Synth(\ns_monoInput,[\inBus,inChans[0],\outBus,bus],modGroup) ) },
            2, { synths.add( Synth(\ns_stereoInput,[\inBus,inChans[0],\outBus,bus],modGroup) ) },
            { "only mono and stereo inputs implemented at the moment".error }
        );

        localResponder.free;
        localResponder = OSCFunc({ |msg|

            if( msg[2].asBoolean.not,{
                { 
                    rms[0].value = msg[4].ampdb.linlin(-80, 0, 0, 1);
                    rms[0].peakLevel = msg[3].ampdb.linlin(-80, 0, 0, 1,\min)
                }.defer

            },{
                { 
                    rms[1].value = msg[4].ampdb.linlin(-80, 0, 0, 1);
                    rms[1].peakLevel = msg[3].ampdb.linlin(-80, 0, 0, 1,\min)
                }.defer

            })
        }, '/inSynth', argTemplate: [synths[0].nodeID]);

        controls.add(
            NS_Fader(win,"inAmp",\amp,{ |f| synths[0].set(\inAmp, f.value) },initVal: 0).round_(0.1)
        );
        assignButtons[0] = NS_AssignButton();

        // compressor section
        controls.add(
            NS_Fader(win,"thresh",\db,{ |f| synths[0].set(\dbThresh, f.value) },initVal: -12).maxWidth_(60).round_(0.1)
        );
        assignButtons[1] = NS_AssignButton().maxWidth_(60);

        controls.add(
            NS_Knob(win,"atk",ControlSpec(0.001,0.25),{ |k| synths[0].set(\compAtk, k.value) },false,0.01).round_(0.01)
        );
        assignButtons[2] = NS_AssignButton();

        controls.add(
            NS_Knob(win,"rls",ControlSpec(0.001,0.25),{ |k| synths[0].set(\compRls, k.value) },false,0.1).round_(0.01)
        );
        assignButtons[3] = NS_AssignButton();

        controls.add(
            NS_Knob(win,"ratio",ControlSpec(1,8,\lin),{ |k| synths[0].set(\ratio, k.value) },false,2).round_(0.1)
        );
        assignButtons[4] = NS_AssignButton();

        controls.add(
            NS_Knob(win,"knee",ControlSpec(0,1,\lin),{ |k| synths[0].set(\knee, k.value) },false,0.01).round_(0.01)
        );
        assignButtons[5] = NS_AssignButton();

        controls.add(
            NS_Fader(win,"muGain",\boostcut,{ |k| synths[0].set(\dbMakeUp, k.value) }).round_(0.1)
        );
        assignButtons[6] = NS_AssignButton();

        rms = 2.collect({
            LevelIndicator().minWidth_(15).style_(\led).numTicks_(11).numMajorTicks_(3)
            .stepWidth_(2).drawsPeak_(true).warning_(0.9).critical_(1.0)
        });

        win.layout_(
            HLayout(
                VLayout(
                    HLayout(
                        controls[0],
                        rms[0],
                    ),
                    assignButtons[0]
                ),
                VLayout(
                    controls[1], assignButtons[1]
                ),
                View().layout_(
                    GridLayout.rows(
                        [ VLayout(controls[2], assignButtons[2]), VLayout(controls[3], assignButtons[3]) ],
                        [ VLayout(controls[4], assignButtons[4]), VLayout(controls[5], assignButtons[5]) ],
                        [ 
                            StaticText().string_("input bus:"), 
                            DragSource().background_(Color.white).align_(\center)
                            .object_([inChans, NS_Input, this])
                            .string_(inChans.asString)
                            .dragLabel_(inChans.asString)
                        ]
                    ).spacing_(4).margins_(0)
                ),
                VLayout(
                    HLayout(
                        controls[6],
                        rms[1],
                    ),
                    assignButtons[6]
                )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    addSend { |group, outBus|
        var send = Synth(\ns_inSend,[\inBus,bus,\outBus, outBus],group);
        synths.add(send);
        ^send
    }

    makeOSCFragment {}
}
