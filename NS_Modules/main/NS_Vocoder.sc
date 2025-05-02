NS_Vocoder : NS_SynthModule {

    // based on Eli Fieldsteel's Mini Tutorial: 12
    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_vocoder" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;
                var gate = Amplitude.ar(sig,0.01,0.1) > -60.dbamp;

                var numBands = 30;
                var bpfhz = (1..numBands).linexp(1, numBands, 100, 8000);
                var rq = \rq.kr(2 ** (-1/6));
                var bpfmod = BPF.ar(sig, bpfhz, rq, rq.reciprocal.sqrt);
                var track = Amplitude.ar(bpfmod,0.01,0.1).tanh;
                var pitch = FluidPitch.kr(sig,[\pitch]);

                var car = SawDPW.ar(\octave.kr(1) * 20.max(pitch).lag(\port.kr(0.01))).tanh;
                car = SelectX.ar(pitch > 5000, [car, PinkNoise.ar]);
                sig = BPF.ar(car, bpfhz, rq, rq.reciprocal.sqrt).tanh * track * gate;
                sig = LeakDC.ar(sig.sum);

                sig = (sig * \trim.kr(1)).tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth|
                synths.add(synth);
        
                controls[0] = NS_Control(\port, ControlSpec(0,0.5,\lin))
                .addAction(\synth,{ |c| synths[0].set(\port, c.value) });

                controls[1] = NS_Control(\octave, ControlSpec(0,4,\lin,1), 2)
                .addAction(\synth,{ |c| synths[0].set(\octave, [0.25,0.5,1,2,4].at(c.value)) });

                controls[2] = NS_Control(\rq, ControlSpec(0.01,1,\exp), 2 ** (-1/6))
                .addAction(\synth,{ |c| synths[0].set(\rq, c.value) });

                controls[3] = NS_Control(\trim, ControlSpec(-9,9,\db), 0)
                .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });

                controls[4] = NS_Control(\mix, ControlSpec(0,1,\lin), 1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("Vocoder", Rect(0,0,210,150));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlSwitch(controls[1], ["16vb", "8vb", "nat", "8va", "16va"], 5),
                NS_ControlFader(controls[2], 0.001),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlButton(controls[5], ["▶", "bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {  
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Switch(5, 5),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2)
        ], randCol: true).oscString("Vocoder")
    }
}
