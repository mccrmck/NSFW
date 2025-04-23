NS_VarDelay : NS_SynthModule {
    classvar <isSource = true;
    var buffer;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
        buffer = Buffer.allocConsecutive(numChans, server, server.sampleRate);      

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_varDelay" ++ numChans).asSymbol,
            {
                var sig     = In.ar(\bus.kr, numChans);
                var buffer  = \buffer.kr(0 ! numChans);
                var clip    = \clip.kr(1, 0.1);
                var sinFreq = \sinFreq.kr(0.05) * ({ 0.9.rrand(1) } ! numChans);
                var tap     = DelTapWr.ar(buffer, sig + LocalIn.ar(numChans));

                sig = DelTapRd.ar(
                    buffer,
                    tap,
                    \dTime.kr(0.2, 0.05) + SinOsc.ar(sinFreq).range(-0.02, 0),
                    2
                ); 
                sig = sig + PinkNoise.ar(0.0001);
                sig = Clip.ar(sig, clip.neg, clip);

                LocalOut.ar(sig.rotate(1) * \feedB.kr(0.95));

                sig = LeakDC.ar(sig);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
            },
            [\bus, strip.stripBus, \buffer, buffer],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\dtime, ControlSpec(0.01,1,\lin), 0.2)
        .addAction(\synth,{ |c| synths[0].set(\dTime, c.value) });

        controls[1] = NS_Control(\clip, ControlSpec(0.01,1,\lin), 1)
        .addAction(\synth,{ |c| synths[0].set(\clip, c.value) });

        controls[2] = NS_Control(\sinHz, ControlSpec(0.01,40,\exp), 0.05)
        .addAction(\synth,{ |c| synths[0].set(\sinHz, c.value) });

        controls[3] = NS_Control(\feedB, ControlSpec(0.5,1.05,\exp), 0.95)
        .addAction(\synth,{ |c| synths[0].set(\feedB, c.value) });

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin), 0)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

        this.makeWindow("VarDelay",Rect(0,0,240,120));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2)
        ], randCol: true).oscString("VarDelay")
    }
}
