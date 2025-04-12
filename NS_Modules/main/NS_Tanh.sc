NS_Tanh : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(12);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_tanh" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);

                sig = BHiShelf.ar(sig, \preHiFreq.kr(8000), 1, \preHidB.kr(0));
                sig = BLowShelf.ar(sig, \preLoFreq.kr(200), 1, \preLodB.kr(0));
                sig = (sig * \gain.kr(1)).tanh;
                sig = BLowShelf.ar(sig, \postLoFreq.kr(200), 1, \postLodB.kr(0));
                sig = BHiShelf.ar(sig, \postHiFreq.kr(8000), 1, \postHidB.kr(0));

                sig = LeakDC.ar(sig);
                sig = sig * \trim.kr(1);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\preLoHz,ControlSpec(20,2000,\exp),200)
        .addAction(\synth,{ |c| synths[0].set(\preLoFreq, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\preLodB,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\preLodB, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \knob);

        controls[2] = NS_Control(\preHiHz,ControlSpec(2000,10000,\exp),8000)
        .addAction(\synth,{ |c| synths[0].set(\preHiFreq, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\preHidB,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\preHidB, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \knob);

        controls[4] = NS_Control(\postLoHz,ControlSpec(20,2000,\exp),200)
        .addAction(\synth,{ |c| synths[0].set(\postLoFreq, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\postLodB,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\postLodB, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \knob);

        controls[6] = NS_Control(\postHiHz,ControlSpec(2500,10000,\exp),8000)
        .addAction(\synth,{ |c| synths[0].set(\postHiFreq, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(30);

        controls[7] = NS_Control(\postHidB,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\postHidB, c.value) });
        assignButtons[7] = NS_AssignButton(this, 7, \knob);

        controls[8] = NS_Control(\gain,ControlSpec(0,32,\db),0)
        .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) });
        assignButtons[8] = NS_AssignButton(this, 8, \fader).maxWidth_(30);

        controls[9] = NS_Control(\trim,\db,0)
        .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });
        assignButtons[9] = NS_AssignButton(this, 9, \fader).maxWidth_(30);

        controls[10] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[10] = NS_AssignButton(this, 10, \fader).maxWidth_(30);

        controls[11] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[11] = NS_AssignButton(this, 11, \button).maxWidth_(30);

        this.makeWindow("Tanh", Rect(0,0,270,300));

        win.layout_(
            VLayout(
                VLayout( 
                    *4.collect({ |i| 
                        HLayout( 
                            NS_ControlFader(controls[i * 2], 1), 
                            assignButtons[i * 2]
                        )
                    })
                ),
                HLayout( 
                    *4.collect({ |i| 
                        VLayout( 
                            NS_ControlKnob(controls[i * 2 + 1]), 
                            assignButtons[i * 2 +1]
                        )
                    })
                ),
                HLayout( NS_ControlFader(controls[8]),                    assignButtons[8] ),
                HLayout( NS_ControlFader(controls[9]),                    assignButtons[9] ),
                HLayout( NS_ControlFader(controls[10]),                   assignButtons[10] ),
                HLayout( NS_ControlButton(controls[11], ["▶", "bypass"]), assignButtons[11] ),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Panel({OSC_Knob()} ! 4, columns: 4),
            OSC_Panel({OSC_Knob()} ! 4, columns: 4),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([
                OSC_Fader(false), 
                OSC_Button(width: "20%")
            ], columns: 2, height: "20%")
        ], randCol: true).oscString("Tanh")
    }
}
