NS_SwellFB : NS_SynthModule {

    buildSynthModule {
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_swellFB" ++ numChans).asSymbol,
            {
                var in = In.ar(\bus.kr, numChans);
                var coef = \coef.kr(1);
                var thresh = \thresh.kr(0.5);
                var sig = in.sum *
                numChans.reciprocal.sqrt * 
                Env.sine(\dur.kr(0.1)).ar(gate: \trig.tr);
                var pan = Demand.kr(\trig.tr, 0, Dwhite(-1.0, 1.0));
                sig = sig + LocalIn.ar(1);
                sig = DelayC.ar(sig, 0.1, \delay.kr(0.03));
                sig = sig * (1 - Trig.ar(Amplitude.ar(sig) > thresh, 0.1)).lag(0.01);
                LocalOut.ar(sig * coef);
                sig = LeakDC.ar(HPF.ar(sig, 80));
                //sig = ReplaceBadValues.ar(sig,0,);

                sig = NS_Pan(sig, numChans, pan, numChans / 4);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0));
                sig = (in * \drySig.kr(0)) + sig;
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) ) 
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\delay, ControlSpec(1 / 500, 0.1, \exp), 0.03)
                    .addAction(\synth, { |c| synths[0].set(\delay, c.value) }),

                    NS_ControlFloat(\dur, ControlSpec(0.01, 0.1, \exp), 0.1)
                    .addAction(\synth, { |c| synths[0].set(\dur, c.value) }),

                    NS_ControlFloat(\coef, ControlSpec(0.95, 1.1), 1)
                    .addAction(\synth, { |c| synths[0].set(\coef, c.value) }),

                    NS_ControlFloat(\thresh, ControlSpec(-24, -3, \db), -6)
                    .addAction(\synth, { |c| synths[0].set(\thresh, c.value.dbamp) }),

                    NS_ControlInt(\trig, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\trig, c.value) }),

                    NS_ControlInt(\drySig, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\drySig, c.value) }),

                    NS_ControlFloat(\amp, \db, -18)
                    .addAction(\synth, { |c| synths[0].set(\amp, c.value.dbamp) }),

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
            NS_ControlFader(controlDict['delay'], 0.001),
            NS_ControlFader(controlDict['dur'], 0.001),
            NS_ControlFader(controlDict['coef']),
            NS_ControlFader(controlDict['thresh'], 1),
            NS_ControlButton(controlDict['trig'], "trig" ! 2),
            NS_ControlButton(controlDict['drySig'], ["unmute thru", "mute thru"]),
            NS_ControlFader(controlDict['amp']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(height: "60%"),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("SwellFB")
    }
}
