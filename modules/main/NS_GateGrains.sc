NS_GateGrains : NS_SynthModule {
    var buffer;

    // inspired by/adapted from the FluidAmpGate helpfile example
    buildSynthModule {

        buffer = Buffer.alloc(nsServer.server, nsServer.options.sampleRate * 2);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_gateGrains" ++ numChans).asSymbol,
            {
                var sig    = In.ar(\bus.kr,numChans).sum * numChans.reciprocal;
                var thresh = \thresh.kr(-18);
                var width  = \width.kr(0.5);
                var bufnum = \bufnum.kr;
                var len    = SampleRate.ir * 0.01;
                var gate   = FluidAmpGate.ar(sig, 10, 10, thresh, thresh-5, len, len, len, len);
                var phase  = Phasor.ar(DC.ar(0), 1 * gate, 0, BufFrames.kr(bufnum) - 1);
                var trig   = Impulse.ar(\tFreq.kr(8)) * (1-gate);
                var pan    = Demand.ar(trig, 0, Dwhite(width.neg, width));
                var pos    = \pos.kr(0) + Demand.ar(trig, 0, Dwhite(-0.002, 0.002));

                var rec    = BufWr.ar(sig, bufnum, phase);

                // i could get fancy and add gain compensation based on overlap? must test...
                sig = GrainBuf.ar(
                    numChans, trig, \grainDur.kr(0.1), bufnum,
                    \rate.kr(1), pos.clip(0, 1), 4, pan
                );
                sig = sig.tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus, \bufnum, buffer],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\grainDur, ControlSpec(0.01, 1, \exp), 0.1)
                    .addAction(\synth, { |c| synths[0].set(\grainDur, c.value) }),

                    NS_ControlFloat(\tFreq, ControlSpec(4, 80, \exp), 8)
                    .addAction(\synth, { |c| synths[0].set(\tFreq, c.value) }),

                    NS_ControlFloat(\pos, ControlSpec(0, 1), 0)
                    .addAction(\synth, { |c| synths[0].set(\pos, c.value) }),

                    NS_ControlFloat(\rate, ControlSpec(0.25, 2, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

                    NS_ControlFloat(\thresh, ControlSpec(-72, -18, \db), -18)
                    .addAction(\synth, { |c| synths[0].set(\thresh, c.value) }),

                    NS_ControlFloat(\width, ControlSpec(0, 1), 0.5)
                    .addAction(\synth, { |c| synths[0].set(\width, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
                    .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    }),
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['grainDur']),
            NS_ControlFader(controlDict['tFreq']),
            NS_ControlFader(controlDict['pos']),
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['thresh'], 1),
            NS_ControlFader(controlDict['width']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStagePanel({OpenStageXY()} ! 2, columns: 2, height: "50%"),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width:"20%")
            ], columns: 2)
        ], randCol: true).oscString("GateGrains")
    }
}
