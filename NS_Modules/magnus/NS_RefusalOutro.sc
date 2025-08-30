NS_RefusalOutro : NS_SynthModule {
    var buffer, bufferPath;
    var ampBus;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_refusalOutro,{
            }).add
        }
    }

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);

        buffer = Buffer.read(server, "audio/refusalOutro.wav".resolveRelative);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_refusalOutro" ++ numChans).asSymbol,
            {
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var trig     = \trig.tr(0);
                var sig, pos = Phasor.ar(
                    TDelay.ar(T2A.ar(trig), 0.04),
                    BufRateScale.kr(bufnum) * \rate.kr(1),
                    \offset.kr(0) * frames,
                    frames
                );
                // slighty different than original, should check
                pos = SelectX.ar(DelayN.kr(\which.kr(0), 0.04), [
                    pos,
                    pos * LFDNoise1.kr(1).range(0.9, 1.1)
                ]);
                sig = BufRd.ar(2, bufnum, pos % frames, 4);
                sig = sig * Env([1, 0, 1],[0.04, 0.04]).ar(0, trig + Changed.kr(\which.kr));

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )  
            },
            [\bus, strip.stripBus, \bufnum, buffer],
            { |synth|
                synths.add(synth);

                controls[0] = NS_Control(\rate, ControlSpec(0.25,1,\exp), 1)
                .addAction(\synth,{ |c| synths[0].set(\rate, c.value) });

                controls[1] = NS_Control(\which, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| synths[0].set(\which, c.value) });

                controls[2] = NS_Control(\trig, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| synths[0].set(\trig, c.value) });

                controls[3] = NS_Control(\offset, ControlSpec(0,1),0)
                .addAction(\synth,{ |c| synths[0].set(\trig, 1, \offset, c.value) });

                controls[4] = NS_Control(\amp, \amp)
                .addAction(\synth,{ |c| synths[0].set(\amp, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1))
                .addAction(\synth,{ |c|  
                    var val = c.value;
                    strip.inSynthGate_(val);
                    synths[0].set(\trig, val, \thru, val)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("RefusalOutro", Rect(0,0,240,150));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlSwitch(controls[1], ["dry", "wet"], 2),
                NS_ControlButton(controls[2], "trig" ! 2),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStageSwitch(2, 2, 'slide'),
            OpenStageFader(),
            OpenStageButton('push'),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width: "20%")
            ], columns: 2)      
        ], randCol: true).oscString("RefusalOutro")
    }
}
