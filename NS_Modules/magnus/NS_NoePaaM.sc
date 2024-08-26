NS_NoePaaM : NS_SynthModule {
    classvar <isSource = true;
    var detuneBus, noiseBus, rqBus, gainBus, atkBus, rlsBus, curveBus, muteBus, ampBus;
    var arpPat;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_noePaaM,{
                var numChans = NSFW.numOutChans;
                var numVoices = 4;
                var sig = SinOsc.ar(\freq.kr() * LFNoise1.kr(0.1!numVoices,\detune.kr(0.001)).midiratio,Rand(0,2pi));
                sig = sig + HPF.ar(PinkNoise.ar(\noise.kr(0)).wrap2,\freq.kr());
                sig = RLPF.ar(sig,\filtFreq.kr(),\rq.kr(0.5));
                sig = (sig * \gain.kr(1)).tanh;
                sig = LeakDC.ar(sig);
                sig = Splay.ar(sig, 1, 1, \pan.kr(0));
                sig = sig * Env.perc(\atk.kr(0.01),\rls.kr(0.125),1,\curve.kr(0)).ar(2);
                sig = sig * \amp.kr(1);

                // no idea why, but NS_Out messes up the multichannel expansion somehow..
                //sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                //NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
                Out.ar(\bus.kr,sig)
            }).add
        }
    }

    init {
        this.initModuleArrays(9);
        strip.inSynthGate_(1);
        this.makeWindow("NoePaaM", Rect(0,0,420,320));

        TempoClock.default.tempo = 92.5/60;

        arpPat = this.pattern(modGroup, bus);

        detuneBus = Bus.control(modGroup.server,1).set(0.001);
        noiseBus  = Bus.control(modGroup.server,1).set(0);
        rqBus     = Bus.control(modGroup.server,1).set(0.5);
        gainBus   = Bus.control(modGroup.server,1).set(1);
        atkBus    = Bus.control(modGroup.server,1).set(0.01);
        rlsBus    = Bus.control(modGroup.server,1).set(0.125);
        curveBus  = Bus.control(modGroup.server,1).set(0);
        muteBus   = Bus.control(modGroup.server,1).set(1);
        ampBus    = Bus.control(modGroup.server,4).setn(1!4);

        controls.add(
            NS_XY("atk",ControlSpec(0.01,0.25,\exp),"rls", ControlSpec(0.1,0.5,\exp),{ |xy|
                var atk = xy.x;
                var rls = xy.y;
              var curve = 4 * (atk-rls).sign;
                atkBus.set(atk);
                rlsBus.set(rls);
                curveBus.set(curve);
            },[0.01,0.125]).round_([0.01,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("noise",ControlSpec(0,1,\lin),"rq", ControlSpec(2.sqrt.reciprocal,0.01,\exp),{ |xy|
                noiseBus.set(xy.x);
                rqBus.set(xy.y);
            },[0,0.5]).round_([0.01,0.01])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        controls.add(
            NS_Fader("detune",ControlSpec(0.001,1,\exp),{ |f| detuneBus.set( f.value ) },'horz',initVal:0.001).round_(0.001)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("gain",ControlSpec(1,40,\exp),{ |f| gainBus.set( f.value ) },'horz',initVal:1)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mute",ControlSpec(0.25,1,\lin),{ |f| muteBus.set( f.value ) },'horz',initVal:1)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            NS_Switch(["aFlat", "hoved","mm13","mm37","mm53"],{ |sw| 

                switch(sw.value,
                    0,{ Pbindef(\goy,\dummyF,Pstep(Pseq(this.data['aFlat'],inf)).midicps) },
                    1,{ Pbindef(\goy,\dummyF,Pstep(Pseq(this.data['hoved'],inf)).midicps) },
                    2,{ Pbindef(\goy,\dummyF,Pstep(Pseq(this.data['mm13'],inf)).midicps) },
                    3,{ Pbindef(\goy,\dummyF,Pstep(Pseq(this.data['mm37'],inf)).midicps) },
                    4,{ Pbindef(\goy,\dummyF,Pstep(Pseq(this.data['mm53'],inf)).midicps) }
                )
            },'horz')
        );
        assignButtons[5] = NS_AssignButton(this, 5, \switch).maxWidth_(45);

        controls.add(
            NS_Fader("arp_dB",\db,{ |f| ampBus.subBus(0,3).setn(f.value.dbamp!3) },initVal:0).maxWidth_(45)
        );
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("ab_dB",\db,{ |f| ampBus.subBus(3).set(f.value.dbamp) },initVal:0).maxWidth_(45)
        );
        assignButtons[7] = NS_AssignButton(this, 7, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;             
                if(val == 1,{
                    arpPat.play
                },{
                    arpPat.stop
                })
            })
        );
        assignButtons[8] = NS_AssignButton(this, 8, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout(
                    HLayout(
                        VLayout( controls[0], assignButtons[0] ),
                        VLayout( controls[1], assignButtons[1] ),
                    ),
                    HLayout( controls[2], assignButtons[2] ),
                    HLayout( controls[3], assignButtons[3] ),
                    HLayout( controls[4], assignButtons[4] ),
                    HLayout( controls[5], assignButtons[5] ),
                ),
                VLayout( controls[6], assignButtons[6] ),
                VLayout( controls[7], assignButtons[7], controls[8], assignButtons[8])
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    pattern { |grp, outBus|
        ^Pdef(\noePaaM,
            Ppar([
                Pbindef(\goy,
                    \server,  grp.server,
                    \instrument, \ns_noePaaM,
                    \group,   grp.nodeID,
                    \dur,     Pwrand([0.25,Pseq([0.25/2],2)],[0.5,0.1].normalizeSum,inf),
                    \legato,  Pfunc{ {0.2.rrand(0.8)}!4 },
                    \dummyF,  Pstep(Pseq(this.data['aFlat'],inf)).midicps,
                    \freq,    Pkey(\dummyF) * Prand(([1,2]!4).allTuples,inf) * Prand([[1,1,1,1],[1,1,1,0.5]],inf),
                    \filtFreq,Pkey(\freq) * 2,
                    \detune,  Pfunc{ detuneBus.getSynchronous },
                    \noise,   Pfunc{ noiseBus.getSynchronous },
                    \rq,      Pfunc{ rqBus.getSynchronous } * Pkey(\dur), 
                    \gain,    Pfunc{ gainBus.getSynchronous },
                    \atk,     Pfunc{ atkBus.getSynchronous },
                    \rls,     Pfunc{ rlsBus.getSynchronous },
                    \curve,   Pfunc{ curveBus.getSynchronous },
                    \pan,     [0,-0.5,0.5,0],
                    \accent,  Pfunc{ [1,0.5,0.5,0.5].scramble },
                    \mute,    Pfunc{ { (0.8.rand <= muteBus.getSynchronous).asInteger }!2 },
                    \amp,     Pfunc{ ampBus.getnSynchronous(4) } * Pkey(\mute) * Pkey(\accent) * -12.dbamp,
                    \bus,     outBus,
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
        detuneBus.free;
        noiseBus.free;
        rqBus.free;
        gainBus.free;
        atkBus.free;
        rlsBus.free;
        curveBus.free;
        muteBus.free;
        ampBus.free;
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false,widgetArray:[
            OSC_Panel(height: "40%",widgetArray:[
                OSC_XY(snap:true),
                OSC_XY(snap:true)
            ]),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true, snap: true),
            OSC_Switch(numPads: 5),
            OSC_Fader(horizontal: true)
        ],randCol:true).oscString("NoePaaM")
    }
}
