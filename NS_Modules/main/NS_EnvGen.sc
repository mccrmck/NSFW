NS_EnvGen : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(8);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_envGen" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var tFreq = \tFreq.kr(0.01);
                var rFreq = \rFreq.kr(0.25);
                var rMult = tFreq * \rMult.kr(1);


                // bug: revPerc can't update it's length during it's first (long) segment
                // maybe I cen refactor this using Demand Ugens and/or Latch?
                var ramp = Select.kr(\which.kr(0),[
                    0,
                    LFSaw.kr(rFreq).range(0, rMult),
                    LFTri.kr(rFreq).range(0, rMult)
                ]);
                var env = \env.kr(Env.perc(0.01,0.99,1,-4).asArray);
                tFreq = tFreq + ramp;

                env = EnvGen.ar(env, Impulse.kr(tFreq), timeScale: \tScale.kr(1) * tFreq.reciprocal);

                sig = sig * env;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\tFreq, ControlSpec(0.05, 12, \exp), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });

                controls[1] = NS_Control(\tScale, ControlSpec(0.01, 1, \lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\tScale, c.value) });

                controls[2] = NS_Control(\ramp, ControlSpec(0, 2, \lin, 1), 0)
                .addAction(\synth,{ |c| synths[0].set(\which, c.value) });

                controls[3] = NS_Control(\window, ControlSpec(0, 2, \lin,1), 0)
                .addAction(\synth,{ |c| 
                    var env = switch(c.value.asInteger,
                        0,{ Env.perc(0.01, 0.99, 1, 4.neg).asArray },
                        1,{ Env([0,1,0], [0.5,0.5], 'wel').asArray },
                        2,{ Env.perc(0.99, 0.01, 1, 4).asArray }
                    );

                    synths[0].set(\env, env) 
                });

                controls[4] = NS_Control(\rampHz,ControlSpec(0.1,5,\exp),0.25)
                .addAction(\synth,{ |c| synths[0].set(\rFreq, c.value) });

                controls[5] = NS_Control(\rampMul,ControlSpec(1,10,\exp),1)
                .addAction(\synth,{ |c| synths[0].set(\rMult, c.value) });

                controls[6] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[7] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value);
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        );
    }

    makeModuleWindow {
        this.makeWindow("EnvGen", Rect(0,0,180,210));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlSwitch(controls[2], ["impulse", "saw", "tri"], 3),
                NS_ControlSwitch(controls[3], ["perc", "welch", "revPerc"], 3),
                NS_ControlFader(controls[4]),
                NS_ControlFader(controls[5]),
                NS_ControlFader(controls[6]),
                NS_ControlButton(controls[7], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(horizontal: false),
            OpenStageFader(horizontal: false),
            OpenStageSwitch(3),
            OpenStageSwitch(3),
            OpenStageFader(horizontal: false),
            OpenStageFader(horizontal: false),
            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageButton(height:"20%")
            ])     
        ], columns: 7, randCol: true).oscString("EnvGen")
    }
}
