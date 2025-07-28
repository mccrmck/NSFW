NS_HenonSine : NS_SynthModule {
    classvar <isSource = true;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_henonSine" ++ numChans).asSymbol,
            {
                var freqRate = \fRate.kr(0.1);
                var noise = \noise.kr(0.5);
                var spread = \spread.kr(0.5);
                var freq = HenonL.ar(freqRate, noise, spread).clip2;
                var sig = SinOsc.ar(freq.linexp(-1,1,80,3500));
                sig = (sig * \gain.kr(1)).fold2;
                sig = sig * -18.dbamp;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\fRate,ControlSpec(0,250,4),0.1)
                .addAction(\synth,{ |c| synths[0].set(\fRate, c.value) });

                controls[1] = NS_Control(\noise,ControlSpec(1.1,1.4,\lin),0.1)
                .addAction(\synth,{ |c| synths[0].set(\noise, c.value) });

                controls[2] = NS_Control(\gain,ControlSpec(1,8,\exp),1)
                .addAction(\synth,{ |c| synths[0].set(\gain, c.value) });

                controls[3] = NS_Control(\spread,ControlSpec(0,0.3,\lin),0.1)
                .addAction(\synth,{ |c| synths[0].set(\spread, c.value) });

                controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("HenonSine", Rect(0,0,240,120));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 0.1),
                NS_ControlFader(controls[1], 0.001),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3], 0.001),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false, false), 
                OpenStageButton(height:"20%")
            ], width: "15%")
        ], columns: 3, randCol: true).oscString("HenonSine")
    }
}
