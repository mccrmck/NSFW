NS_Mute : NS_SynthModule {

    buildSynthModule {
        // must consider if this module should immediately open the gateBool
        // another option would be to invert the envelope so upon "unmuting"
        // the envelope opens and the stripGate is opened
        // ...but then when I "mute" the gate will close immediately, even with
        // a long release time...

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_mute" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var mute = Env.asr(\atk.kr, 1, \rls.kr, \lin).ar(0, 1 - \mute.kr(0));
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                this.gateBool_(true);

                controlDict.addAll(
                    NS_ControlFloat(\atk, ControlSpec(0.01, 10), 0.02)
                    .addAction(\synth,{ |c| synths[0].set(\atk, c.value) }),

                    NS_ControlFloat(\rls, ControlSpec(0.01, 10), 0.02)
                    .addAction(\synth,{ |c| synths[0].set(\rls, c.value) }),

                    NS_ControlInt(\mute, 0, 1, 0)
                    .addAction(\synth,{ |c| synths[0].set(\mute, c.value) })
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['atk']),
            NS_ControlFader(controlDict['rls']),
            NS_ControlButton(controlDict['mute'], ["mute", "▶"])
        )
    }

    freeExtra {
        this.gateBool_(false)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStagePanel([
                OpenStageFader(false, false),
                OpenStageFader(false, false)
            ]),
            OpenStageButton(height: "25%")
        ], randCol: true).oscString("Mute")
    }
}
