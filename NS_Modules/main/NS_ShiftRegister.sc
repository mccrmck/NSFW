NS_ShiftRegister : NS_SynthModule {
    classvar <isSource = true;

    // pretty sure I got this synthDef from Alejandro Olarte, but I can't remember when
    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_shiftRegister" ++ numChans).asSymbol,
            {
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

                var sig = SelectX.ar(\which.kr(0).clip(0, array.size - 1).lag(0.1), array);

                sig = sig % bitsRaised;
                sig = sig * (0.5 ** (bits-1) ) - 1;
                sig = LeakDC.ar(sig) * -12.dbamp;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\sRate, ControlSpec(0.01,1,\exp), 1)
        .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) });
        
        controls[1] = NS_Control(\bits, ControlSpec(8,32,\exp), 32)
        .addAction(\synth,{ |c| synths[0].set(\bits, c.value) });

        controls[2] = NS_Control(\freq, ControlSpec(0.01,250,\exp), 4)
        .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });

        controls[3] = NS_Control(\which, ControlSpec(0,6,\lin,1), 0)
        .addAction(\synth,{ |c| synths[0].set(\which, c.value) });

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

        this.makeWindow("ShiftRegister", Rect(0,0,240,150));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlSwitch(controls[3], (0..6), 7),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(height: "45%"),
            OSC_Fader(),
            OSC_Switch(7, 7),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2)
        ], randCol: true).oscString("ShiftRegister")
    }
}
