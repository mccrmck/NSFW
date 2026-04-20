NS_SpeakerTest : NS_SynthModule {
    var currentChan = 0;

    buildSynthModule {
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_speakerTest" ++ numChans).asSymbol,
            {
                var freq = LFDNoise3.kr(1).range(80, 8000);
                var sig = Select.ar(\whichSig.kr(0),[
                    SinOsc.ar(freq, mul: AmpCompA.kr(freq, 80)) * -6.dbamp,
                    PinkNoise.ar(),
                ]);
                var pan = SelectX.kr(\whichPan.kr(0),[
                    \chan.kr(0),
                    LFSaw.ar(\rate.kr(0.05), 1).range(0, 2)
                ]);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0));
                sig = PanAz.ar(numChans, sig, pan, 1, 1, 0);
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, modBus],
            { |synth| 
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlInt(\whichSig, 0, 1, 0)
                    .addAction(\synth,{ |c| synths[0].set(\whichSig, c.value) }),


                    // I think we can create .next and .prev methods
                    // these don't need to be saved as controls

                    // these args should use `synth.get` to increment or
                    // decrement or an instance variable or something to be in
                    // sync  with the LFSaw
                    NS_ControlInt(\prev, 0, 0, 0)
                    .addAction(\synth,{ |c|
                        currentChan = (currentChan - 1).wrap(0, numChans - 1);
                        synths[0].set(\whichPan, 0, \chan, (currentChan * 2) / numChans)
                    }, false),

                    NS_ControlInt(\next, 0, 0, 0)
                    .addAction(\synth,{ |c|
                        currentChan = (currentChan + 1).wrap(0, numChans - 1);
                        synths[0].set(\whichPan, 0, \chan, (currentChan * 2) / numChans)
                    }, false), 

                    NS_ControlFloat(\rate, ControlSpec(0, 0.25, 'lin'), 0.05)
                    .addAction(\synth,{ |c| 
                        synths[0].set(\whichPan, 1, \rate, c.value)
                    }),

                    NS_ControlFloat(\amp, \db)
                    .addAction(\synth,{ |c| synths[0].set(\amp, c.value.dbamp) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth,{ |c| 
                        this.gateBool_(c.value);
                        synths[0].set(\thru, c.value)
                    })
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlSwitch(controlDict['whichSig'], ["sine", "noise"], 2),
            HLayout(
                NS_ControlButton(controlDict['prev'], ["prev"]),
                NS_ControlButton(controlDict['next'], ["next"])
            ),
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['amp']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageSwitch(2, 2),
            OpenStagePanel({ OpenStageButton() } ! 2, columns: 2),
            OpenStageFader(false),
            OpenStageFader(false),
            OpenStageButton()
        ], randCol: true).oscString("SpeakerTest")
    }
}
