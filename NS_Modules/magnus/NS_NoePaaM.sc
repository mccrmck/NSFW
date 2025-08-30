NS_NoePaaM : NS_SynthModule {
    var arpPat, busses;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(11);

        TempoClock.default.tempo = 92.5/60;

        arpPat = this.pattern;

        busses = (
            detune: Bus.control(server, 1).set(0.001),
            noise:  Bus.control(server, 1).set(0),
            rq:     Bus.control(server, 1).set(0.5),
            gain:   Bus.control(server, 1).set(1),
            atk:    Bus.control(server, 1).set(0.01),
            rls:    Bus.control(server, 1).set(0.125),
            mute:   Bus.control(server, 1).set(1),
            amp:    Bus.control(server, 4).setn(1!4),
            curve:  Bus.control(server, 1).set(0), // no Control for this atm...
        );

        nsServer.addSynthDef(
            'ns_noePaaM',
            {
                var numVoices = 4;
                var sig = SinOsc.ar(
                    \freq.kr() * 
                    LFNoise1.kr(0.1 ! numVoices, \detune.kr(0.001)).midiratio,
                    Rand(0,2pi)
                );

                sig = sig + HPF.ar(PinkNoise.ar(\noise.kr(0)).wrap2,\freq.kr());
                sig = RLPF.ar(sig, \filtFreq.kr(), \rq.kr(0.5));
                sig = (sig * \gain.kr(1)).tanh;
                sig = LeakDC.ar(sig);
                sig = Splay.ar(sig, 1, 1, \pan.kr(0));
                sig = sig * Env.perc(\atk.kr(0.01), \rls.kr(0.125), 1, \curve.kr(0)).ar(2);
                sig = sig * \amp.kr(1);

                // no idea why, but NS_Out messes up the multichannel expansion somehow..
                //sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                //NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
                Out.ar(\bus.kr,sig)
            }
        );

        controls[0] = NS_Control(\atk, ControlSpec(0.01,0.25,\exp), 0.01)
        .addAction(\synth,{ |c| busses['atk'].set(c.value) });

        controls[1] = NS_Control(\rls, ControlSpec(0.1,0.5,\exp), 0.125)
        .addAction(\synth,{ |c| busses['rls'].set(c.value) });

        controls[2] = NS_Control(\noise, ControlSpec(0,1,\lin), 0)
        .addAction(\synth,{ |c| busses['noise'].set(c.value) });

        controls[3] = NS_Control(\rq, ControlSpec(2.sqrt.reciprocal,0.01,\exp), 0.5)
        .addAction(\synth,{ |c| busses['rq'].set(c.value) });

        controls[4] = NS_Control(\detune, ControlSpec(0.001,1,\exp), 0.001)
        .addAction(\synth,{ |c| busses['detune'].set(c.value) });

        controls[5] = NS_Control(\gain, ControlSpec(1,40,\exp), 1)
        .addAction(\synth,{ |c| busses['gain'].set(c.value) });

        controls[6] = NS_Control(\mute, ControlSpec(0.25,1,\lin), 1)
        .addAction(\synth,{ |c| busses['mute'].set(c.value) });

        controls[7] = NS_Control(\harmony, ControlSpec(0,4,\lin,1), 0)
        .addAction(\synth,{ |c| 
            switch(c.value.asInteger,
                0, { Pbindef(\goy, \dummyF, Pstep(Pseq(this.data['aFlat'], inf)).midicps) },
                1, { Pbindef(\goy, \dummyF, Pstep(Pseq(this.data['hoved'], inf)).midicps) },
                2, { Pbindef(\goy, \dummyF, Pstep(Pseq(this.data['mm13'], inf)).midicps) },
                3, { Pbindef(\goy, \dummyF, Pstep(Pseq(this.data['mm37'], inf)).midicps) },
                4, { Pbindef(\goy, \dummyF, Pstep(Pseq(this.data['mm53'], inf)).midicps) }
            )
        });

        controls[8] = NS_Control(\arp_dB, \db, 0)
        .addAction(\synth,{ |c| 
            busses['amp'].subBus(0,3).setn(c.value.dbamp ! 3)
        });

        controls[9] = NS_Control(\ab_dB, \db, 0)
        .addAction(\synth,{ |c| busses['amp'].subBus(3).set(c.value.dbamp) });

        controls[10] = NS_Control(\playStop, \db, 0)
        .addAction(\synth,{ |c| 
            var val = c.value;             
            this.gateBool_(val);
            if(val == 1, { arpPat.play }, { arpPat.stop })
        });

    }

    makeModuleWindow {
        this.makeWindow("NoePaaM", Rect(0,0,300,300));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 0.001),
                NS_ControlFader(controls[1], 0.001),
                NS_ControlFader(controls[2]),       
                NS_ControlFader(controls[3], 0.001),
                NS_ControlFader(controls[4], 0.001),
                NS_ControlFader(controls[5]),       
                NS_ControlFader(controls[6]),       
                NS_ControlSwitch( controls[7], ["aFlat", "hoved", "mm13", "mm37", "mm53"], 5), 
                NS_ControlFader(controls[8]),
                NS_ControlFader(controls[9]),
                NS_ControlButton(controls[10], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    pattern { 
        ^Pdef(\noePaaM,
            Ppar([
                Pbindef(\goy,
                    \server,     modGroup.server,
                    \instrument, \ns_noePaaM,
                    \group,      modGroup.nodeID,
                    \dur,        Pwrand([0.25, Pseq([0.25/2], 2)], [0.5, 0.1].normalizeSum, inf),
                    \legato,     Pfunc{ {0.2.rrand(0.8)} ! 4 },
                    \dummyF,     Pstep(Pseq(this.data['aFlat'],inf)).midicps,
                    \freq,       Pkey(\dummyF) * Prand(([1,2]!4).allTuples,inf) *
                    Prand([[1,1,1,1],[1,1,1,0.5]],inf),
                    \filtFreq,   Pkey(\freq) * 2,
                    \detune,     Pfunc{ busses['detune'].getSynchronous },
                    \noise,      Pfunc{ busses['noise'].getSynchronous },
                    \rq,         Pfunc{ busses['rq'].getSynchronous } * Pkey(\dur), 
                    \gain,       Pfunc{ busses['gain'].getSynchronous },
                    \atk,        Pfunc{ busses['atk'].getSynchronous },
                    \rls,        Pfunc{ busses['rls'].getSynchronous },
                    \curve,      Pfunc{ busses['curve'].getSynchronous },
                    \pan,        [0,-0.5,0.5,0],
                    \accent,     Pfunc{ [1,0.5,0.5,0.5].scramble },
                    \mute,       Pfunc{ {(0.8.rand <= busses['mute'].getSynchronous).asInteger} ! 2 },
                    \amp,        Pfunc{ busses['amp'].getnSynchronous(4) } *
                                 Pkey(\mute) * Pkey(\accent) * -12.dbamp,
                    \bus,        strip.stripBus,
                ).quant_(1)
            ])
        )
    }

    data {
        ^Dictionary[
            'hoved' -> [          // also takt 5
                [59,63,64,68],
                [58,61,65,68],
                [59,61,64,68],
                [60,61,63,68],

                [58,63,67,68],
                [59,63,66,68],
                [58,63,65,68],
                [60,63,67,68],

                [59,63,66,68],  // "toppen"
                [59,61,65,68],
                [58,62,65,68],
                [59,63,64,68],

                [59,63,64,68],  
                [58,61,64,68],
                [57,61,64,68],
                [60,61,65,68],
            ],

            'mm13' -> [          
                [60,63,67,68],
                [59,63,65,68],
                [60,63,65,68],
                [60,63,65,68],

                [58,61,65,68],
                [58,61,65,68],
                [58,62,64,68],
                [59,62,64,68],

                [57,61,64,68],
                [58,61,65,68],
                [58,61,65,68],
                [59,63,66,68],

                [60,63,65,68],
                [58,61,65,68],
                [58,62,65,68],
                [59,62,64,68],

                [57,61,64,68],
                [58,61,65,68],
                [58,61,65,68],
                [59,63,66,68],

                [60,63,65,68],
                [58,61,65,68],
                [58,62,65,68],
                [59,62,64,68],

                [57,61,64,68],
                [58,61,65,68],
                [58,61,65,68],
                [59,63,66,68],

                [60,63,65,68],
                [58,61,65,68],
                [58,62,65,68],
                [59,62,64,68],

                [57,61,64,68],
                [58,61,65,68],
                [58,61,65,68],
                [59,63,66,68],

                [60,63,65,68],
                [58,61,65,68],
                [58,62,65,68],
                [59,62,64,68],

                [57,61,64,68],
                [58,61,65,68],
                [58,61,65,68],
                [59,63,66,68],

                [60,63,65,68],
                [58,61,65,68],
                [58,62,65,68],
                [60,63,67,68],
            ],

            'mm37' -> [          // 8 takter
                [59,63,66,68],
                [57,61,64,68],
                [58,59,64,68],
                [59,61,64,68],

                [57,60,64,68],
                [58,61,65,68],
                [60,61,65,68],
                [58,61,65,68],

                [58,63,65,68],
                [58,63,67,68],
                [58,63,67,68],
                [59,63,65,68],

                [60,63,66,68],
                [59,61,64,68],
                [57,61,64,68],
                [59,63,64,68],
            ],

            // 'mm45' -> hovedskjema

            'mm53' -> [          // 16 takter
                [60,63,67,68],
                [59,63,65,68],
                [60,63,65,68],
                [60,63,65,68],

                [58,61,65,68],
                [58,61,65,68],
                [58,62,64,68],
                [59,62,64,68],

                [57,61,64,68],
                [58,61,65,68],
                [58,61,65,68],
                [59,63,66,68],

                [60,63,65,68],
                [58,61,65,68],
                [58,61,65,68], // the notes here don't match the voicing in the chart...
                [60,63,67,68],

                [59,63,66,68],
                [57,61,64,68],
                [58,59,64,68],
                [59,61,64,68],

                [57,60,64,68],
                [58,61,65,68],
                [60,61,65,68],
                [58,61,65,68],

                [58,63,65,68],
                [58,63,67,68],
                [58,63,67,68],
                [59,63,65,68],

                [60,63,66,68],
                [59,61,64,68],
                [57,61,64,68],
                [59,63,64,68],
            ],

            // 'mm69' -> hovedskjema

            //'mm77' -> ~mm53, // 77-92

            'aFlat' -> [68!4],

            //'mm99' -> [      // all the way to 152
            //    [59,63,64,68],
            //    [58,61,65,68],
            //    [59,61,64,68],
            //    [60,61,63,68],

            //    [60,63,67,68],
            //    [59,63,66,68],
            //    [60,61,65,68],

            //    [60,63,67,68],
            //    [58,59,63,68],

            //    [59,63,64,68],

            //    [58,61,64,68],
            //    [57,61,64,68],

            //    [59,63,66,68],
            //    [58,61,66,68],
            //    [56,60,63,68],
            //    [54,60,63,68],

            //    68!4,

            //    [60,63,65,68],
            //    [58,62,65,68],
            //    [57,61,64,68],

            //    [56,60,63,68],

            //    68!4,

            //    [59,63,64,68],
            //    [59,62,65,68],
            //    [59,61,64,68],
            //    [60,61,63,68],

            //    [58,63,67,68],
            //    [59,63,66,68],
            //    [58,63,65,68],
            //    [60,63,67,68],

            //    [59,63,66,68],
            //    [59,61,65,68],
            //    [58,62,65,68],
            //    [59,63,64,68],

            //    [59,61,64,68],  // mm. 121
            //    [59,63,66,68],
            //    [60,63,67,68],
            //    [60,63,67,68], // mm. 122 -> this chord repeats...should it be a tie?

            //    [59,61,64,68],
            //    [60,63,67,68],

            //    [59,63,66,68],

            //    [59,63,64,68], // mm. 126
            //    [60,63,67,68],
            //    [60,63,65,68],

            //    [59,61,64,68],
            //    [60,63,67,68],
            //    [59,61,64,68],

            //    [60,62,65,68],
            //    [59,61,64,68],
            //    [60,63,67,68],

            //    [63,67,68,68],
            //    [63,68,63,68],

            //    68!4,

            //    [60,61,65,68], // mm. 135
            //    [59,63,65,68],
            //    [60,63,65,68],
            //    [58,62,65,68],

            //    [59,61,64,68],
            //    [62,63,67,68],
            //    [60,63,65,68],
            //    [59,63,64,68],
            //    [59,63,65,68],

            //    68!4,

            //    [59,63,64,68],
            //    [60,63,67,68],
            //    [60,63,65,68],

            //    [58,61,65,68],
            //    [59,63,64,68],
            //    [59,61,64,68],

            //    [60,62,65,68],
            //    [59,63,64,68],
            //    [59,63,66,68],

            //    [58,62,65,68],
            //    [57,61,64,68],
            //    [58,61,65,68],
            //    [59,63,66,68],
            //    [56,59,63,68],

            //    68!4,
            //],

            //'mm99Durs' -> [  // 1 == half note @ 92.5
            //    1, 1, 1, 1,
            //    0.75, 0.75, 1.25, 0.75, 3.5,
            //    2.25, 0.5, 0.5, 0.75, 0.75, 0.75, 1.5,
            //    3.5, // mm. 108
            //    2, 2, 2.5,
            //    2, 2,
            //    1!12, // mm. 115
            //    1, 0.75, 0.5, 0.75, 0.667, 0.333 + 1.5,
            //    4.25, // mm. 126
            //    0.5, 0.5, 2.75, 0.75, 0.5, 2.75, 1.25, 0.75, 2, 1, 1.5,
            //    4, // mm. 133
            //    1, 0.75, 2, 0.5, 0.5, 2, 0.5, 0.5, 2.75, 3,
            //    0.75, 0.5, 2.5, 1, 0.5, 2.75, 1.25, 0.75, 2.5,
            //    1.75, 0.75, 0.75, 0.75, 2, 2,

            //].flat,

            //'mm152' -> [          // 21 takter
            //    [59,63,64,68],
            //    [58,61,65,68],
            //    [59,61,64,68],
            //    [60,61,63,68],

            //    [58,63,67,68],
            //    [59,63,66,68],
            //    [58,63,65,68],
            //    [60,63,67,68],

            //    [59,63,66,68],
            //    [59,61,65,68],
            //    [60,62,65,68], // bottom note different than hovedskjema?
            //    [59,63,64,68],

            //    [63,64,68,68],
            //    [64,68,64,68],
            //    [65,68,65,68],
            //    [67,68,67,68],

            //    68!4,
            //    68!4,
            //    [63,64,68,68],
            //    [60,61,68,68],

            //    [63,67,68,68],
            //    [63,66,68,68],
            //    [63,65,68,68],
            //    [60,67,68,68],

            //    [61,64,68,68],
            //    [60,61,63,68],
            //    [58,65,68,68],
            //    [51,63,68,68],

            //    [59,62,64,68],
            //    [58,61,64,68],
            //    [57,61,64,68],
            //    [56,60,63,68],

            //    68!4,
            //    [59,68,59,68],
            //    [60,63,65,68],
            //    [60,63,65,68],

            //    [59,63,66,68],
            //    [59,63,66,68],
            //    [58,62,65,68],
            //    [58,62,65,68],

            //    [54,60,64,68],
            //    [54,60,64,68],
            //],

            //'mm193' -> [
            //    68!4,
            //    [63,68,63,68],
            //    [61,65,68,68],
            //    [59,63,68,68],
            //    [60,67,68,68],
            //    [60,62,68,68],
            //    [63,67,68,68],
            //    [58,63,65,68],
            //    [59,63,64,68],
            //    [57,61,64,68],
            //    [59,61,64,68],

            //    [54,63,64,68], // mm. 199
            //    [57,61,64,68],
            //    [57,61,62,68],
            //    [60,62,65,68],
            //    [61,63,65,68],
            //    [60,63,67,68],
            //    [56,60,63,68],
            //],

            //'mm193Durs' -> [  // 1 == half note @ 92.5
            //    1, 1, 1, 0.75, 0.5, 0.5, 1.25,
            //    1, 1, 2, 2, //mm. 199
            //    1, 3.5, 1.5,
            //    2, 1.5, 2, 2.5
            //],

            //'mm206' -> [[56,60,63,68]],

            //'mm311' -> [73!4],

            // 'mm315' -> [
            //     [59,63,64,68],
            //     [58,61,65,68],
            //     [59,61,64,68],
            //     [60,61,63,68],

            //     [58,63,67,68],
            //     [59,63,66,68],
            //     [58,63,65,68],
            //     [60,63,67,68],

            //     [59,63,66,68],  // starts here
            //     [59,61,65,68]
            // ] + 5,

            //'mm326' -> mm315.reverse,

            //'mm348' -> [
            //    [64,68,71,73],
            //    [63,68,72,73],
            //    [65,66,68,73],
            //    [64,66,69,73],
            //    [63,66,70,73],
            //    [64,68,69,73],
            //],

            // ~mm348 = ~mm348;
        ]
    }

    freeExtra {
        arpPat.clear;
        busses.do(_.free);
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStagePanel({ OpenStageXY() } ! 2, columns: 2, height: "40%"),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageSwitch(5, 5),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2) 
        ], randCol: true).oscString("NoePaaM")
    }
}
