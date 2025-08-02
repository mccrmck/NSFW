NS_GrainDelay : NS_SynthModule {
    classvar <isSource = false;

    /* SynthDef based on the similar SynthDef by PlaymodesStudio */
    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_grainDelay" ++ numChans).asSymbol,
            {
                var sig       = In.ar(\bus.kr, numChans).sum * numChans.reciprocal.sqrt;
                var sRate     = SampleRate.ir;

                var circleBuf = LocalBuf(sRate * 3, 1).clear;
                var bufFrames = BufFrames.kr(circleBuf) - 1;
                var writePos  = Phasor.ar(DC.ar(0), 1, 0, bufFrames);
                var rec       = BufWr.ar(sig /*+ LocalIn.ar(numChans)*/, circleBuf, writePos);

                var readPos   = Wrap.ar(writePos - (\dTime.kr(0.1) * sRate), 0, bufFrames);
                var grainDur  = \grainDur.kr(0.25);

                var trig      = Impulse.ar(\tFreq.kr(4));
                var pan       = Demand.ar(trig, 0, Dwhite(-1, 1));
                var durJit    = Demand.ar(trig, 0, Dwhite(1, 1.5));
                var posJit    = Demand.ar(trig, 0, Dwhite(0, grainDur)) * sRate;

                sig = GrainBuf.ar(
                    numChans,
                    trig,
                    grainDur * durJit, 
                    circleBuf <! rec, 
                    \rate.kr(1),
                    (readPos - posJit) / bufFrames,
                    pan: pan
                );

                // LocalOut.ar(sig * \coef.kr(0.9));

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\grainDur,ControlSpec(0.01,1,\exp),0.25)
                .addAction(\synth,{ |c| synths[0].set(\grainDur, c.value) });

                controls[1] = NS_Control(\tFreq,ControlSpec(2,80,\exp),4)
                .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });

                controls[2] = NS_Control(\dTime,ControlSpec(0.1,1.5,\exp),0.1)
                .addAction(\synth,{ |c| synths[0].set(\dTime, c.value) });

                controls[3] = NS_Control(\rate,ControlSpec(0.5,2,\exp),1)
                .addAction(\synth,{ |c| synths[0].set(\rate, c.value) });

                controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
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
        this.makeWindow("GrainDelay", Rect(0,0,240,120));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false,false),
                OpenStageButton(height:"20%")
            ], width: "15%")
        ], columns: 3, randCol: true).oscString("Grain Delay")
    }
}
