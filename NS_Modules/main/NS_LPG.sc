NS_LPG : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(7);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_lpg" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var sigSum = sig.sum * numChans.reciprocal.sqrt * \gainOffset.kr(1);
                var amp = Amplitude.ar(sigSum, \atk.kr(0.1), \rls.kr(0.1));
                var rq = \rq.kr(0.707);

                sig = Select.ar(\which.kr(0),[
                    BLowPass.ar(sig, amp.linexp(0,1,20,20000), rq),
                    BHiPass.ar(sig,  amp.linexp(0,1,20,20000), rq),
                    BLowPass.ar(sig, amp.linexp(0,1,20000,20), rq),
                    BHiPass.ar(sig,  amp.linexp(0,1,20000,20), rq),
                ]);

                sig = LeakDC.ar(sig.tanh);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\trim,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\gainOffset, c.value.dbamp) });

        controls[1] = NS_Control(\atk,ControlSpec(0.001,0.1,\lin),0.1)
        .addAction(\synth,{ |c| synths[0].set(\atk, c.value) });

        controls[2] = NS_Control(\rls,ControlSpec(0.001,0.1,\lin),0.1)
        .addAction(\synth,{ |c| synths[0].set(\rls, c.value) });

        controls[3] = NS_Control(\filt,ControlSpec(0,3,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\which, c.value) });

        controls[4] = NS_Control(\rq,ControlSpec(1,0.01,-2), 2.sqrt.reciprocal)
        .addAction(\synth,{ |c| synths[0].set(\rq, c.value) });

        controls[5] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

        controls[6] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

        this.makeWindow("LPG", Rect(0,0,240,180));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1], 0.001),
                NS_ControlFader(controls[2], 0.001),
                NS_ControlSwitch(controls[3], ["LPG","HPG","ILPG","IHPG"], 4),
                NS_ControlFader(controls[4], 0.001),
                NS_ControlFader(controls[5]),
                NS_ControlButton(controls[6], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {
        ^OSC_Panel([
            OSC_Panel([
                OSC_XY(width: "75%"), 
                OSC_Switch(4)
            ], columns: 2, height: "50%"),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([
                OSC_Fader(false), 
                OSC_Button(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("LPG")
    }
}

