NS_MonoSumSend : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;
        var outChans = nsServer.options.outChannels - 1; // sends a mono signal

        this.initModuleArrays(5);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_monoSumSend" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var sum = sig.sum * numChans.reciprocal;
                var coFreq = \coFreq.kr(80,0.1);

                sum = SelectX.ar(\which.kr(0),[sum, LPF.ar(LPF.ar(sum,coFreq),coFreq)]);
                
                sum = NS_Envs(sum, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                Out.ar(\sendBus.kr,sum * \sendAmp.kr(0) * \mute.kr(0));
                ReplaceOut.ar(\bus.kr, sig)
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\lpf, ControlSpec(20,120,\exp), 80)
        .addAction(\synth, { |c| synths[0].set(\coFreq, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\filter, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| synths[0].set(\which, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(30);
        
        controls[2] = NS_Control(\outBus,ControlSpec(0,outChans,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\sendBus, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \switch).maxWidth_(30);

        controls[3] = NS_Control(\amp, ControlSpec(0,1,\lin), 0)
        .addAction(\synth, { |c| synths[0].set(\sendAmp, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\mute, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(30);
         
        this.makeWindow("MonoSumSend", Rect(0,0,300,90));        

        win.layout_(
            VLayout(
                HLayout( 
                    NS_ControlFader(controls[0], 1),                               assignButtons[0], 
                    NS_ControlButton(controls[1], ["LPF","noFilt"]).maxWidth_(60), assignButtons[1]
                ),
                HLayout(
                    NS_ControlMenu(controls[2], (0..outChans) ),                   assignButtons[2],
                    NS_ControlButton(controls[4], ["▶", "bypass"]).maxWidth_(60),  assignButtons[4]
                ),
                HLayout( NS_ControlFader(controls[3]), assignButtons[3] ),
            )
        );
     
        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Button(),
            OSC_Fader(false),
            OSC_Button()
        ],randCol: true).oscString("MonoSumSend")
    }
}
