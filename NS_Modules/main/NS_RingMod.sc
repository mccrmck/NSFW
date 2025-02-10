NS_RingMod : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_ringMod,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var freq = \freq.kr(40).lag(0.05);
                sig = sig * SinOsc.ar(freq + SinOsc.ar(\modFreq.kr(40),mul: \modMul.kr(1)) );

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("RingMod", Rect(0,0,285,120));

        synths.add( Synth(\ns_ringMod,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\freq,ControlSpec(1,3500,\exp),40)
        .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\mFreq,ControlSpec(1,3500,\exp),4)
        .addAction(\synth,{ |c| synths[0].set(\modFreq, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\mMul,ControlSpec(1,3500,\amp))
        .addAction(\synth,{ |c| synths[0].set(\modMul, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                 , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1])                 , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                 , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3])                 , assignButtons[3] ),
                HLayout( NS_ControlButton(controls[4], ["▶","bypass"]), assignButtons[4] ),
            )
        );

        win.layout.spacing_(2).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader("15%",snap:true),
            OSC_Panel("15%",horizontal:false,widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
            ])
        ],randCol:true).oscString("RingMod")
    }
}
