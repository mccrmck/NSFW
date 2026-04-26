NS_SamplePB : NS_SynthModule{
    var busses;
    var bufArray, bufferPath;

    buildSynthModule {

        bufArray = Array.newClear(16);
        busses = (
            rate: Bus.control(nsServer.server, 1).set(1),
            amp:  Bus.control(nsServer.server, 1).set(1)
        );

        nsServer.addSynthDef(
            ("ns_samplePBmono" ++ numChans).asSymbol,
            {
                var bufnum = \bufnum.kr;
                var rate   = BufRateScale.kr(bufnum) * \rate.kr(1);
                var sig    = PlayBuf.ar(1, bufnum, rate, doneAction: 2);

                // should I add an envelope with BufDur? This is lazy...

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                sig = NS_Pan(sig, numChans, Rand(-0.8, 0.8), numChans / 4);

                // should I add a mix control here? 
                Out.ar(\bus.kr, sig);
                //NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
        );

        controlDict.addAll(
            NS_ControlInt(\which, 0, 15,0)
            .addAction(\synth, { |c| 
                Synth(("ns_samplePBmono" ++ numChans).asSymbol, [
                    \bufnum, bufArray[c.value],
                    \rate,   busses['rate'].getSynchronous,
                    \amp,    busses['amp'].asMap,
                    \bus,    modBus
                ], modGroup, \addToHead)
            }, false),

            NS_ControlString(\path, "") // default val: "drag folder here" or something?
            .addAction(\synth, { |c| 
                bufArray.do(_.free);
                bufArray = Array.newClear(16);
                if(c.value.size > 0) {
                    PathName(c.value).entries.wrapExtend(16).do { |entry, index|
                        bufArray[index] = Buffer.readChannel(
                            nsServer.server, entry.fullPath, channels: [0]
                        );
                    };
                }
            }, false),

            NS_ControlFloat(\rate, ControlSpec(0.5, 2, \exp), 1)
            .addAction(\synth, { |c| busses['rate'].set( c.value ) }),

            NS_ControlFloat(\amp, \db, 1)
            .addAction(\synth, { |c| busses['amp'].set( c.value.dbamp ) }),

            NS_ControlInt(\bypass, 0, 1, 0)
            .addAction(\synth, { |c|
                // this needs more, of course!
                this.gateBool_(c.value)
            }),
        );

        loaded = true;
    }

    nsModuleLayout {
        ^VLayout(
            NS_ControlSwitch(controlDict['which'], ""!16, 4).minHeight_(120),
            NS_ControlSink(controlDict['path']),
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['amp'], 1),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    freeExtra {
        bufArray.do(_.free);
        busses.do(_.free)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageSwitch(16, 4, 'tap', height: "50%"),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2),
        ], randCol: true).oscString("SamplePB")
    }
}
