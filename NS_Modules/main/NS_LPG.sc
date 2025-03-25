NS_LPG : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_lpg,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var amp = Amplitude.ar(sig.sum * -3.dbamp * \gainOffset.kr(1),\atk.kr(0.1),\rls.kr(0.1));
                var rq = \rq.kr(0.707);

                sig = Select.ar(\which.kr(0),[
                    BLowPass.ar(sig,amp.linexp(0,1,20,20000),rq),
                    BHiPass.ar(sig,amp.linexp(0,1,20,20000),rq),
                    BLowPass.ar(sig,amp.linexp(0,1,20000,20),rq),
                    BHiPass.ar(sig,amp.linexp(0,1,20000,20),rq),
                ]);

                sig = LeakDC.ar(sig.tanh);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(7);
        this.makeWindow("LPG", Rect(0,0,240,180));

        synths.add( Synth(\ns_lpg,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\trim,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\gainOffset, c.value.dbamp) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\atk,ControlSpec(0.001,0.1,\exp),0.1)
        .addAction(\synth,{ |c| synths[0].set(\atk, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\rls,ControlSpec(0.001,0.1,\exp),0.1)
        .addAction(\synth,{ |c| synths[0].set(\rls, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\filt,ControlSpec(0,3,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\which, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \switch).maxWidth_(30);

        controls[4] = NS_Control(\rq,ControlSpec(0.01,1,\exp), 2.sqrt.reciprocal)
        .addAction(\synth,{ |c| synths[0].set(\rq, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[5] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[6] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                 , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]).round_(0.001)   , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]).round_(0.001)   , assignButtons[2] ),
                HLayout( NS_ControlSwitch(controls[3], ["LPG","HPG","ILPG","IHPG"],4), assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4]).round_(0.001)   , assignButtons[4] ),
                HLayout( NS_ControlFader(controls[5])                 , assignButtons[5] ),
                HLayout( NS_ControlButton(controls[6], ["▶","bypass"]), assignButtons[6] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {
        ^OSC_Panel([
            OSC_Panel([OSC_XY(width: "75%"), OSC_Switch(4)], columns: 2, height: "50%"),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width:"20%")], columns: 2)
        ],randCol:true).oscString("LPG")
    }
}

