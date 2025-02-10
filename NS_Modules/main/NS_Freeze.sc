NS_Freeze : NS_SynthModule {
    classvar <isSource = false;
    var trigGroup, synthGroup;
    var bufferArray, bufIndex, localResponder;
    var sendBus, mixBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_freezeTrig,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr,numChans);
                var sum = sig.sum * numChans.reciprocal.sqrt;
                var trig = FluidOnsetSlice.ar(sum,9,\thresh.kr(1));
                trig = Select.ar(\which.kr(0),[trig, Impulse.ar(\trigFreq.kr(0)), Dust.ar(\trigFreq.kr(0))]);
                trig = trig * \trigMute.kr(0);
                trig = trig + \trig.tr(0);

                SendTrig.ar(trig,0,1);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                Out.ar(\sendBus.kr,sum * 3.dbamp );

                ReplaceOut.ar(\bus.kr,sig * (1 - \mix.kr(0.5)) )
            }).add;

            SynthDef(\ns_freeze,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr, 1);

                sig = FFT(\bufnum.kr,sig);
                sig = PV_Freeze(sig,1);
                sig = IFFT(sig);

                sig = sig * Env.asr(0.5,1,0.02).ar(2,\gate.kr(1) + Impulse.kr(0));
                sig = sig * Env.asr(0,1,0).kr(1,\pauseGate.kr(1));

                sig = NS_Pan(sig,numChans,Rand(-0.8,0.8),numChans/4);

                Out.ar(\outBus.kr,sig * \amp.kr(1) * \mix.kr(0.5) )
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("Freeze",Rect(0,0,240,180));

        trigGroup  = Group(modGroup);
        synthGroup = Group(trigGroup,\addAfter);

        synths = List.newClear(2);

        sendBus = Bus.audio(modGroup.server,1);
        mixBus = Bus.control(modGroup.server,1).set(0.5);

        bufIndex = 0;
        bufferArray = [128,1024,2048].collect({ |frames| Buffer.alloc(modGroup.server, frames) });

        synths.put(0,Synth(\ns_freezeTrig,[\bus,bus,\sendBus,sendBus, \mix,mixBus.asMap],trigGroup));

        localResponder.free;
        localResponder = OSCFunc({ |msg|
            if(synths[1].notNil,{ synths[1].set(\gate,0) });
            synths.put(1, Synth(\ns_freeze,[\inBus,sendBus,\bufnum, bufferArray[bufIndex], \mix,mixBus.asMap, \outBus,bus],synthGroup) );

        },'/tr',argTemplate: [synths[0].nodeID]);

        controls[0] = NS_Control(\whichTrig, ControlSpec(0,2,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\which,c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \switch).maxWidth_(30);

        controls[1] = NS_Control(\fftSize, ControlSpec(0,2,\lin,1),0)
        .addAction(\synth,{ |c|  bufIndex = c.value });
        assignButtons[1] = NS_AssignButton(this,1, \switch).maxWidth_(30);

        controls[2] = NS_Control(\tFreq,ControlSpec(0,4,\lin),0)
        .addAction(\synth,{ |c| synths[0].set(\trigFreq, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\thresh,\db,0)
        .addAction(\synth,{ |c| synths[0].set(\thresh, c.value.dbamp) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),0.5)
        .addAction(\synth,{ |c| mixBus.set(c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass, ControlSpec(0,2,\lin,1), 0)
        .addAction(\synth,{ |c| 
            switch(c.value,
                0,{ synths[0].set(\trigMute,0); synths[1].set(\gate,0); synths[1] = nil },
                1,{ synths[0].set(\trigMute,1) },
                2,{ synths[0].set(\trigMute,0); synths[0].set(\trig,1) }
            );
            strip.inSynthGate_(c.value.sign);
        });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        win.view.layout_(
            VLayout(
                HLayout( NS_ControlSwitch(controls[0], ["onsets","impulse","dust"], 3), assignButtons[0] ),
                HLayout( NS_ControlSwitch(controls[1], ["128","1024","2048"], 3)      , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                                 , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]).round_(1)                                 , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4])                                 , assignButtons[4] ),
                HLayout( NS_ControlSwitch(controls[5], ["free","▶", "trig"] ,3)       , assignButtons[5] ),
            )
        );

        win.layout.spacing_(4).margins_(4);
    }

    freeExtra {
        trigGroup.free;
        synthGroup.free;
        bufferArray.do(_.free);
        sendBus.free;
        mixBus.free;
        localResponder.free
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Panel(widgetArray:[
                OSC_Switch(columns: 3, numPads: 3),
                OSC_Switch(columns: 3, numPads: 3),
            ]),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Switch(columns:3, numPads:3)
        ],randCol:true).oscString("Freeze")
    }
}
