NS_ShortLoops : NS_SynthModule {
    var buffers, samps, phasorBus, phasorStart, phasorEnd;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);

        buffers     = Array.newClear(numChans);
        samps       = modGroup.server.sampleRate * 6;
        phasorBus   = Bus.control(server,1).set(0);
        phasorStart = 0;
        phasorEnd   = samps;

        numChans.do({ |index|
            buffers[index] = Buffer.alloc(modGroup.server, samps);
        });

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_shortLoops" ++ numChans).asSymbol,
            {
                var sig, in  = In.ar(\bus.kr, numChans);
                var bufnum   = \bufnum.kr(0 ! numChans);
                var frames   = BufFrames.kr(bufnum);

                var recHead  = Phasor.ar(DC.ar(0), \rec.kr(0), 0, frames);
                var rec      = numChans.collect{ |i| BufWr.ar(in[i], bufnum[i], recHead) };

                var trigLoop = \tLoop.tr(1);
                var plyStart = \playStart.kr(0) + \deviation.kr(0 ! numChans);
                var plyEnd   = \playEnd.kr(48000) + \offset.kr(0);
                var rate     = \rate.kr(1);
                var plyHead  = Phasor.ar(
                    TDelay.ar(T2A.ar(trigLoop), 0.02),
                    rate,
                    plyStart, 
                    plyEnd, 
                    plyStart
                ).wrap(0,frames);

                var duckTime = SampleRate.ir * 0.02 * rate;
                var duck     = plyHead > (plyEnd.wrap(0, frames) - duckTime);
                duck         = duck + (plyHead > (frames - duckTime));

                Out.kr(\phasorBus.kr, A2K.kr(recHead));

                sig = numChans.collect{ |i| BufRd.ar(1, bufnum[i], plyHead[i]) } * \mute.kr(0);
                sig = sig * Env([1, 0, 1], [0.02, 0.02]).ar(0, duck + trigLoop);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0));
                sig = (in * \drySig.kr(0)) + sig;
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) ) 
            },
            [\bus, strip.stripBus, \bufnum, buffers, \phasorBus, phasorBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\rate, ControlSpec(0.25,2,\exp),1)
                .addAction(\synth, { |c| synths[0].set(\rate, c.value) });

                controls[1] = NS_Control(\dev, ControlSpec(0,0.5,\lin),0)
                .addAction(\synth, { |c| 
                    var dev = { c.value.rand } ! numChans;
                    var delta = (phasorEnd - phasorStart).wrap(0, samps);
                    dev = delta * dev;
                    synths[0].set(\tLoop, 1, \deviation, dev) 
                });

                controls[2] = NS_Control(\recLoop, ControlSpec(0,1,\lin,1),0)
                .addAction(\synth, { |c| 
                    if(c.value == 1,{
                        synths[0].set(\rec,1);
                        phasorStart = phasorBus.getSynchronous;
                    },{
                        var offset = 0;
                        phasorEnd = phasorBus.getSynchronous;
                        if((phasorEnd - phasorStart).isNegative,{ offset = samps });
                        synths[0].set(
                            \rec,       0, 
                            \tLoop,     1, 
                            \playStart, phasorStart,
                            \playEnd,   phasorEnd,
                            \offset,    offset,
                            \mute,      1
                        )
                    })
                });

                controls[3] = NS_Control(\amp, \db, -18)
                .addAction(\synth,{ |c| synths[0].set(\amp, c.value.dbamp) });

                controls[4] = NS_Control(\drySig, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth, { |c| synths[0].set(\drySig, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
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
        this.makeWindow("ShortLoops", Rect(0,0,210,120));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlButton(controls[2], ["rec", "loop"]),
                NS_ControlFader(controls[3]),
                NS_ControlButton(controls[4], ["unmute thru", "mute thru"]),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    freeExtra {
        buffers.do(_.free);
        phasorBus.free;
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageButton('push', height: "40%"),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("ShortLoops")
    }
}
