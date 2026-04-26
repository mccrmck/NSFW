NS_Benjolin : NS_SynthModule {

    /* SynthDef based on the work of Alejandro Olarte, inspired by Rob Hordijk's Benjolin */

    buildSynthModule {

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_benjolin" ++ numChans).asSymbol,
            {
                var sh0, sh1, sh2, sh3, sh4, sh5, sh6, sh7, sh8 = 1, sig;

                var sr = SampleDur.ir;
                var local = LocalIn.ar(2, 0);
                var rungler = local[0];
                var buf = local[1];

                var loop = \loop.kr(0);
                var freq1 = \freq1.kr(40);
                var freq2 = \freq2.kr(4);
                var rungler1 = \rungler1.kr(0.5);
                var rungler2 = \rungler2.kr(0.5);

                var runglerFilt = \runglerFilt.kr(0.5);
                var filtFreq = \filtFreq.kr(250);
                var rq = \rq.kr(0.5);
                var gain = \gain.kr(1);
                var tri1 = LFTri.ar((rungler * rungler1) + freq1);
                var tri2 = LFTri.ar((rungler * rungler2) + freq2);
                var osc1 = PulseDPW.ar((rungler * rungler1) + freq1);
                var osc2 = PulseDPW.ar((rungler * rungler2) + freq2);

                var pwm = BinaryOpUGen('>', (tri1 + tri2), 0); // pwm = tri1 > tri2;

                osc1 = (buf * loop) + (osc1 * (1 - loop));  // loop spits out nans sometimes
                sh0 = BinaryOpUGen('>', osc1, 0.5);
                sh0 = BinaryOpUGen('==', (sh8 > sh0), (sh8 < sh0));
                sh0 = 1 - sh0;

                // this can probably be cleaned up with some clever syntax, no?
                sh1 = DelayN.ar(Latch.ar(sh0, osc2), 0.01, sr);
                sh2 = DelayN.ar(Latch.ar(sh1, osc2), 0.01, sr * 2);
                sh3 = DelayN.ar(Latch.ar(sh2, osc2), 0.01, sr * 3);
                sh4 = DelayN.ar(Latch.ar(sh3, osc2), 0.01, sr * 4);
                sh5 = DelayN.ar(Latch.ar(sh4, osc2), 0.01, sr * 5);
                sh6 = DelayN.ar(Latch.ar(sh5, osc2), 0.01, sr * 6);
                sh7 = DelayN.ar(Latch.ar(sh6, osc2), 0.01, sr * 7);
                sh8 = DelayN.ar(Latch.ar(sh7, osc2), 0.01, sr * 8);

                //rungler = ((sh6/8)+(sh7/4)+(sh8/2)); //original circuit
                //rungler = ((sh5/16)+(sh6/8)+(sh7/4)+(sh8/2));

                rungler = (
                    (sh1/2.pow(8)) + (sh2/2.pow(7)) + (sh3/2.pow(6)) + 
                    (sh4/2.pow(5)) + (sh5/2.pow(4)) + (sh6/2.pow(3)) + 
                    (sh7/2.pow(2)) + (sh8/2.pow(1))
                );

                buf     = rungler;
                rungler = (rungler * \scale.kr(1).linlin(0, 1, 0, 127));
                rungler = rungler.midicps;

                LocalOut.ar([rungler, buf]);

                sig = SelectX.ar(\whichSig.kr(5), [tri1, tri2, osc1, osc2, pwm, sh0]);

                sig = LeakDC.ar(sig);

                sig = SVF.ar(
                    sig, (rungler * runglerFilt) + filtFreq, 1 - rq,
                    \lowP.kr(1), \bandP.kr(0), \highP.kr(0), \notch.kr(0), \peak.kr(0),
                    mul: gain
                );

                sig = sig * -18.dbamp;

                sig = NS_Envs(sig.tanh, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\freq1, \freq, 40)
                    .addAction(\synth, { |c| synths[0].set(\freq1, c.value) }),

                    NS_ControlFloat(\freq2, ControlSpec(0.1, 14000, \exp), 4)
                    .addAction(\synth, { |c| synths[0].set(\freq2, c.value) }),

                    NS_ControlFloat(\filtFreq, \freq, 250)
                    .addAction(\synth, { |c| synths[0].set(\filtFreq, c.value) }),

                    NS_ControlFloat(\rq, ControlSpec(1, 0.01, -2), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\rq, c.value) }),

                    NS_ControlFloat(\rungler1, ControlSpec(0, 1), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\rungler1, c.value) }),

                    NS_ControlFloat(\rungler2, ControlSpec(0, 1), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\rungler2, c.value) }),

                    NS_ControlFloat(\runglerFilt, ControlSpec(0, 10), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\runglerFilt, c.value) }),

                    NS_ControlFloat(\gain, ControlSpec(0, 9, \db), 0)
                    .addAction(\synth, { |c| synths[0].set(\gain, c.value.dbamp) }),

                    NS_ControlInt(\whichSig, 0, 5, 5)
                    .addAction(\synth, { |c| synths[0].set(\whichSig, c.value) }),

                    NS_ControlInt(\whichFilt, 0, 4, 0)
                    .addAction(\synth, { |c|
                        var args = (lowP: 0, bandP: 0, highP: 0, notch: 0, peak: 0);

                       c.value.switch(
                            0, { args['lowP']  = 1 },
                            1, { args['bandP'] = 1 },
                            2, { args['highP'] = 1 },
                            3, { args['notch'] = 1 },
                            4, { args['peak']  = 1 },
                        );
                        synths[0].set(*args.asPairs)
                    }),

                    NS_ControlFloat(\loop, ControlSpec(0, 1), 0)
                    .addAction(\synth, { |c| synths[0].set(\loop, c.value) }),

                    NS_ControlFloat(\scale, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\scale, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        this.gateBool_(c.value); 
                        synths[0].set(\thru, c.value)
                    }),
                );

                loaded = true;
            }
        );
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['freq1'], 1),
            NS_ControlFader(controlDict['freq2'], 1),
            NS_ControlFader(controlDict['filtFreq'], 1),
            NS_ControlFader(controlDict['rq']),
            NS_ControlFader(controlDict['rungler1']),
            NS_ControlFader(controlDict['rungler2']),
            NS_ControlFader(controlDict['runglerFilt']),
            NS_ControlFader(controlDict['gain']),
            NS_ControlSwitch(
                controlDict['whichSig'], 
                ["tri1", "tri2", "osc1", "osc2", "pwm", "sh0"],
                6
            ),
            NS_ControlSwitch(
                controlDict['whichFilt'], 
                ["lpf", "bpf", "hpf", "notch", "peak"], 
                5
            ),
            NS_ControlFader(controlDict['loop']),
            NS_ControlFader(controlDict['scale']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageXY(),
            OpenStagePanel([
                OpenStageSwitch(6, 1), 
                OpenStageSwitch(5, 1)
            ], columns: 2),
            OpenStageXY(),
            OpenStageXY(),
            OpenStagePanel({OpenStageKnob(false)} ! 3 ++ [OpenStageButton()])
        ], columns: 3, randCol: true).oscString("Benjolin")
    }
}
