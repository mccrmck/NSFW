NS_Benjolin : NS_SynthModule {
    classvar <isSource = true;

    /* SynthDef based on the work of Alejandro Olarte, inspired by Rob Hordijk's Benjolin */

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(14);

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_benjolin" ++ numChans).asSymbol,
            {
                var sh0, sh1, sh2, sh3, sh4, sh5, sh6, sh7, sh8 = 1, sig;

                var sr = SampleDur.ir;
                var local = LocalIn.ar(2,0);
                var rungler = local[0];
                var buf = local[1];

                var loop = \loop.kr(0);
                var freq1 = \freq1.kr(40);
                var freq2 = \freq2.kr(4);
                var rungler1 = \rungler1.kr(0.5);
                var rungler2 = \rungler2.kr(0.5);

                var runglerFilt = \runglerFilt.kr(0.5);
                var filtFreq = \filtFreq.kr(250);
                var rq = \rq.kr(0.5);
                var gain = \gain.kr(1);
                var tri1 = LFTri.ar((rungler * rungler1) + freq1);
                var tri2 = LFTri.ar((rungler * rungler2) + freq2);
                var osc1 = PulseDPW.ar((rungler * rungler1) + freq1);
                var osc2 = PulseDPW.ar((rungler * rungler2) + freq2);

                var pwm = BinaryOpUGen('>', (tri1 + tri2), 0); // pwm = tri1 > tri2;

                osc1 = ( (buf * loop) + (osc1 * (loop * -1 + 1)) );  // loop spits out nans sometimes
                sh0 = BinaryOpUGen('>', osc1, 0.5);
                sh0 = BinaryOpUGen('==', (sh8 > sh0), (sh8 < sh0));
                sh0 = (sh0 * -1) + 1;

                // this can probably be cleaned up with some clever syntax, no?
                sh1 = DelayN.ar(Latch.ar(sh0, osc2), 0.01, sr);
                sh2 = DelayN.ar(Latch.ar(sh1, osc2), 0.01, sr * 2);
                sh3 = DelayN.ar(Latch.ar(sh2, osc2), 0.01, sr * 3);
                sh4 = DelayN.ar(Latch.ar(sh3, osc2), 0.01, sr * 4);
                sh5 = DelayN.ar(Latch.ar(sh4, osc2), 0.01, sr * 5);
                sh6 = DelayN.ar(Latch.ar(sh5, osc2), 0.01, sr * 6);
                sh7 = DelayN.ar(Latch.ar(sh6, osc2), 0.01, sr * 7);
                sh8 = DelayN.ar(Latch.ar(sh7, osc2), 0.01, sr * 8);

                //rungler = ((sh6/8)+(sh7/4)+(sh8/2)); //original circuit
                //rungler = ((sh5/16)+(sh6/8)+(sh7/4)+(sh8/2));

                rungler = (
                    (sh1/2.pow(8)) + (sh2/2.pow(7)) + (sh3/2.pow(6)) + 
                    (sh4/2.pow(5)) + (sh5/2.pow(4)) + (sh6/2.pow(3)) + 
                    (sh7/2.pow(2)) + (sh8/2.pow(1))
                );

                buf     = rungler;
                rungler = (rungler * \scale.kr(1).linlin(0,1,0,127));
                rungler = rungler.midicps;

                LocalOut.ar([rungler,buf]);

                sig = SelectX.ar(\whichSig.kr(5), [tri1, tri2, osc1, osc2, pwm, sh0]);

                sig = LeakDC.ar(sig);

                sig = SelectX.ar(\whichFilt.kr(0), [
                    RLPF.ar(sig, (rungler*runglerFilt)+filtFreq, rq, gain),
                    BMoog.ar(sig,(rungler*runglerFilt)+filtFreq, 1 - rq, 0, gain),
                    RHPF.ar(sig, (rungler*runglerFilt)+filtFreq, rq, gain),
                    SVF.ar( sig, (rungler*runglerFilt)+filtFreq, 1 - rq,1,0,0,0,0,gain),
                    DFM1.ar(sig, (rungler*runglerFilt)+filtFreq, 1 - rq ,gain,1)
                ]);

                sig = sig * -15.dbamp;

                sig = NS_Envs(sig.tanh, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );
        
        controls[0] = NS_Control(\freq1,ControlSpec(20,20000,\exp),40)
        .addAction(\synth,{ |c| synths[0].set(\freq1, c.value) });

        controls[1] = NS_Control(\freq2,ControlSpec(0.1,14000,\exp),4)
        .addAction(\synth,{ |c| synths[0].set(\freq2, c.value) });

        controls[2] = NS_Control(\filtFreq,ControlSpec(20,20000,\exp),250)
        .addAction(\synth,{ |c| synths[0].set(\filtFreq, c.value) });

        controls[3] = NS_Control(\rq,ControlSpec(1,0.01,-2),0.5)
        .addAction(\synth,{ |c| synths[0].set(\rq, c.value) });

        controls[4] = NS_Control(\rungler1,ControlSpec(0,1,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\rungler1, c.value) });

        controls[5] = NS_Control(\rungler2,ControlSpec(0,1,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\rungler2, c.value) });

        controls[6] = NS_Control(\runglerFilt,ControlSpec(0,10,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\runglerFilt, c.value) });

        controls[7] = NS_Control(\gain,ControlSpec(0,18,\db),0)
        .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) });

        controls[8] = NS_Control(\whichSig,ControlSpec(0,5,\lin,1),5)
        .addAction(\synth,{ |c| synths[0].set(\whichSig, c.value) });

        controls[9] = NS_Control(\whichFilt,ControlSpec(0,4,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\whichFilt, c.value) });

        controls[10] = NS_Control(\loop,ControlSpec(0,1,\lin),0)
        .addAction(\synth,{ |c| synths[0].set(\loop, c.value) });

        controls[11] = NS_Control(\scale,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\scale, c.value) });

        controls[12] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

        controls[13] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

        this.makeWindow("Benjolin",Rect(0,0,270,330));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0], 1),
                NS_ControlFader(controls[1], 1),
                NS_ControlFader(controls[2], 1),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlFader(controls[5]),
                NS_ControlFader(controls[6]),
                NS_ControlFader(controls[7]),
                NS_ControlSwitch(controls[8], ["tri1", "tri2", "osc1", "osc2", "pwm", "sh0"],6),
                NS_ControlSwitch(controls[9], ["rlpf", "moog", "rhpf", "svf", "dfm1"],5),
                NS_ControlFader(controls[10]),
                NS_ControlFader(controls[11]),
                NS_ControlFader(controls[12]),
                NS_ControlButton(controls[13], ["▶","bypass"]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {
        ^OSC_Panel([
            OSC_XY(),
            OSC_XY(),
            OSC_Panel([OSC_Switch(6, 1), OSC_Switch(5, 1)],columns: 2),
            OSC_XY(),
            OSC_XY(),
            OSC_Panel({OSC_Knob(false)} ! 3 ++ [OSC_Button()])
        ],columns: 3, randCol:true).oscString("Benjolin")
    }
}
