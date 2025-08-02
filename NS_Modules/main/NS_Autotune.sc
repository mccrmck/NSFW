NS_Autotune : NS_SynthModule {
    var buttons, transpose;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(3);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_autotune" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal.sqrt;
                var track     = Pitch.kr(sig);
                var pitch     = 20.max(track[0]);
                var quantMidi = pitch.cpsmidi.round;
                var pitchDif  = (quantMidi - pitch.cpsmidi).midiratio;
                var harm      = \harm.kr([0,1.5,2]).varlag(1,-10);
                var shift     = Mix(PitchShiftPA.ar(sig, pitch, pitchDif * harm, 1));
                sig           = SelectX.ar(track[1].lag(0.01),[sig, shift]);
                sig           = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                buttons = [
                    '5U'  , [0, 7, 12],
                    '5D'  , [0,-5,-12],
                    '5UD' , [0, 7, -5],

                    'maj0', [0, 4, 7],
                    'maj3', [0, 3, 8],
                    'maj5', [0, 5, 9],

                    'min0', [0, 3, 7],
                    'min3', [0, 4, 9],
                    'min5', [0, 5, 8],
                ];

                transpose = [[1],[0.5,1],[1,0.5]].allTuples;

                controls[0] = NS_Control(\harm, ControlSpec(0,(buttons.size/2) - 1,'lin',1), 0)
                .addAction(\synth,{ |c|
                    var val = c.value;
                    var harm = buttons[val * 2 + 1];
                    harm = if(val > 2,{ 
                        harm.midiratio * transpose.choose
                    },{
                        harm.midiratio
                    });
                    synths[0].set(\harm, harm)
                });

                controls[1] = NS_Control(\mix, ControlSpec(0,1,\lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[2] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value);
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true
            }
        );
    }

    makeModuleWindow {
        this.makeWindow("Autotune", Rect(0,0,180,150));

        win.layout_(
            VLayout(
                NS_ControlSwitch(controls[0], buttons[0, 2..], 3).minHeight_(90),
                NS_ControlFader(controls[1], 0.01),
                NS_ControlButton(controls[2], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageSwitch(9, 3),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2, height: "20%")
        ], randCol: true).oscString("Autotune")
    }
}
