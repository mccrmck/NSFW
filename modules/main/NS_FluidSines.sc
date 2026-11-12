NS_FluidSines : NS_SynthModule {
    var maxPeaks = 50;
    var busses, localResponder;

    buildSynthModule {
        var sineSynthName = ("ns_sineCluster" ++ numChans).asSymbol;
        var attack = 0.1; 
        // for some reason the first bus in this Event doesn't get allocated or
        // something, so passing bus.asMap just outputs zeros for ever...
        // I'm using variable here to get around it, but wtf? does this need to be async?
        busses = (
            atk: Bus.control(nsServer.server, 1).set(0.1),
            rls: Bus.control(nsServer.server, 1).set(3),
            amp: Bus.control(nsServer.server, 1).set(-9.dbamp),
        );

        nsServer.addSynthDef(
            sineSynthName,
            {
                var freqs = \freqs.kr(220 ! maxPeaks);
                var amps  = \amps.kr(0 ! maxPeaks);
                var sig = SinOsc.ar(freqs, mul: amps); // this should eventually be Splay-ed

                // try to add some noise here
                sig = NS_Pan(sig.sum, numChans, \pan.kr(0));
                sig = sig * Env([0, 1, 0], [\atk.kr(0.01), \rls.kr(4)], \squared).ar(2, Impulse.ar(0));

                Out.ar(\outBus.kr, sig * \amp.kr(0.1))
            }
        );

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_sineFeature" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var sines = FluidSineFeature.kr(sig, \numPeaks.kr, maxNumPeaks: maxPeaks);
                // this needs to be flattened, otherwise it multichannel expands
                SendReply.kr(\trig.tr, '/sineFeatures', sines.flat); 
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                sig = sig * \drySig.kr(0);
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus],
            { |synth|
                synths.add(synth);

                localResponder.free;
                localResponder = OSCFunc({ |msg|
                    var vals  = msg[3..];
                    var freqs = vals[..(maxPeaks - 1)];
                    var amps  = vals[maxPeaks..];
                    Synth(sineSynthName, [
                        \freqs,    freqs.max(40),
                        \amps,     amps.min(1),
                        //\atk,      busses['atk'].asMap,
                        \atk,      attack,
                        \rls,      busses['rls'].asMap,
                        \amp,      busses['amp'].asMap,
                        \outBus,   modBus
                    ], modGroup, \addToTail) 
                }, '/sineFeatures', argTemplate: [synths[0].nodeID]);

                controlDict.addAll(
                    NS_ControlFloat(\numPeaks, ControlSpec(1, maxPeaks), floor(maxPeaks / 2))
                    .addAction(\synth, { |c| synths[0].set(\numPeaks, c.value.asInteger) }),

                    NS_ControlFloat(\atk, ControlSpec(0.01, 5, \lin), 0.1)
                    .addAction(\synth, { |c| attack = c.value }),

                    NS_ControlFloat(\rls, ControlSpec(0.01, 5, \lin), 3)
                    .addAction(\synth, { |c| busses['rls'].set(c.value) }),

                    NS_ControlInt(\trig, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\trig, c.value) }, false),

                    NS_ControlInt(\drySig, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\drySig, c.value) }),

                    NS_ControlFloat(\amp, ControlSpec(-9, 12, \db), -6)
                    .addAction(\synth, { |c| busses['amp'].set(c.value.dbamp) }),

                    NS_ControlInt(\bypass, 0, 1, 0)
                    .addAction(\synth, { |c| 
                        this.gateBool_(c.value.sign);
                        synths[0].set(\thru, c.value)
                    })
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlFader(controlDict['numPeaks'], 1),
            NS_ControlFader(controlDict['atk']),
            NS_ControlFader(controlDict['rls']),
            NS_ControlButton(controlDict['trig']),
            NS_ControlButton(controlDict['drySig'], ["unmute thru", "mute thru"]),
            NS_ControlFader(controlDict['amp']),
            NS_ControlButton.bypass(controlDict['bypass']),

        )
    }

    free { super.free; localResponder.free; busses.do(_.free) }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageButton().mode_("push"),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false),
                OpenStageButton().width_("20%")
            ]).columns_(2),
        ]).randCol.label_("FluidSines")
    }
}
