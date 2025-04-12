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
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\freq2,ControlSpec(0.1,14000,\exp),4)
        .addAction(\synth,{ |c| synths[0].set(\freq2, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\filtFreq,ControlSpec(20,20000,\exp),250)
        .addAction(\synth,{ |c| synths[0].set(\filtFreq, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\rq,ControlSpec(1,0.01,\exp),0.5)
        .addAction(\synth,{ |c| synths[0].set(\rq, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\rungler1,ControlSpec(0,1,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\rungler1, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\rungler2,ControlSpec(0,1,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\rungler2, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(30);

        controls[6] = NS_Control(\runglerFilt,ControlSpec(0,10,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\runglerFilt, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(30);

        controls[7] = NS_Control(\gain,ControlSpec(0,18,\db),0)
        .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) });
        assignButtons[7] = NS_AssignButton(this, 7, \fader).maxWidth_(30);

        controls[8] = NS_Control(\whichSig,ControlSpec(0,5,\lin,1),5)
        .addAction(\synth,{ |c| synths[0].set(\whichSig, c.value) });
        assignButtons[8] = NS_AssignButton(this, 8, \switch).maxWidth_(30);

        controls[9] = NS_Control(\whichFilt,ControlSpec(0,4,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\whichFilt, c.value) });
        assignButtons[9] = NS_AssignButton(this, 9, \switch).maxWidth_(30);

        controls[10] = NS_Control(\loop,ControlSpec(0,1,\lin),0)
        .addAction(\synth,{ |c| synths[0].set(\loop, c.value) });
        assignButtons[10] = NS_AssignButton(this, 10, \fader).maxWidth_(30);

        controls[11] = NS_Control(\scale,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\scale, c.value) });
        assignButtons[11] = NS_AssignButton(this, 11, \fader).maxWidth_(30);

        controls[12] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[12] = NS_AssignButton(this, 12, \fader).maxWidth_(30);

        controls[13] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[13] = NS_AssignButton(this, 13, \button).maxWidth_(30);

        this.makeWindow("Benjolin",Rect(0,0,270,330));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0], 1), assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1], 1), assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2], 1), assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]), assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4]), assignButtons[4] ),
                HLayout( NS_ControlFader(controls[5]), assignButtons[5] ),
                HLayout( NS_ControlFader(controls[6]), assignButtons[6] ),
                HLayout( NS_ControlFader(controls[7]), assignButtons[7] ),
                HLayout( NS_ControlSwitch(controls[8], ["tri1", "tri2", "osc1", "osc2", "pwm", "sh0"],6), assignButtons[8]),
                HLayout( NS_ControlSwitch(controls[9], ["rlpf", "moog", "rhpf", "svf", "dfm1"],5), assignButtons[9]),
                HLayout( NS_ControlFader(controls[10]), assignButtons[10] ),
                HLayout( NS_ControlFader(controls[11]), assignButtons[11] ),
                HLayout( NS_ControlFader(controls[12]), assignButtons[12] ),
                HLayout( NS_ControlButton(controls[13], ["▶","bypass"]), assignButtons[13] ),
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
