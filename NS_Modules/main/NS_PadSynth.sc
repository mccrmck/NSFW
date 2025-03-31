NS_PadSynth : NS_SynthModule {
    classvar <isSource = true;
    var root, chord;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);
            
            SynthDef(\ns_padSynth,{
                var freq = \freq.kr([36,43,52].midicps);
                var fFreq = LFNoise2.kr(0.3).range(1000,3000);
                var width = LFNoise2.kr(0.4).range(0.1,0.4);
                //var sig = VarSaw.ar(freq * LFNoise2.kr(0.1!3).range(-0.1,0.1).midiratio,width: width,mul:\gain.kr(0.2).lag(0.1)).fold2.sum;
                var sig = Pulse.ar(freq.lag(0.1) * LFNoise2.kr(0.1!3).range(-0.1,0.1).midiratio,width: width,mul: \gain.kr(0.2).lag(0.1)).fold2.sum;
                sig = sig + PinkNoise.ar(\noiseAmp.kr(0));
                sig = sig * -12.dbamp;

                sig = RLPF.ar(sig.tanh,fFreq,\rq.kr(0.5));
                sig = sig + CombC.ar(sig, 0.4,LFNoise2.kr(0.1).range(0.2,0.4),2,0.5);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(7);
        this.makeWindow("PadSynth", Rect(0,0,240,210));

        synths.add( Synth(\ns_padSynth,[\bus,bus],modGroup) );

        root  = 36;
        chord = [0,7,16];

        controls[0] = NS_Control(\root,ControlSpec(0,11,\lin,1), 0)
        .addAction(\synth,{ |c|
            var notes = (36..47);
            root = notes[c.value];
            synths[0].set(\freq, (chord + root).midicps * ([1] ++ ({ [0.5,1,2].choose }!2)) )
        });
        assignButtons[0] = NS_AssignButton(this, 0, \switch).maxWidth_(30);

        controls[1] = NS_Control(\chord, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c|
            var chords = [ [0,19,28], [0,19,27] ];
            chord = chords[c.value];
            synths[0].set( \freq, (chord + root).midicps * ([1] ++ ({ [0.5,1,2 ].choose }!2)) )
        });
        assignButtons[1] = NS_AssignButton(this, 1, \switch).maxWidth_(30);

        controls[2] = NS_Control(\rq, ControlSpec(0.1,1,\exp), 0.5)
        .addAction(\synth,{ |c| synths[0].set(\rq, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\noise, \amp, 0)
        .addAction(\synth,{ |c| synths[0].set(\noiseAmp, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\gain, \amp, 0.2)
        .addAction(\synth,{ |c| synths[0].set(\gain, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\mix, ControlSpec(0,1,\lin), 1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(30);

        controls[6] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlSwitch(controls[0],["C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"],6), assignButtons[0] ),
                HLayout( NS_ControlSwitch(controls[1],["maj","min"], 2)                                        , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                                                          , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3])                                                          , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4])                                                          , assignButtons[4] ),
                HLayout( NS_ControlFader(controls[5])                                                          , assignButtons[5] ),
                HLayout( NS_ControlButton(controls[6],["▶","bypass"])                                          , assignButtons[6] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Switch(12),
            OSC_Switch(2),
            OSC_Fader(horizontal: false),
            OSC_Fader(horizontal: false),
            OSC_Fader(horizontal: false),
            OSC_Panel([OSC_Fader(false, false), OSC_Button(height:"20%")])
        ], columns: 6, randCol:true).oscString("PadSynth")
    }
}
