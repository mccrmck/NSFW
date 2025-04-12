NS_PitchShift : NS_SynthModule {
    classvar <isSource = false;

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
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\ratio, ControlSpec(0.25,4,\exp),1)
        .addAction(\synth,{ |c| synths[0].set(\ratio, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\formant, ControlSpec(0.25,4,\exp),1)
        .addAction(\synth,{ |c| synths[0].set(\formant, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(30);

        this.makeWindow("PitchShift", Rect(0,0,240,90));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]),                 assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]),                 assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]),                 assignButtons[2] ),
                HLayout( NS_ControlButton(controls[3],["▶","bypass"]), assignButtons[3] ),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(),
            OSC_Panel([
                OSC_Fader(false, false), 
                OSC_Button(height: "20%")
            ], width: "15%")
        ], columns: 2, randCol: true).oscString("PitchShift")
    }
}
