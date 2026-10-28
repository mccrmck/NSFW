NS_BufScratch : NS_SynthModule {
    var buffer;

    buildSynthModule {
        buffer = Buffer.alloc(nsServer.server, nsServer.options.sampleRate * 4, numChans);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_bufScratch" ++ numChans).asSymbol,
            {
                var in    = In.ar(\bus.kr, numChans);
                var buf   = \bufnum.kr;
                var rec   = \rec.kr(1);
                var dummy = RecordBuf.ar(in * rec.lag(0.5), buf, run: rec);

                var freq  = \freq.kr(1);
                var noise = LFDNoise0.ar(freq, \rateDev.kr(0.5)).range(0.25, 2);
                var rate  = BufRateScale.kr(buf) * \rate.kr(1) * noise * \direction.kr(1);

                var pos   = Phasor.ar(DC.ar(0), rate, 0, BufFrames.kr(buf));
                var sig   = BufRd.ar(numChans, buf, pos);

                sig = HPF.ar(sig, 30);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus, \bufnum, buffer],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\freq, ControlSpec(0.1, 250, 10), 1)
                    .addAction(\synth, { |c| synths[0].set(\freq, c.value) }),

                    NS_ControlFloat(\rateDev, ControlSpec(0.01, 1, \exp), 0.02)
                    .addAction(\synth, { |c| synths[0].set(\rateDev, c.value) }),

                    NS_ControlFloat(\rate, ControlSpec(0.25, 4, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

                    NS_ControlInt(\direction, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\direction, (c.value.neg * 2) + 1) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 2, 0)
                    .addAction(\synth, { |c| 
                        c.value.switch(
                            0, { synths[0].set(\rec, 1, \thru, 0) }, // rec, dry
                            1, { synths[0].set(\rec, 0, \thru, 1) }, // no rec, wet
                            2, { synths[0].set(\rec, 1, \thru, 1) }, // rec, wet
                        );
                        this.gateBool_(c.value.sign);
                    })
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['freq']),
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['rateDev']),
            NS_ControlButton(controlDict['direction'],["backward", "forward"]),
            NS_ControlFader(controlDict['mix']),
            NS_ControlSwitch(
                controlDict['bypass'],
                ["bypass", NS_Style('play'), NS_Style('play') + "+" + NS_Style('rec')],
                3
            )
        )
    }

    free {
        super.free;
        buffer.free;
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader().snap_(false),
            OpenStageFader().snap_(false),
            OpenStageFader(),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false),
                OpenStageButton().width_("20%")
            ]).columns_(2),
            OpenStageSwitch().columns_(3)
        ]).randCol.label_("BufScratch")
    }
}
