NS_AmpMod : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_ampMod" ++ numChans).asSymbol,
            {
                var sig    = In.ar(\bus.kr, numChans);
                var freq   = \freq.kr(4);
                var rDuty  = \rDuty.kr(2);
                var phase  = Phasor.ar(DC.ar(0), freq * SampleDur.ir) * rDuty;
                var window = NS_UnitShape.gaussianWin(phase.clip(0, 1), \skew.kr(0.5), \index.kr(1));
               
                sig = sig * window;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\freq, ControlSpec(1, 5000, \exp), 4)
                .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });

                controls[1] = NS_Control(\rDuty, ControlSpec(1, 10, \lin), 2)
                .addAction(\synth,{ |c| synths[0].set(\rDuty, c.value) });

                controls[2] = NS_Control(\skew, ControlSpec(0, 0.99, \lin), 0.5)
                .addAction(\synth,{ |c| synths[0].set(\skew, c.value) });

                controls[3] = NS_Control(\index, ControlSpec(1, 8, \lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\index, c.value) });

                controls[4] = NS_Control(\mix, ControlSpec(0, 1, \lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0, 1, \lin, 1), 0)
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

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
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
