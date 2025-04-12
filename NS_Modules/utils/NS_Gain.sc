NS_Gain : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(2);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_gain" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = sig * \gain.kr(1);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\trim,\boostcut.asSpec,0)
        .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(30);

        this.makeWindow("Gain", Rect(0,0,230,60));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0], 0.01),             assignButtons[0] ),
                HLayout( NS_ControlButton(controls[1], ["▶", "bypass"]), assignButtons[1] )
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(false, false),
            OSC_Button(height: "20%"),
        ], randCol: true).oscString("Gain")
    }
}

