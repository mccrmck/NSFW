NS_Decimator : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;
        var sRate    = nsServer.options.sampleRate;

        this.initModuleArrays(4);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_decimator" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);

                sig = Decimator.ar(sig,\sRate.kr(sRate),\bits.kr(10));
                sig = LeakDC.ar(sig);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\sRate,ControlSpec(80, sRate, \exp), sRate)
        .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) });

        controls[1] = NS_Control(\bits,ControlSpec(1,10,\lin),10)
        .addAction(\synth,{ |c| synths[0].set(\bits, c.value) });

        controls[2] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

        controls[3] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

        this.makeWindow("Decimator", Rect(0,0,180,90));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 1),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlButton(controls[3], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(width: "85%"),
            OSC_Panel([OSC_Fader(false, false), OSC_Button(height: "20%")])
        ], columns: 2, randCol: true).oscString("Decimator")
    }
}
