NS_Freedom2Live : NS_SynthModule {
    var arpPat, busses;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_freedom2Live,{
            }).add
        }
    }

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];

        this.initModuleArrays(8);

        TempoClock.default.tempo = 92.5/60;

        arpPat = this.pattern;

        busses = (
            dur:   Bus.control(server, 1).set(1),
            atk:   Bus.control(server, 1).set(0.01),
            rls:   Bus.control(server, 1).set(1),
            curve: Bus.control(server, 1).set(-4),
            rq:    Bus.control(server, 1).set(0.5),
            strum: Bus.control(server, 1).set(0),
            amp:   Bus.control(server, 1).set(1),
        );

        nsServer.addSynthDef(
            'ns_freedom2Live',
            {
                var filtEnv = XLine.kr(0.8, 1.2, Rand(0.1, 0.8));
                var sig = LFTri.ar([1, 2] * \freq.kr(80)).sum;

                sig = sig * SinOsc.kr(\tremFreq.kr(0.03), Rand(pi/2, pi)).range(0.25, 0.5);
                sig = RLPF.ar(sig, \filtFreq.kr(8000) * filtEnv, \rq.kr(1));
                sig = (sig * 2).tanh;
                sig = sig * Env.perc(\atk.kr(0.1), \rls.kr(1), 1, \curve.kr(-3)).ar(2);
                sig = LeakDC.ar(sig);

                sig = Pan2.ar(sig, \pan.kr(0), \amp.kr(0.2));
                Out.ar(\outBus.kr, sig);       
            }
        );

        controls[0] = NS_Control(\dur, ControlSpec(0.5,2,\exp), 1)
        .addAction(\synth,{ |c| busses['dur'].set(c.value) });

        controls[1] = NS_Control(\atk, ControlSpec(0.01,1,\exp), 0.01)
        .addAction(\synth,{ |c| busses['atk'].set(c.value) });

        controls[2] = NS_Control(\rls, ControlSpec(0.1,2,\exp), 1)
        .addAction(\synth,{ |c| busses['rls'].set(c.value) });

        controls[3] = NS_Control(\crv, ControlSpec(-10,10,\lin), -4)
        .addAction(\synth,{ |c| busses['curve'].set(c.value) });

        controls[4] = NS_Control(\rq, ControlSpec(0.05,1,\exp), 0.5)
        .addAction(\synth,{ |c| busses['rq'].set(c.value) });

        controls[5] = NS_Control(\strum, ControlSpec(-0.5,0.5,\lin), 0)
        .addAction(\synth,{ |c| busses['strum'].set(c.value) });

        controls[6] = NS_Control(\amp, \db, 0)
        .addAction(\synth,{ |c| busses['amp'].set(c.value.dbamp) });

        controls[7] = NS_Control(\bypass, ControlSpec(0,1,'lin',1))
        .addAction(\synth,{ |c|
            var val = c.value;
            this.gateBool_(val); 
            if(val == 1, { arpPat.play }, { arpPat.stop })
        });

        { this.makeModuleWindow }.defer;
        loaded = true;
    }

    makeModuleWindow {
        this.makeWindow("Freedom2Live", Rect(0,0,270,210));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlFader(controls[5]),
                NS_ControlFader(controls[6]),
                NS_ControlButton(controls[7], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    pattern { 
        ^Pdef(\liveFree, 
            Pbind(
                \server,     modGroup.server,
                \instrument, \ns_freedom2Live,
                \group,      modGroup.nodeID,
                \dur,        Pfunc{ busses['dur'].getSynchronous },
                \freq,       Pseq( this.data, inf ).midicps,
                \atk,        Pfunc{ busses['atk'].getSynchronous },
                \rls,        Pfunc{ busses['rls'].getSynchronous },
                \curve,      Pfunc{ busses['curve'].getSynchronous },
                \tremFreq,   Pwhite(0.5, 2, inf),
                \filtFreq,   Pfunc{ |event| event.freq.last },
                \rq,         Pfunc{ busses['rq'].getSynchronous },
                \strum,      Pfunc{ busses['strum'].getSynchronous } + Pbrown(-0.1,0.1,0.01),
                \pan,        Pgauss(0.0,0.3,inf),
                \amp,        Pfunc{ busses['amp'].getSynchronous } * -12.dbamp,
                \outBus,     strip.stripBus,
            )
        )
    }

    data {
        ^[
            [58, 63, 68, 72, 80],
            [63, 70, 77, 80, 89],
            [60, 68, 75, 77, 87],
            [56, 65, 70, 75, 82],
            [59, 64, 69, 74, 81],
            [64, 71, 74, 81, 86],
            [62, 69, 71, 76, 83],
            [57, 62, 64, 71, 76],
            [53, 57, 60, 67, 72],
            [48, 55, 57, 65, 69],
            [53, 60, 67, 69, 79],
            [55, 60, 65, 69, 77],
            [53, 58, 61, 67, 73],
            [49, 55, 60, 65, 72],
            [61, 67, 70, 77, 82],
            [58, 65, 67, 73, 79],
            [57, 62, 67, 77, 81],
            [55, 62, 65, 69, 77],
            [53, 57, 62, 67, 74],
            [50, 55, 57, 65, 69],
            [53, 57, 64, 65, 72, 77],
            [53, 60, 64, 69, 76],
            [52, 57, 60, 65, 72],
            [48, 53, 55, 64, 67],
            [47, 52, 54, 62, 66],
            [42, 50, 52, 59, 64],
            [50, 54, 59, 64, 71],
            [52, 59, 62, 78, 74],
            [52, 58, 62, 65, 74],
            [50, 53, 58, 64, 70],
            [53, 62, 64, 70, 76],
            [58, 64, 65, 74, 77],
            [58, 63, 67, 72, 79],
            [55, 60, 63, 70, 75],
            [51, 58, 60, 67, 72],
            [48, 55, 58, 63, 70],
            [62, 67, 70, 77, 82],
            [58, 65, 67, 74, 79],
            [55, 62, 65, 70, 77],
            [53, 58, 62, 67, 74],
            [61, 66, 69, 72, 77, 84],
            [54, 60, 65, 69, 72, 81],
            [54, 57, 65, 72, 78],
            [53, 54, 60, 65, 69, 77],
            [57, 63, 66, 72, 78],
            [60, 66, 72, 75, 81, 87],
            [57, 63, 69, 72, 78, 84],
            [59, 63, 69, 73, 78, 85],
            [62, 65, 70, 74, 81, 86],
            [64, 69, 72, 77, 84],
            [57, 58, 64, 65, 72, 77],
            [58, 64, 67, 72, 79],
            [60, 53, 68, 74, 80],
            [56, 62, 67, 72, 79],
            [53, 56, 60, 67, 72],
            [55, 60, 62, 68, 74],
            [58, 65, 66, 72, 78],
            [54, 60, 65, 70, 77],
            [48, 54, 58, 65, 70],
            [53, 58, 60, 66, 72],
            [53, 58, 61, 67, 73],
            [46, 53, 55, 61, 67],
            [49, 55, 58, 65, 70],
            [55, 61, 65, 70, 77],
            [58, 65, 69, 74, 81],
            [62, 69, 70, 77, 82],
            [57, 62, 65, 70, 77],
            [53, 58, 62, 69, 74],
            [54, 61, 65, 70, 77],
            [58, 65, 66, 73, 78],
            [49, 54, 58, 65, 70],
            [41, 49, 66, 58, 66],
            [41, 50, 53, 58, 65],
            [53, 58, 62, 69, 74],
            [50, 57, 58, 65, 70],
            [58, 65, 69, 74, 81],
            [60, 65, 68, 75, 80],
            [65, 72, 75, 80, 87],
            [63, 68, 72, 77, 84],
            [68, 75, 77, 84, 89],
            [70, 75, 78, 85, 90],
            [73, 78, 82, 87, 94],
            [75, 80, 84, 89, 96],
            [66, 73, 75, 82, 87],
            [68, 73, 77, 82, 89],
            [70, 77, 79, 85, 91],
            [61, 67, 70, 75, 82],
            [58, 65, 68, 73, 80],
            [59, 66, 69, 74, 81],
            [62, 69, 71, 78, 83],
            [57, 62, 66, 71, 78],
            [57, 62, 64, 71, 76],
            [62, 69, 72, 77, 84],
            [57, 62, 65, 72, 77],
            [60, 65, 69, 74, 81],
            [57, 65, 67, 74, 79],
            [61, 65, 66, 72, 73, 78],
            [60, 65, 70, 72, 77],
            [58, 60, 65, 66, 72],
            [54, 58, 60, 65, 70],
            [53, 58, 61, 67, 73],
            [49, 54, 58, 65, 72],
            [53, 58, 61, 67, 73],
            [61, 67, 72, 77, 84],
            [60, 65, 67, 69, 76, 81],
        ]  
    }

    freeExtra {
        arpPat.clear;
        busses.do(_.free);
    }

    *oscFragment {       
        ^OpenStagePanel(
            { OpenStageFader() }.dup(6) ++
            [ 
                OpenStagePanel([
                    OpenStageFader(false),
                    OpenStageButton(width: "20%")
                ], columns: 2)
            ], 
            randCol: true
        ).oscString("Free2Live")
    }
}
