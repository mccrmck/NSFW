NS_ShiftRegister : NS_SynthModule {
    classvar <isSource = true;

    // pretty sure I got this synthDef from Alejandro Olarte, but I can't remember when
    *initClass {
        ServerBoot.add{
            SynthDef(\ns_shiftRegister,{
                var numChans = NSFW.numChans;
                var freq = \freq.kr(4);
                var sr = SampleRate.ir * \sRate.kr(1);
                var bits = \bits.ar(32);  
                var bitsRaised = 2 ** bits;
                var t = Phasor.ar(DC.ar(0), freq * (bitsRaised / sr), 0, bitsRaised - 1 );
                var array = [
                    t * (( (t>>64) | (t>>8) ) & (63 & (t>>4)) ),
                    t * (( (t>>9)  | (t>>13)) & (25 & (t>>6)) ),
                    t * (( (t>>5)  | (t>>8) ) & 63),
                    t * (((t>>11)  & (t>>8) ) & (123 & (t>>3)) ),
                    t * (t>>8 * ((t>>15) | (t>>8)) & (20 | (t>>19) * 5>>t | (t>>3))),
                    t * (t>>( (t>>9) | (t>>8) ) & (63 & (t>>4)) ),
                    (t>>7 | t | t>>6) * 10 + 4 * (t & t>>13 | t>>6 )
                ];

                var sig = SelectX.ar(\which.kr(0).clip(0,array.size - 1).lag(0.1),array);

                sig = sig % bitsRaised;
                sig = sig * (0.5 ** (bits-1) ) - 1;
                sig = LeakDC.ar(sig) * -18.dbamp;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig,numChans,\bus.kr,\mix.kr(1),\thru.kr(0))
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("ShiftRegister", Rect(0,0,240,150));

        synths.add( Synth(\ns_shiftRegister,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\sRate, ControlSpec(0.01,1,\exp), 1)
        .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);
        
        controls[1] = NS_Control(\bits, ControlSpec(8,32,\exp), 32)
        .addAction(\synth,{ |c| synths[0].set(\bits, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\freq, ControlSpec(0.01,250,\exp), 4)
        .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\which, ControlSpec(0,6,\lin,1), 0)
        .addAction(\synth,{ |c| synths[0].set(\which, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \switch).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1])                , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                , assignButtons[2] ),
                HLayout( NS_ControlSwitch(controls[3],(0..6),7)      , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4])                , assignButtons[4] ),
                HLayout( NS_ControlButton(controls[5],["▶","bypass"]), assignButtons[5] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(width: "55%"),
            OSC_Fader(horizontal: false),
            OSC_Switch(7),
            OSC_Panel([OSC_Fader(false,false), OSC_Button(height:"20%")])
        ], columns: 4, randCol:true).oscString("ShiftRegister")
    }
}
