NS_Freeze : NS_SynthModule {
    var trigGroup, synthGroup;
    var bufferArray, bufIndex, localResponder;
    var busses;

    buildSynthModule {

        trigGroup  = Group(modGroup);
        synthGroup = Group(trigGroup, \addAfter);

        synths  = List.newClear(2);
        busses  = (
            send: Bus.audio(nsServer.server, 1),
            amp:  Bus.control(nsServer.server, 1).set(0.5);
        );

        nsServer.addSynthDef(
            ("ns_freeze" ++ numChans).asSymbol,
            {
                var sig = In.ar(\inBus.kr, 1);

                sig = FFT(\bufnum.kr, sig);
                sig = PV_Freeze(sig, 1);
                sig = IFFT(sig);

                // using these lines instead of NS_Envs to try and avoid some clicks
                // should test with NS_Env to see if it's still clicking
                sig = sig * Env.asr(0.5, 1, 0.02).ar(2, \gate.kr(1) + Impulse.kr(0));
                sig = sig * Env.asr(0, 1, 0).kr(1, \pauseGate.kr(1));

                sig = NS_Pan(sig, numChans, Rand(-0.8, 0.8), numChans / 4);

                Out.ar(\outBus.kr, sig * \amp.kr(1))
            }
        );

        nsServer.addSynthDefCreateSynth(
            trigGroup,
            ("ns_freezeTrig" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr,numChans);
                var sum = sig.sum * numChans.reciprocal.sqrt;
                var trig = FluidOnsetSlice.ar(sum, 9, \thresh.kr(1));
                var tFreq = \trigFreq.kr(0);
                trig = Select.ar(\which.kr(0), [trig, Impulse.ar(tFreq), Dust.ar(tFreq)]);
                trig = trig * \trigMute.kr(0);
                trig = trig + \trig.tr(0);

                SendTrig.ar(trig, 0, 1);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                Out.ar(\sendBus.kr, sum);
                sig = sig * \drySig.kr(0);
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
                //ReplaceOut.ar(\bus.kr,sig * (1 - \mix.kr(0.5)) )
            },
            [\bus, modBus, \sendBus, busses['send']],
            { |synth| 
                synths.put(0, synth);

                // this should maybe be in the finishing action of \ns_freeze?
                // it relies on \ns_freezeTrig to have a nodeID, so maybe not...

                localResponder.free;
                localResponder = OSCFunc({ |msg|
                    if(synths[1].notNil) { synths[1].set(\gate,0) };
                    synths.put(1, 
                        Synth(("ns_freeze" ++ numChans).asSymbol, [
                            \inBus,  busses['send'],
                            \bufnum, bufferArray[bufIndex],
                            \amp,    busses['amp'].asMap,
                            \outBus, modBus
                        ], synthGroup) 
                    );

                }, '/tr', argTemplate: [synths[0].nodeID]);

                bufIndex = 0;
                bufferArray = [128, 1024, 2048].collect { |frames| 
                    Buffer.alloc(nsServer.server, frames) 
                };

                controlDict.addAll(
                    NS_ControlInt(\whichTrig, 0, 2, 0)
                    .addAction(\synth, { |c| synths[0].set(\which, c.value) }),

                    NS_ControlInt(\fftSize, 0, 2, 0)
                    .addAction(\synth, { |c| bufIndex = c.value }),

                    NS_ControlFloat(\tFreq, ControlSpec(0, 4), 0.2)
                    .addAction(\synth, { |c| synths[0].set(\trigFreq, c.value) }),

                    NS_ControlFloat(\thresh, \db, 0)
                    .addAction(\synth, { |c| synths[0].set(\thresh, c.value.dbamp) }),

                    NS_ControlInt(\drySig, 0, 1, 0)
                    .addAction(\synth, { |c| synths[0].set(\drySig, c.value) }),

                    NS_ControlFloat(\amp, ControlSpec(-24, 6, \db), -9)
                    .addAction(\synth, { |c| busses['amp'].set(c.value.dbamp) }),

                    NS_ControlInt(\bypass, 0, 2, 0)
                    .addAction(\synth, { |c| 
                        var binVal = (c.value > 0).binaryValue;
                        c.value.switch(
                            0, { 
                                synths[0].set(\trigMute,0); 
                                synths[1].set(\gate, 0); 
                                synths[1] = nil
                            },
                            1, { 
                                synths[0].set(\trigMute,1)
                            },
                            2, { 
                                synths[0].set(\trigMute,0);
                                synths[0].set(\trig, 1)
                            }
                        );
                        this.gateBool_(binVal);
                        synths[0].set(\thru, binVal)
                    });
                );

                loaded = true;
            }
        )
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlSwitch(controlDict['whichTrig'], ["onsets", "impulse", "dust"], 3),
            NS_ControlSwitch(controlDict['fftSize'], ["128", "1024", "2048"], 3),
            NS_ControlFader(controlDict['tFreq']),
            NS_ControlFader(controlDict['thresh'], 1),
            NS_ControlButton(controlDict['drySig'], ["unmute thru", "mute thru"]),
            NS_ControlFader(controlDict['amp']),
            NS_ControlSwitch(controlDict['bypass'], ["free", "▶", "trig"], 3),
        )
    }

    freeExtra {
        trigGroup.free;
        synthGroup.free;
        bufferArray.do(_.free);
        busses.do(_.free);
        localResponder.free
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStagePanel([
                OpenStageSwitch(3, 3), 
                OpenStageSwitch(3, 3)
            ], columns: 2),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageSwitch(3, 3)
        ], randCol: true).oscString("Freeze")
    }
}
