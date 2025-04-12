NS_GrainDelay : NS_SynthModule {
    classvar <isSource = false;

    /* SynthDef based on the similar SynthDef by PlaymodesStudio */
    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(6);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_grainDelay" ++ numChans).asSymbol,
            {
                var sig         = In.ar(\bus.kr, numChans).sum * numChans.reciprocal.sqrt;
                var sRate       = SampleRate.ir;

                var circularBuf = LocalBuf(sRate * 3, 1).clear;
                var bufFrames   = BufFrames.kr(circularBuf) - 1;
                var writePos    = Phasor.ar(DC.ar(0), 1, 0, bufFrames);
                var rec         = BufWr.ar(sig /*+ LocalIn.ar(numChans)*/, circularBuf, writePos);

                var readPos     = Wrap.ar(writePos - (\dTime.kr(0.1) * sRate), 0, bufFrames);
                var grainDur    = \grainDur.kr(0.25);

                var trig        = Impulse.ar(\tFreq.kr(4));
                var pan         = Demand.ar(trig, 0, Dwhite(-1, 1));
                var durJit      = Demand.ar(trig, 0, Dwhite(1, 1.5));
                var posJit      = Demand.ar(trig, 0, Dwhite(0, grainDur)) * sRate;

                sig = GrainBuf.ar(
                    numChans,
                    trig,
                    grainDur * durJit, 
                    circularBuf <! rec, 
                    \rate.kr(1),
                    (readPos - posJit) / bufFrames,
                    pan: pan
                );

                // LocalOut.ar(sig * \coef.kr(0.9));

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\grainDur,ControlSpec(0.01,1,\exp),0.25)
        .addAction(\synth,{ |c| synths[0].set(\grainDur, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\tFreq,ControlSpec(2,80,\exp),4)
        .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\dTime,ControlSpec(0.1,1.5,\exp),0.1)
        .addAction(\synth,{ |c| synths[0].set(\dTime, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\rate,ControlSpec(0.5,2,\exp),1)
        .addAction(\synth,{ |c| synths[0].set(\rate, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.boolGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        this.makeWindow("GrainDelay", Rect(0,0,240,150));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]),                  assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]),                  assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]),                  assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]),                  assignButtons[3] ),
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
                OSC_Fader(false,false),
                OSC_Button(height:"20%")
            ], width: "15%")
        ], columns: 3, randCol: true).oscString("Grain Delay")
    }
}
