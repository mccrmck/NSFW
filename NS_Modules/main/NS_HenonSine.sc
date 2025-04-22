NS_HenonSine : NS_SynthModule {
    classvar <isSource = true;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_henonSine" ++ numChans).asSymbol,
            {
                var freqRate = \fRate.kr(0.1);
                var noise = \noise.kr(0.5);
                var spread = \spread.kr(0.5);
                var freq = HenonL.ar(freqRate, noise, spread).clip2;
                var sig = SinOsc.ar(freq.linexp(-1,1,80,3500));
                sig = (sig * \gain.kr(1)).fold2;
                sig = sig * -18.dbamp;
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\fRate,ControlSpec(0,250,4),0.1)
        .addAction(\synth,{ |c| synths[0].set(\fRate, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\noise,ControlSpec(1.1,1.4,\lin),0.1)
        .addAction(\synth,{ |c| synths[0].set(\noise, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);   

        controls[2] = NS_Control(\gain,ControlSpec(1,8,\exp),1)
        .addAction(\synth,{ |c| synths[0].set(\gain, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\spread,ControlSpec(0,0.3,\lin),0.1)
        .addAction(\synth,{ |c| synths[0].set(\spread, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);
         
        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        this.makeWindow("HenonSine", Rect(0,0,240,150));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0], 0.1),             assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1], 0.001),           assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]),                  assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3], 0.001),           assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4]),                  assignButtons[4] ),
                HLayout( NS_ControlButton(controls[5], ["▶","bypass"]), assignButtons[5] ),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_XY(),
            OSC_XY(),
            OSC_Panel([
                OSC_Fader(false, false), 
                OSC_Button(height:"20%")
            ], width: "15%")
        ], columns: 3, randCol: true).oscString("HenonSine")
    }
}
