NS_SwellFB : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_swellFB,{
                var numChans = NSFW.numChans;
                var in = In.ar(\bus.kr, numChans);
                var coef = \coef.kr(1);
                var thresh = \thresh.kr(0.5);
                var sig = in.sum * numChans.reciprocal.sqrt * Env.sine(\dur.kr(0.1)).ar(gate: \trig.tr);
                var pan = Demand.kr(\trig.tr(),0,Dwhite(-1.0,1.0));
                sig = sig + LocalIn.ar(1);
                sig = DelayC.ar(sig,0.1, \delay.kr(0.03));
                sig = sig * (1 -Trig.ar(Amplitude.ar(sig) > thresh,0.1)).lag(0.01);
                LocalOut.ar(sig * coef);
                sig = LeakDC.ar(HPF.ar(sig,80));
                //sig = ReplaceBadValues.ar(sig,0,);
                                
                sig = NS_Pan(sig, numChans,pan,numChans/4);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
                sig = (in * \muteThru.kr(1)) + sig;
                ReplaceOut.ar(\bus.kr, sig);
            }).add
        }
    }

    init {
        this.initModuleArrays(8);
        this.makeWindow("SwellFB", Rect(0,0,240,210));

        synths.add( Synth(\ns_swellFB,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\delay, ControlSpec(1000.reciprocal,0.1,\exp), 0.03)
        .addAction(\synth, { |c| synths[0].set(\delay, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);
        
        controls[1] = NS_Control(\dur, ControlSpec(0.01,0.1,\exp), 0.1)
        .addAction(\synth, { |c| synths[0].set(\dur, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\coef, ControlSpec(0.95,1.5,\lin), 1)
        .addAction(\synth, { |c| synths[0].set(\coef, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\thresh, ControlSpec(-24,-3,\db), -6)
        .addAction(\synth, { |c| synths[0].set(\thresh, c.value.dbamp) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\trig, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth, { |c| synths[0].set(\trig, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(30);
        
        controls[5] = NS_Control(\muteThru, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth, { |c| synths[0].set(\muteThru, 1 - c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        controls[6] = NS_Control(\amp,\db)
        .addAction(\synth,{ |c| synths[0].set(\amp, c.value.dbamp) });
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(30);

        controls[7] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[7] = NS_AssignButton(this, 7, \button).maxWidth_(30);
       
        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]).round_(0.001)                , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]).round_(0.001)                , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                              , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]).round_(1)                    , assignButtons[3] ),
                HLayout( NS_ControlButton(controls[4], ["trig","trig"])            , assignButtons[4] ),
                HLayout( NS_ControlButton(controls[5], ["mute thru","unmute thru"]), assignButtons[5] ),
                HLayout( NS_ControlFader(controls[6])                              , assignButtons[6] ),
                HLayout( NS_ControlButton(controls[7], ["▶","bypass"])             , assignButtons[7] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(height: "70%"),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width:"20%")], columns: 2)
        ],randCol:true).oscString("SwellFB")
    }
}
