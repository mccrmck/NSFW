NS_SamplePB : NS_SynthModule{
    var busses;
    var bufArray, bufferPath;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(5);

        bufArray = Array.newClear(16);
        busses = (
            rate: Bus.control(server, 1).set(1),
            amp:  Bus.control(server, 1).set(1)
        );

        nsServer.addSynthDef(
            ("ns_samplePBmono" ++ numChans).asSymbol,
            {
                var bufnum   = \bufnum.kr;
                var sig = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * \rate.kr(1), doneAction: 2);

                // should I add an envelop with BufDur? This is lazy...

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                sig = NS_Pan(sig, numChans, Rand(-0.8, 0.8), numChans/4);

                // should I add a mix control here? 
                Out.ar(\bus.kr, sig);
                //NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
        );

        controls[0] = NS_Control(\which, ControlSpec(0,15,\lin,1),0)
        .addAction(\synth,{ |c| 
            Synth(("ns_samplePBmono" ++ numChans).asSymbol,[
                \bufnum, bufArray[c.value],
                \rate,   busses['rate'].getSynchronous,
                \amp,    busses['amp'].asMap,
                \bus,    strip.stripBus
            ], modGroup, \addToHead)
        }, false);

        controls[1] = NS_Control(\path, \string, "")
        .addAction(\synth,{ |c| 
            bufArray.do(_.free);
            PathName(c.value).entries.wrapExtend(16).do({ |entry, index|
                bufArray[index] = Buffer.readChannel(
                    server, entry.fullPath, channels: [0]
                );
            });
        }, false);

        controls[2] = NS_Control(\rate,ControlSpec(0.5,2,\exp),1)
        .addAction(\synth,{ |c| busses['rate'].set( c.value ) });

        controls[3] = NS_Control(\amp,\db,1)
        .addAction(\synth,{ |c| busses['amp'].set( c.value.dbamp ) });

        controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value) });

        { this.makeModuleWindow }.defer;
        loaded = true;
    }

    makeModuleWindow {
        this.makeWindow("SamplePB", Rect(0,0,210,210));

        win.layout_(
            VLayout(
                NS_ControlSwitch(controls[0], ""!16, 4).minHeight_(120),
                NS_ControlSink(controls[1]), // needs method: .string_("drag sample folder here")
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3], 1),
                NS_ControlButton(controls[4], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
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
