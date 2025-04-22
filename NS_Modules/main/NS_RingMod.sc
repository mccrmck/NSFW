NS_RingMod : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(5);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_ringMod" ++ numChans).asSymbol,
            {
                var sig  = In.ar(\bus.kr, numChans);
                var freq = \freq.kr(40).lag(0.05);
                var mod  = SinOsc.ar(\modFreq.kr(40), mul: \modMul.kr(1));
                sig = sig * SinOsc.ar(freq + mod);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\freq,ControlSpec(1,3500,\exp),40)
        .addAction(\synth,{ |c| synths[0].set(\freq, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\mFreq,ControlSpec(1,3500,\exp),4)
        .addAction(\synth,{ |c| synths[0].set(\modFreq, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\mMul,ControlSpec(1,3500,\amp))
        .addAction(\synth,{ |c| synths[0].set(\modMul, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(30);

        this.makeWindow("RingMod", Rect(0,0,180,120));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0], 1),               assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1], 1),               assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2], 1),               assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]),                  assignButtons[3] ),
                HLayout( NS_ControlButton(controls[4], ["▶","bypass"]), assignButtons[4] ),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(width: "70%"),
            OSC_Fader(true, false),
            OSC_Panel([OSC_Fader(false, false), OSC_Button(height: "20%")])
        ], columns: 3, randCol: true).oscString("RingMod")
    }
}
