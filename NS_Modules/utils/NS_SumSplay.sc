NS_SumSplay : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;
        var outChans = nsServer.options.outChannels - 2; // sends a stereo signal

        this.initModuleArrays(2);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_sumpSplay" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                Out.ar(\sendBus.kr(),Splay.ar(sig,1, \sendAmp.kr(0)) * \mute.kr(0) );
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        this.initModuleArrays(3);

        controls[0] = NS_Control(\sendBus,ControlSpec(0,outChans,'lin',1))
        .addAction(\synth,{ |c| synths[0].set(\sendBus, c.value ) });
        assignButtons[0] = NS_AssignButton(this, 0, \switch).maxWidth_(30);

        controls[1] = NS_Control(\amp, \db.asSpec)
        .addAction(\synth,{ |c| synths[0].set(\amp, c.value.dbamp ) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\mute, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(30);
        
        this.makeWindow("SumSplay", Rect(0,0,240,75));
       
        win.layout_(
            VLayout(
                HLayout( NS_ControlMenu(controls[0], (0..outChans)),       assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1], 1),               assignButtons[1] ),
                HLayout( NS_ControlButton(controls[2], ["▶","bypass"]), assignButtons[2] )
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(false, false),
            OSC_Button(height:"20%")
        ],randCol: true).oscString("SumSplay")
    }
}
