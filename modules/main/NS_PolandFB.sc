NS_PolandFB : NS_SynthModule {

    buildSynthModule {
        var sRate = nsServer.options.sampleRate;
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_polandFB" ++ numChans).asSymbol,
            { 
                // make this into a bus w/ switchable waveforms?
                var wave  = 40.collect({ |i| (i/40 * 2pi).sin });
                var fbBuf = LocalBuf(1);

                var sig;
                var noise = Dwhite(-1, 1) * \noiseAmp.kr(0.05);
                var osc = DemandEnvGen.ar(
                    Dseq(wave, inf),
                    \oscFreq.kr(40).reciprocal / 40,
                    5, // shapeNumber 5 == curve
                    0, // curve 0 == linear interpolation
                    levelScale: \oscAmp.kr(0.04)
                );
                var in = Dbufrd(fbBuf);

                in = in + noise + osc;
                in = in.wrap2(\wrap.kr(5));
                in = in.round( 2 ** (\bits.kr(24) - 1).neg );

                sig = Dbufwr(in, fbBuf);
                sig = Duty.ar(\sRate.ar(sRate).reciprocal, 0, sig);
                //sig = SelectX.ar(\which.kr(0),[sig, sig.sign - sig]);
                sig = sig.fold2(\fold.kr(2));
                sig = LeakDC.ar(sig);
                sig = (sig * 4).clip2 * -15.dbamp;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\oscAmp, ControlSpec(0, 0.5, \amp), 0.05)
                    .addAction(\synth,{ |c| synths[0].set(\oscAmp, c.value) }),

                    NS_ControlFloat(\noiseAmp, ControlSpec(0, 0.5, \amp), 0.05)
                    .addAction(\synth,{ |c| synths[0].set(\noiseAmp, c.value) }),

                    NS_ControlFloat(\sRate, ControlSpec(2000, sRate, \exp), 24000)
                    .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) }),

                    NS_ControlFloat(\bits, ControlSpec(2, 24), 16)
                    .addAction(\synth,{ |c| synths[0].set(\bits, c.value) }),

                    NS_ControlFloat(\oscFreq, ControlSpec(0.1, 250, \exp), 40)
                    .addAction(\synth,{ |c| synths[0].set(\oscFreq, c.value) }),

                    NS_ControlFloat(\wrap, ControlSpec(0.5, 10, \exp), 5)
                    .addAction(\synth,{ |c| synths[0].set(\wrap, c.value) }),

                    NS_ControlFloat(\fold, ControlSpec(0.1, 2), 2)
                    .addAction(\synth,{ |c| synths[0].set(\fold, c.value) }),

                    NS_ControlFloat(\mix, ControlSpec(0, 1, \lin), 1)
                    .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value) 
                    })
                )
            }
        );

        loaded = true;
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['oscAmp']),
            NS_ControlFader(controlDict['noiseAmp']),
            NS_ControlFader(controlDict['sRate'], 1),
            NS_ControlFader(controlDict['bits']),
            NS_ControlFader(controlDict['oscFreq']),
            NS_ControlFader(controlDict['wrap']),
            NS_ControlFader(controlDict['fold']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )          
    }

    *oscFragment {
        ^OpenStagePanel([
            OpenStagePanel({OpenStageXY()} ! 2, columns: 2, height: "50%"),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2),
        ], randCol: true).oscString("PolandFB")
    }
}
