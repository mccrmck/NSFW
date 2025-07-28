NS_GateGrains : NS_SynthModule {
    classvar <isSource = false;
    var buffer;

    // inspired by/adapted from the FluidAmpGate helpfile example
    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(8);
        buffer = Buffer.alloc(server, server.sampleRate * 2);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_gateGrains" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr,numChans).sum * numChans.reciprocal;
                var thresh = \thresh.kr(-18);
                var width = \width.kr(0.5);
                var bufnum = \bufnum.kr;
                var slice = SampleRate.ir * 0.01;
                var gate = FluidAmpGate.ar(sig, 10, 10, thresh, thresh-5, slice, slice, slice, slice);
                var phase = Phasor.ar(DC.ar(0), 1 * gate, 0, BufFrames.kr(bufnum) - 1);
                var trig = Impulse.ar(\tFreq.kr(8)) * (1-gate);
                var pan = Demand.ar(trig,0,Dwhite(width.neg, width));
                var pos = \pos.kr(0) + Demand.ar(trig, 0, Dwhite(-0.002, 0.002));

                var rec = BufWr.ar(sig, bufnum, phase);

                // i could get fancy and add gain compensation based on overlap? must test...
                sig = GrainBuf.ar(
                    numChans, trig, \grainDur.kr(0.1), bufnum,
                    \rate.kr(1), pos.clip(0, 1), 4, pan
                );
                sig = sig.tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus, \bufnum, buffer],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\grainDur,ControlSpec(0.01,1,\exp),0.1)
                .addAction(\synth,{ |c| synths[0].set(\grainDur, c.value) });

                controls[1] = NS_Control(\tFreq,ControlSpec(4,80,\exp),8)
                .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });

                controls[2] = NS_Control(\pos,ControlSpec(0,1,\lin),0)
                .addAction(\synth,{ |c| synths[0].set(\pos, c.value) });

                controls[3] = NS_Control(\rate,ControlSpec(0.25,2,\exp),1)
                .addAction(\synth,{ |c| synths[0].set(\rate, c.value) });

                controls[4] = NS_Control(\thresh,ControlSpec(-72,-18,\db),-18)
                .addAction(\synth,{ |c| synths[0].set(\thresh, c.value) });

                controls[5] = NS_Control(\width,ControlSpec(0,1,\lin),0.5)
                .addAction(\synth,{ |c| synths[0].set(\width, c.value) });

                controls[6] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[7] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value);
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("GateGrains", Rect(0,0,210,180));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4], 1),
                NS_ControlFader(controls[5]),
                NS_ControlFader(controls[6]),
                NS_ControlButton(controls[7],["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStagePanel({OpenStageXY()} ! 2, columns: 2, height: "50%"),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false),
                OpenStageButton(width:"20%")
            ], columns: 2)
        ], randCol: true).oscString("GateGrains")
    }
}
