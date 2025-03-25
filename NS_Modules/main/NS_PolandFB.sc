NS_PolandFB : NS_SynthModule {
    classvar <isSource = true;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_polandFB,{
                var numChans = NSFW.numChans;
                var wave     = 40.collect({ |i| (i/40 * 2pi).sin }); // make this into a bus w/ switchable waveforms?
                var fbBuf    = LocalBuf(1);

                var sig;
                var noise = Dwhite(-1, 1) * \noiseAmp.kr(0.05);
                var osc = DemandEnvGen.ar(Dseq(wave,inf),\oscFreq.kr(40).reciprocal / 40,5,0,levelScale: \oscAmp.kr(0.04));
                var in = Dbufrd(fbBuf);

                in = in + noise + osc;
                in = in.wrap2(\wrap.kr(5));
                in = in.round( 2 ** (\bits.kr(24) - 1).neg );

                sig = Dbufwr(in, fbBuf);
                sig = Duty.ar(\sRate.ar(48000).reciprocal, 0, sig);
                //sig = SelectX.ar(\which.kr(0),[sig, sig.sign - sig]);
                sig = sig.fold2(\fold.kr(2));
                sig = LeakDC.ar(sig);
                sig = (sig * 4).clip2 * -15.dbamp;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(9);
        this.makeWindow("PolandFB",Rect(0,0,300,210));

        synths.add( Synth(\ns_polandFB,[\bus,bus],modGroup) );

        controls[0] = NS_Control(\oscAmp, ControlSpec(0,0.5,\amp),0.05)
        .addAction(\synth,{ |c| synths[0].set(\oscAmp, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\noiseAmp, ControlSpec(0,0.5,\amp),0.05)
        .addAction(\synth,{ |c| synths[0].set(\noiseAmp, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\sRate, ControlSpec(2000,48000,\exp),24000)
        .addAction(\synth,{ |c| synths[0].set(\sRate, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\bits, ControlSpec(2,24,\lin),16)
        .addAction(\synth,{ |c| synths[0].set(\bits, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);
       
        controls[4] = NS_Control(\oscFreq, ControlSpec(0.1,250,\exp),40)
        .addAction(\synth,{ |c| synths[0].set(\oscFreq, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\wrap, ControlSpec(0.5,10,\exp),5)
        .addAction(\synth,{ |c| synths[0].set(\wrap, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(30);

        controls[6] = NS_Control(\fold, ControlSpec(0.1,2,\lin),2)
        .addAction(\synth,{ |c| synths[0].set(\fold, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(30);

        controls[7] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[7] = NS_AssignButton(this, 7, \fader).maxWidth_(30);

        controls[8] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[8] = NS_AssignButton(this, 8, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1])                , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3])                , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4])                , assignButtons[4] ),
                HLayout( NS_ControlFader(controls[5])                , assignButtons[5] ),
                HLayout( NS_ControlFader(controls[6])                , assignButtons[6] ),
                HLayout( NS_ControlFader(controls[7])                , assignButtons[7] ),
                HLayout( NS_ControlButton(controls[8],["▶","bypass"]), assignButtons[8] ),
            )          
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {
        ^OSC_Panel([
            OSC_Panel({OSC_XY()} ! 2, columns: 2, height: "50%"),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2),
        ],randCol:true).oscString("PolandFB")

    }
}
