NS_SwellFB : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(8);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_swellFB" ++ numChans).asSymbol,
            {
                var in = In.ar(\bus.kr, numChans);
                var coef = \coef.kr(1);
                var thresh = \thresh.kr(0.5);
                var sig = in.sum *
                numChans.reciprocal.sqrt * 
                Env.sine(\dur.kr(0.1)).ar(gate: \trig.tr);
                var pan = Demand.kr(\trig.tr(), 0, Dwhite(-1.0, 1.0));
                sig = sig + LocalIn.ar(1);
                sig = DelayC.ar(sig,0.1, \delay.kr(0.03));
                sig = sig * (1 - Trig.ar(Amplitude.ar(sig) > thresh, 0.1)).lag(0.01);
                LocalOut.ar(sig * coef);
                sig = LeakDC.ar(HPF.ar(sig, 80));
                //sig = ReplaceBadValues.ar(sig,0,);

                sig = NS_Pan(sig, numChans, pan, numChans/4);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(0));
                sig = (in * \drySig.kr(0)) + sig;
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) ) 
            },
            [\bus, strip.stripBus],
            { |synth|
                synths.add(synth);
                controls[0] = NS_Control(\delay, ControlSpec(1000.reciprocal,0.1,\exp), 0.03)
                .addAction(\synth, { |c| synths[0].set(\delay, c.value) });

                controls[1] = NS_Control(\dur, ControlSpec(0.01,0.1,\exp), 0.1)
                .addAction(\synth, { |c| synths[0].set(\dur, c.value) });

                controls[2] = NS_Control(\coef, ControlSpec(0.95,1.5,\lin), 1)
                .addAction(\synth, { |c| synths[0].set(\coef, c.value) });

                controls[3] = NS_Control(\thresh, ControlSpec(-24,-3,\db), -6)
                .addAction(\synth, { |c| synths[0].set(\thresh, c.value.dbamp) });

                controls[4] = NS_Control(\trig, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth, { |c| synths[0].set(\trig, c.value) });

                controls[5] = NS_Control(\drySig, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth, { |c| synths[0].set(\drySig, c.value) });

                controls[6] = NS_Control(\amp,\db, -18)
                .addAction(\synth,{ |c| synths[0].set(\amp, c.value.dbamp) });

                controls[7] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c|
                    this.gateBool_(c.value);
                    synths[0].set(\thru, c.value)
                });
                
                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("SwellFB", Rect(0,0,210,180));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 0.001),
                NS_ControlFader(controls[1], 0.001),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3], 1),
                NS_ControlButton(controls[4], ["trig","trig"]),
                NS_ControlButton(controls[5], ["unmute thru", "mute thru"]),
                NS_ControlFader(controls[6]),
                NS_ControlButton(controls[7], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageXY(height: "60%"),
            OpenStageFader(),
            OpenStagePanel([
                OpenStageFader(false), 
                OpenStageButton(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("SwellFB")
    }
}
