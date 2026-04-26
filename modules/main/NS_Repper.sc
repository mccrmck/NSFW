NS_Repper : NS_SynthModule {
    var swell = false;
    var tapGroup, repGroup;
    var busses;

    buildSynthModule {

        tapGroup = Group(modGroup);
        repGroup = Group(tapGroup, \addAfter);

        busses = (
            send:  Bus.audio(modGroup.server, 1),           // sumBus
            dTime: Bus.control(modGroup.server, 1).set(0.1),
            atk:   Bus.control(modGroup.server, 1).set(0.01),
            rls:   Bus.control(modGroup.server, 1).set(2),
            curve: Bus.control(modGroup.server, 1).set(0),
            env:   Bus.control(modGroup.server, 1).set(3),
            amp:   Bus.control(modGroup.server, 1).set(0.5),
        );

        // maybe instead of/in addition to zippers up and down,
        // we could also use Latch to make some different pitched phrases?

        nsServer.addSynthDef(
            ("ns_repper" ++ numChans).asSymbol,
            {
                var sig = In.ar(\inBus.kr,1); // sum bus, only needs one channel
                var dTime = \dTime.kr(0.2) * Rand(0.75, 1);
                var atk = \atk.kr(0.01);
                var rls = \rls.kr(2);
                var dur = (atk + rls) * Rand(0.75,1);
                var lineDown = XLine.kr(dTime, dTime * 2, dur );
                var lineUp = XLine.kr(dTime, dTime / 2,dur);
                var direction = Select.kr(\which.kr(0), [dTime, lineDown, lineUp]);

                sig = sig * Env([0, 1, 1, 0], [0.01, 0.98, 0.01]).ar(gate:1, timeScale: dTime );
                sig = CombC.ar(sig, 1, direction, inf);
                sig = LeakDC.ar(sig);
                sig = sig.tanh;
                sig = NS_Pan(sig, numChans, \pan.kr(0), numChans / 4);
                sig = sig * Env.perc(atk, rls, 1, \curve.kr(-2)).ar(2);

                Out.ar(\outBus.kr, sig * \amp.kr(0.5))
            }
        );
      
        nsServer.addSynthDefCreateSynth(
            tapGroup,
            ("ns_repperTap" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                Out.ar(\sendBus.kr, sig.sum * numChans.reciprocal.sqrt);
                sig = sig * \drySig.kr(0);
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, modBus, \sendBus, busses['send']],
            { |synth|
                synths.add(synth);

                controlDict.addAll(
                    NS_ControlFloat(\dTime, ControlSpec(0.02, 1, \exp), 0.1)
                    .addAction(\synth, { |c| busses['dTime'].set( c.value ) }),

                    NS_ControlInt(\synth, 0, 2, 0)
                    .addAction(\synth, { |c|
                        if(gateBool) {
                            Synth(("ns_repper" ++ numChans).asSymbol, [
                                \inBus,  busses['send'],
                                \outBus, modBus,
                                \dTime,  busses['dTime'].getSynchronous,
                                \which,  c.value,
                                \atk,    busses['atk'].getSynchronous,
                                \rls,    busses['rls'].getSynchronous,
                                \curve,  busses['curve'].getSynchronous,
                                \pan,    1.0.rand2,
                                \amp,    busses['amp'].asMap
                            ], repGroup)
                        }
                    }, false),

                    NS_ControlFloat(\envDur, ControlSpec(2, 8, \exp), 3)
                    .addAction(\synth, { |c| 
                        var envDur = c.value;
                        busses['env'].set(envDur);
                        this.prSetEnv(envDur);
                    }),

                    NS_ControlInt(\decaySwell, 0, 1, 0)
                    .addAction(\synth, { |c|  
                        var envDur = busses['env'].getSynchronous;
                        swell = c.value.asBoolean;
                        this.prSetEnv(envDur)
                    }),

                    NS_ControlFloat(\drySig, ControlSpec(0, 1), 0)
                    .addAction(\synth, { |c| synths[0].set(\drySig, c.value) }),

                    NS_ControlFloat(\amp, ControlSpec(-24, 6, \db), -9)
                    .addAction(\synth, { |c| busses['amp'].set(c.value.dbamp) }),

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
            NS_ControlFader(controlDict['dTime']),
            NS_ControlSwitch(controlDict['synth'], ["flat", "down", "up"], 3),
            NS_ControlFader(controlDict['envDur']),
            NS_ControlButton(controlDict['decaySwell'], ["decay", "swell"], 2),
            NS_ControlButton(controlDict['drySig'], ["unmute thru", "mute thru"]),
            NS_ControlFader(controlDict['amp']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    prSetEnv { |envDur|
        if(swell == false) {
            busses['atk'].value_(0.01);
            busses['rls'].value_(envDur);
            busses['curve'].value_(4.neg);
        } {
            busses['atk'].value_(envDur);
            busses['rls'].value_(0.01);
            busses['curve'].value_(4);
        }
    }

    freeExtra {
        tapGroup.free;
        repGroup.free;
        busses.do(_.free)
    }

    // this needs a rewrite
    *oscFragment {       
        ^OpenStagePanel([
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2),
            OpenStageSwitch(3, 3, height: "30%"),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2),
        ], randCol: true).oscString("Repper")
    }
}
