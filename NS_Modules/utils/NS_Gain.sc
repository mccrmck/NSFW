NS_Gain : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_gain,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                sig = sig * \gain.kr(1);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(2);
        this.makeWindow("Gain", Rect(0,0,230,60));

        synths.add( Synth(\ns_gain,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\trim,\boostcut.asSpec,0)
        .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_( c.value ); synths[0].set(\thru, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]).round_(0.1)    , assignButtons[0] ),
                HLayout( NS_ControlButton(controls[1],["▶","bypass"]), assignButtons[1] )
            )
        );

        win.layout.spacing_(2).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(),
            OSC_Button(height: "20%"),
        ],randCol: true).oscString("Gain")
    }
}
