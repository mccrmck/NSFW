NS_FreeVerb : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_freeVerb,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                sig = HPF.ar(sig,80) + PinkNoise.ar(0.0001);
                sig = BLowShelf.ar(sig,\preLoFreq.kr(200),1,\preLodB.kr(0));
                sig = BHiShelf.ar(sig,\preHiFreq.kr(8000),1,\preHidB.kr(0));
                sig = FreeVerb.ar(sig,1,\room.kr(1),\damp.kr(0.9));
                sig = BLowShelf.ar(sig,\postLoFreq.kr(200),1,\postLodB.kr(0));
                sig = BHiShelf.ar(sig,\postHiFreq.kr(8000),1,\postHidB.kr(0));
                
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(10);
        this.makeWindow("FreeVerb", Rect(0,0,270,270));

        synths.add( Synth(\ns_freeVerb,[\bus,bus],modGroup) );

        // this could use a better interface, I think

        controls[0] = NS_Control(\preLoHz,ControlSpec(20,2500,\exp),200)
        .addAction(\synth,{ |c| synths[0].set(\preLoFreq, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\preLodB,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\preLodB, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \knob);

        controls[2] = NS_Control(\preHiHz,ControlSpec(2500,10000,\exp),8000)
        .addAction(\synth,{ |c| synths[0].set(\preHiFreq, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\preHidB,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\preHidB, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \knob);

        controls[4] = NS_Control(\postLoHz,ControlSpec(20,2500,\exp),200)
        .addAction(\synth,{ |c| synths[0].set(\postLoFreq, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\postLodB,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\postLodB, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \knob);

        controls[6] = NS_Control(\postHiHz,ControlSpec(2500,10000,\exp),8000)
        .addAction(\synth,{ |c| synths[0].set(\postHiFreq, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(30);

        controls[7] = NS_Control(\postHidB,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\postHidB, c.value) });
        assignButtons[7] = NS_AssignButton(this, 7, \knob);

        controls[8] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[8] = NS_AssignButton(this, 8, \fader).maxWidth_(30);

        controls[9] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[9] = NS_AssignButton(this, 9, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                VLayout( *4.collect({ |i| HLayout( NS_ControlFader(controls[i * 2]).round_(1), assignButtons[i * 2]) }) ),
                HLayout( *4.collect({ |i| VLayout( NS_ControlKnob(controls[i * 2 + 1])       , assignButtons[i * 2 +1]) }) ),
                HLayout( NS_ControlFader(controls[8])                , assignButtons[8] ),
                HLayout( NS_ControlButton(controls[9],["▶","bypass"]), assignButtons[9] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Panel({OSC_Knob()} ! 4, columns: 4),
            OSC_Panel({OSC_Knob()} ! 4, columns: 4),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2, height: "20%"),
        ],randCol:true).oscString("FreeVerb")
    }
}
