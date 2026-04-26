NS_GrainDelay : NS_SynthModule {

    /* SynthDef based on the similar SynthDef by PlaymodesStudio */
    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_grainDelay" ++ numChans).asSymbol,
            {
                var sig       = In.ar(\bus.kr, numChans).sum * numChans.reciprocal.sqrt;
                var sRate     = SampleRate.ir;

                var circleBuf = LocalBuf(sRate * 3, 1).clear;
                var bufFrames = BufFrames.kr(circleBuf) - 1;
                var writePos  = Phasor.ar(DC.ar(0), 1, 0, bufFrames);
                var rec       = BufWr.ar(sig /*+ LocalIn.ar(numChans)*/, circleBuf, writePos);

                var readPos   = Wrap.ar(writePos - (\dTime.kr(0.1) * sRate), 0, bufFrames);
                var grainDur  = \grainDur.kr(0.25);

                var trig      = Impulse.ar(\tFreq.kr(4));
                var pan       = Demand.ar(trig, 0, Dwhite(-1, 1));
                var durJit    = Demand.ar(trig, 0, Dwhite(1, 1.5));
                var posJit    = Demand.ar(trig, 0, Dwhite(0, grainDur)) * sRate;

                sig = GrainBuf.ar(
                    numChans,
                    trig,
                    grainDur * durJit, 
                    circleBuf <! rec, 
                    \rate.kr(1),
                    (readPos - posJit) / bufFrames,
                    pan: pan
                );

                // LocalOut.ar(sig * \feedB.kr(-0.5.dbamp));

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\grainDur, ControlSpec(0.01, 1, \exp), 0.25)
                    .addAction(\synth, { |c| synths[0].set(\grainDur, c.value) }),

                    NS_ControlFloat(\tFreq, ControlSpec(2, 80, \exp), 4)
                    .addAction(\synth, { |c| synths[0].set(\tFreq, c.value) }),

                    NS_ControlFloat(\dTime, ControlSpec(0.1, 1.5, \exp), 0.1)
                    .addAction(\synth, { |c| synths[0].set(\dTime, c.value) }),

                    NS_ControlFloat(\rate, ControlSpec(0.5, 2, \exp), 1)
                    .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

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
            NS_ControlFader(controlDict['dTime']),
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false,false),
                OpenStageButton(height:"20%")
            ], width: "15%")
        ], columns: 3, randCol: true).oscString("Grain Delay")
    }
}
