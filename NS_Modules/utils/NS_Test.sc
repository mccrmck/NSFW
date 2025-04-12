NS_Test : NS_SynthModule {
    classvar <isSource = true;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(3);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_test" ++ numChans).asSymbol,
            {
                var freq = LFDNoise3.kr(1).range(80,8000);
                var sig = Select.ar(\which.kr(0),[
                    SinOsc.ar(freq,mul: AmpCompA.kr(freq, 80)),
                    PinkNoise.ar()
                ]);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, bus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\which, ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| synths[0].set(\which, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \switch).maxWidth_(30);

        controls[1] = NS_Control(\amp, \amp.asSpec)
        .addAction(\synth,{ |c| synths[0].set(\amp, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(30);

        this.makeWindow("Test", Rect(0,0,165,90));

        win.layout_(
            VLayout(
                HLayout( NS_ControlSwitch(controls[0], ["sine","pink"], 2), assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]),                      assignButtons[1] ),
                HLayout( NS_ControlButton(controls[2], ["▶","bypass"]),     assignButtons[2] )
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Switch(2, 2),
            OSC_Fader(false),
            OSC_Button()
        ],randCol: true).oscString("Test")
    }
}
