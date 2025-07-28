NS_AmpMod : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_ampMod" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var freq = \freq.kr(4);
                var pulse = LFPulse.ar(
                    freq, 
                    width: \width.kr(0.5), 
                    add: \offset.kr(0)
                ).clip(0,1);
                pulse = Lag.ar(pulse, freq.reciprocal * \lag.kr(0));
                sig = sig * pulse;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\freq, ControlSpec(1,10000,\exp), 4)
                .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });

                controls[1] = NS_Control(\width, ControlSpec(0.01,0.99,\lin), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\width, c.value) });

                controls[2] = NS_Control(\lag, ControlSpec(0,0.9,\lin),0)
                .addAction(\synth,{ |c| synths[0].set(\lag, c.value) });

                controls[3] = NS_Control(\offset, ControlSpec(0,0.999,\lin), 0)
                .addAction(\synth,{ |c| synths[0].set(\offset, c.value) });

                controls[4] = NS_Control(\mix, ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| 
                    this.gateBool_(c.value); 
                    synths[0].set(\thru, c.value)
                });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        );
    }

    makeModuleWindow {

        this.makeWindow("AmpMod", Rect(0,0,180,150));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 1),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶","bypass"]),
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
        ], columns: 3, randCol: true).oscString("AmpMod")
    }
}
