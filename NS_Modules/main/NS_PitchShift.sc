NS_PitchShift : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(4);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_pitchShift" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;
                var pitch = Pitch.kr(sig)[0];
                sig = PitchShiftPA.ar(sig, pitch, \ratio.kr(1), \formant.kr(1), 20, 4);

                // sig = PitchShift.ar(sig,0.05,\ratio.kr(1),\pitchDev.kr(0),0.05);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth|
                synths.add(synth);

                controls[0] = NS_Control(\ratio, ControlSpec(0.25,4,\exp),1)
                .addAction(\synth,{ |c| synths[0].set(\ratio, c.value) });

                controls[1] = NS_Control(\formant, ControlSpec(0.25,4,\exp),1)
                .addAction(\synth,{ |c| synths[0].set(\formant, c.value) });

                controls[2] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[3] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
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
        this.makeWindow("PitchShift", Rect(0,0,180,90));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlButton(controls[3],["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(),
            OpenStagePanel([
                OpenStageFader(false, false), 
                OpenStageButton(height: "20%")
            ], width: "15%")
        ], columns: 2, randCol: true).oscString("PitchShift")
    }
}
