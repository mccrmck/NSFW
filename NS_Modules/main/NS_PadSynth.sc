NS_PadSynth : NS_SynthModule {
    var root, chord;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(7);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_padSynth" ++ numChans).asSymbol,
            {
                var freq = \freq.kr([36, 43, 52].midicps);
                var fFreq = LFNoise2.kr(0.3).range(1000, 3000);
                var width = LFNoise2.kr(0.4).range(0.1, 0.4);
               // var sig = VarSaw.ar(
               //     freq * LFNoise2.kr(0.1!3).range(-0.1,0.1).midiratio,
               //     width: width,
               //     mul: \gain.kr(0.2).lag(0.1)
               // ).fold2.sum;
                var sig = Pulse.ar(
                    freq.lag(0.1) * LFNoise2.kr(0.1!3).range(-0.1, 0.1).midiratio,
                    width: width,
                    mul: \gain.kr(0.2).lag(0.1)
                ).fold2.sum;
                sig = sig + PinkNoise.ar(\noiseAmp.kr(0));
                sig = sig * -12.dbamp;

                sig = RLPF.ar(sig.tanh, fFreq, \rq.kr(0.5));
                sig = sig + CombC.ar(sig, 0.4, LFNoise2.kr(0.1).range(0.2,0.4), 2, 0.5);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                root  = 36;
                chord = [0, 7, 16];

                controls[0] = NS_Control(\root,ControlSpec(0,11,\lin,1), 0)
                .addAction(\synth,{ |c|
                    var notes = (36..47);
                    root = notes[c.value];
                    synths[0].set(\freq, (chord + root).midicps * ([1] ++ ({ [0.5,1,2].choose }!2)) )
                });

                controls[1] = NS_Control(\chord, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c|
                    var chords = [[0, 19, 28], [0, 19, 27]];
                    chord = chords[c.value];
                    synths[0].set(\freq, (chord + root).midicps * ([1] ++ ({ [0.5, 1, 2].choose }!2)) )
                });

                controls[2] = NS_Control(\rq, ControlSpec(0.1,1,\exp), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\rq, c.value) });

                controls[3] = NS_Control(\noise, \amp, 0)
                .addAction(\synth,{ |c| synths[0].set(\noiseAmp, c.value) });

                controls[4] = NS_Control(\gain, \amp, 0.2)
                .addAction(\synth,{ |c| synths[0].set(\gain, c.value) });

                controls[5] = NS_Control(\mix, ControlSpec(0,1,\lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[6] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value); 
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("PadSynth", Rect(0,0,240,210));

        win.layout_(
            VLayout(
                NS_ControlSwitch(
                    controls[0], 
                    ["C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"], 
                    6
                ), 
                NS_ControlSwitch(controls[1], ["maj", "min"], 2).maxHeight_(20),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlFader(controls[5]),
                NS_ControlButton(controls[6], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageSwitch(12),
            OpenStageSwitch(2),
            OpenStageFader(horizontal: false),
            OpenStageFader(horizontal: false),
            OpenStageFader(horizontal: false),
            OpenStagePanel([
                OpenStageFader(false, false), 
                OpenStageButton(height: "20%")
            ])
        ], columns: 6, randCol: true).oscString("PadSynth")
    }
}
