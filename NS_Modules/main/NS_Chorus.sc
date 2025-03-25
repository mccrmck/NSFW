NS_Chorus : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_chorus,{
                var numChans = NSFW.numChans;
                var in = In.ar(\bus.kr, numChans);

                var sig = in + LocalIn.ar(numChans);
                var depth = \depth.kr(0.5).lag(0.5) * 0.01;
                8.do{ sig = DelayC.ar(sig,0.03, LFDNoise3.kr(\rate.kr(0.2).lag(0.5)).range(0.018 - depth,0.018 + depth) ) };

                LocalOut.ar(sig.rotate * \feedB.kr(0.5).lag(0.1));
                //sig =  sig + in;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("Chorus", Rect(0,0,240,120));

        synths.add( Synth(\ns_chorus,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\rate,ControlSpec(0.01,20,\exp),0.5)
        .addAction(\synth,{ |c| synths[0].set(\rate,c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\depth,ControlSpec(0,1,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\depth,c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\feedB,ControlSpec(0,0.9,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\feedB,c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]), assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]), assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]), assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]), assignButtons[3] ),
                HLayout( NS_ControlButton(controls[4],["▶","bypass"]), assignButtons[4] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(width: "70%"),
            OSC_Fader(true, false),
            OSC_Panel([OSC_Fader(false, false), OSC_Button(height:"20%")])   
        ],columns: 3, randCol: true).oscString("Chorus")
    }
}
