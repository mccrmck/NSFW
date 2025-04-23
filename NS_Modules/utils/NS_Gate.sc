NS_Gate : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(4);
      
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_gate" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var thresh = \thresh.kr(-36);
                var sliceDur = SampleRate.ir * 0.01;
                var gate = FluidAmpGate.ar(
                    sig,10,10,thresh,thresh-5,sliceDur,sliceDur,sliceDur,sliceDur
                );

                gate = LagUD.ar(gate,\atk.kr(0.01),\rls.kr(0.01));
                sig = sig * gate;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
       );

        controls[0] = NS_Control(\thresh,\db.asSpec, -36)
        .addAction(\synth,{ |c| synths[0].set(\thresh, c.value) });

        controls[1] = NS_Control(\atk,ControlSpec(0,0.1,\lin),0.01)
        .addAction(\synth,{ |c| synths[0].set(\atk, c.value) });

        controls[2] = NS_Control(\rls,ControlSpec(0,0.1,\lin),0.1)
        .addAction(\synth,{ |c| synths[0].set(\rls, c.value) });

        controls[3] = NS_Control(\bypass, ControlSpec(0,1,'lin',1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
  
        this.makeWindow("Gate", Rect(0,0,255,90));
        
        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 0.1),
                NS_ControlFader(controls[1], 0.001),
                NS_ControlFader(controls[2], 0.001),
                NS_ControlButton(controls[3],["▶","bypass"]),
            )
        );
    
        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Button()
        ], randCol: true).oscString("Gate")
    }
}
