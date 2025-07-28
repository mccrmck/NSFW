NS_PolandFB : NS_SynthModule {
    classvar <isSource = true;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(9);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_polandFB" ++ numChans).asSymbol,
            { 
                // make this into a bus w/ switchable waveforms?
                var wave     = 40.collect({ |i| (i/40 * 2pi).sin });
                var fbBuf    = LocalBuf(1);

                var sig;
                var noise = Dwhite(-1, 1) * \noiseAmp.kr(0.05);
                var osc = DemandEnvGen.ar(
                    Dseq(wave, inf),
                    \oscFreq.kr(40).reciprocal / 40,
                    5, // shapeNumber 5 == curve
                    0, // curve 0 == linear interpolation
                    levelScale: \oscAmp.kr(0.04)
                );
                var in = Dbufrd(fbBuf);

                in = in + noise + osc;
                in = in.wrap2(\wrap.kr(5));
                in = in.round( 2 ** (\bits.kr(24) - 1).neg );

                sig = Dbufwr(in, fbBuf);
                sig = Duty.ar(\sRate.ar(server.sampleRate).reciprocal, 0, sig);
                //sig = SelectX.ar(\which.kr(0),[sig, sig.sign - sig]);
                sig = sig.fold2(\fold.kr(2));
                sig = LeakDC.ar(sig);
                sig = (sig * 4).clip2 * -15.dbamp;

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\oscAmp, ControlSpec(0,0.5,\amp),0.05)
        .addAction(\synth,{ |c| synths[0].set(\oscAmp, c.value) });

        controls[1] = NS_Control(\noiseAmp, ControlSpec(0,0.5,\amp),0.05)
        .addAction(\synth,{ |c| synths[0].set(\noiseAmp, c.value) });

        controls[2] = NS_Control(\sRate, ControlSpec(2000,server.sampleRate,\exp),24000)
        .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) });

        controls[3] = NS_Control(\bits, ControlSpec(2,24,\lin),16)
        .addAction(\synth,{ |c| synths[0].set(\bits, c.value) });

        controls[4] = NS_Control(\oscFreq, ControlSpec(0.1,250,\exp),40)
        .addAction(\synth,{ |c| synths[0].set(\oscFreq, c.value) });

        controls[5] = NS_Control(\wrap, ControlSpec(0.5,10,\exp),5)
        .addAction(\synth,{ |c| synths[0].set(\wrap, c.value) });

        controls[6] = NS_Control(\fold, ControlSpec(0.1,2,\lin),2)
        .addAction(\synth,{ |c| synths[0].set(\fold, c.value) });

        controls[7] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

        controls[8] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

        { this.makeModuleWindow }.defer;
        loaded = true;
    }

    makeModuleWindow {
        this.makeWindow("PolandFB",Rect(0,0,240,210));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2],1),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlFader(controls[5]),
                NS_ControlFader(controls[6]),
                NS_ControlFader(controls[7]),
                NS_ControlButton(controls[8], ["▶","bypass"]),
            )          
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {
        ^OpenStagePanel([
            OpenStagePanel({OpenStageXY()} ! 2, columns: 2, height: "50%"),
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2),
        ], randCol: true).oscString("PolandFB")
    }
}
