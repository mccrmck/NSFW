NS_Freeze : NS_SynthModule {
    classvar <isSource = false;
    var trigGroup, synthGroup;
    var bufferArray, bufIndex, localResponder;
    var sendBus, mixBus;

    *initClass {
        StartUp.add{
            SynthDef(\ns_freezeTrig,{
                var sig = In.ar(\bus.kr,2);
                var trig = FluidOnsetSlice.ar(sig.sum * -3.dbamp,9,\thresh.kr(1));
                trig = Select.ar(\which.kr(0),[trig, Impulse.ar(\trigFreq.kr(0)), Dust.ar(\trigFreq.kr(0))]);
                trig = trig * \trigMute.kr(0);
                trig = trig + \trig.tr(0);

                SendTrig.ar(trig,0,1);
                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                Out.ar(\sendBus.kr,sig);
                ReplaceOut.ar(\bus.kr,sig * (1 - \mix.kr(0.5)) )
            }).add;

            SynthDef(\ns_freeze,{
                var sig = In.ar(\inBus.kr, 2).sum;

                sig = FFT(\bufnum.kr,sig);
                sig = PV_Freeze(sig,1);
                sig = IFFT(sig);

                sig = sig * Env.asr(0.5,1,0.02).ar(2,\gate.kr(1) + Impulse.kr(0));
                sig = sig * Env.asr(0,1,0).kr(1,\pauseGate.kr(1));

                sig = Pan2.ar(sig,Rand(-0.8,0.8),\amp.kr(1));

                Out.ar(\outBus.kr,sig * \mix.kr(0.5))
            }).add
        }
    }

    init {
        this.initModuleArrays(8);

        this.makeWindow("Freeze",Rect(0,0,240,360));

        trigGroup  = Group(modGroup);
        synthGroup = Group(trigGroup,\addAfter);

        synths = List.newClear(2);

        sendBus = Bus.audio(modGroup.server,2);
        mixBus = Bus.control(modGroup.server,1).set(0.5);

        bufIndex = 0;
        bufferArray = [128,1024,4096].collect({ |frames| Buffer.alloc(modGroup.server, frames) });

        synths.put(0,Synth(\ns_freezeTrig,[\bus,bus,\sendBus,sendBus, \mix,mixBus.asMap],trigGroup));

        localResponder.free;
        localResponder = OSCFunc({ |msg|
            if(synths[1].notNil,{ synths[1].set(\gate,0) });
            synths.put(1, Synth(\ns_freeze,[\inBus,sendBus,\bufnum, bufferArray[bufIndex], \mix,mixBus.asMap, \outBus,bus],synthGroup) );

        },'/tr',argTemplate: [synths[0].nodeID]);

        controls.add(
            NS_Switch(["onsets","impulse","dust"],{ |switch| synths[0].set(\which,switch.value) })
        );
        assignButtons[0] = NS_AssignButton().maxWidth_(60).setAction(this,0,\switch);

        controls.add(
            NS_Switch(["128","1024","4096"],{ |switch| bufIndex = switch.value })
        );
        assignButtons[1] = NS_AssignButton().maxWidth_(60).setAction(this,1,\switch);

        controls.add(
            NS_Fader("trigFreq",ControlSpec(0,8,\lin),{ |f| synths[0].set(\trigFreq, f.value) },initVal:0).maxWidth_(60);
        );
        assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this,2,\fader);

        controls.add(
            NS_Fader("thresh",\db,{ |f| synths[0].set(\thresh, f.value.dbamp ) },initVal:0).maxWidth_(60);
        );
        assignButtons[3] = NS_AssignButton().maxWidth_(60).setAction(this,3,\fader);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| mixBus.set(f.value) },initVal:0.5).maxWidth_(60)
        );
        assignButtons[4] = NS_AssignButton().maxWidth_(60).setAction(this,4,\fader);

        controls.add(
            Button()
            .minHeight_(45)
            .maxWidth_(60)
            .states_([["▶"],["mute\ntrig",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\trigMute,val)
            })
        );
        assignButtons[5] = NS_AssignButton().maxWidth_(60).setAction(this,5,\button);

        controls.add(
            Button()
            .minHeight_(45)
            .maxWidth_(60)
            .states_([["free",Color.black]])
            .action_({ |but|
                synths[1].set(\gate,0);
                synths[1] = nil
            })
        );
        assignButtons[6] = NS_AssignButton().maxWidth_(60).setAction(this,6,\button);

        controls.add(
            Button()
            .minHeight_(45)
            .maxWidth_(60)
            .states_([["trig"]])
            .action_({ |but|
                synths[0].set(\trig,1)
            })
        );
        assignButtons[7] = NS_AssignButton().maxWidth_(60).setAction(this,7,\button);


        win.view.layout_(
            HLayout(
                VLayout(
                    controls[0],assignButtons[0],
                    controls[1],assignButtons[1],
                ),
                GridLayout.rows(
                    [ controls[2],      controls[3],      controls[4] ],
                    [ assignButtons[2], assignButtons[3], assignButtons[4] ],
                    [ controls[5],      controls[6],      controls[7] ],
                    [ assignButtons[5], assignButtons[6], assignButtons[7] ]
                )
            )
        );

        controls[0].layout.spacing_(4);
        controls[0].buttonsMinHeight_(45);
        controls[1].layout.spacing_(4);
        controls[1].buttonsMinHeight_(45);

        win.layout.spacing_(4).margins_(4);
        win.view.maxWidth_(255).maxHeight_(350);
    }

    freeExtra {
        trigGroup.free;
        synthGroup.free;
        bufferArray.do({ |b| b.free });
        sendBus.free;
        mixBus.free;
        localResponder.free
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Panel(widgetArray:[
                OSC_Switch(numPads: 3),
                OSC_Switch(numPads: 3),
            ]),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Panel(widgetArray:[
                OSC_Button(),
                OSC_Button(mode: 'push'),
                OSC_Button(mode: 'push')
            ])
        ],randCol:true).oscString("Freeze")
    }

}
