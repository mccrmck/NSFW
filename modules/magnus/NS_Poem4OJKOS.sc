NS_Poem4OJKOS : NS_SynthModule {
    var buffer;

    buildSynthModule {

        buffer = Buffer.read(nsServer.server, "audio/poem.wav".resolveRelative);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_poem4ojkos" ++ numChans).asSymbol,
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
                pos = SelectX.ar(DelayN.kr(\which.kr(0), 0.04),[
                    pos,
                    pos * LFDNoise1.kr(1).range(0.9, 1.1)
                ]);
                sig = BufRd.ar(2, bufnum, pos % frames, 4);
                sig = sig * Env([1, 0, 1], [0.04, 0.04]).ar(0, trig + Changed.kr(\which.kr));

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )  
            },
            [\bus, modBus, \bufnum, buffer],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\rate, ControlSpec(0.25, 1, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

                    NS_ControlInt(\which, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\which, c.value) }),

                    NS_ControlInt(\trig, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\trig, c.value) }),

                    NS_ControlFloat(\offset, ControlSpec(0, 1), 0)
                    .addAction(\synth, { |c| synths[0].set(\trig, 1, \offset, c.value) }),

                    NS_ControlFloat(\amp, \amp)
                    .addAction(\synth, { |c| synths[0].set(\amp, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c|  
                        var val = c.value;
                        this.gateBool_(val);
                        synths[0].set(\trig, val, \thru, val)
                    }),
                );

                loaded = true;
            }
        );
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['rate']),
            NS_ControlSwitch(controlDict['which'], ["dry", "wet"], 2),
            NS_ControlButton(controlDict['trig'], "trig" ! 2),
            NS_ControlFader(controlDict['offset']),
            NS_ControlFader(controlDict['amp']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageSwitch().numPads_(2).columns_(2),
            OpenStageFader(),
            OpenStageButton().mode_('push'),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false), 
                OpenStageButton().width_("20%")
            ]).columns_(2)      
        ]).randCol.label_("Poem4OJKOS")
    }
}
