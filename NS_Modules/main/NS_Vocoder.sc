NS_Vocoder : NS_SynthModule {
    classvar <isSource = false;

    // based on Eli Fieldsteel's Mini Tutorial: 12
    *initClass {
        ServerBoot.add{
            SynthDef(\ns_vocoder,{
                var numChans = NSFW.numChans;
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
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("Vocoder", Rect(0,0,210,150));

        synths.add( Synth(\ns_vocoder,[\bus,bus],modGroup) );


        controls[0] = NS_Control(\port, ControlSpec(0,0.5,\lin))
        .addAction(\synth,{ |c| synths[0].set(\port, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\octave, ControlSpec(0,4,\lin,1), 2)
        .addAction(\synth,{ |c| synths[0].set(\octave, [0.25,0.5,1,2,4].at(c.value)) });
        assignButtons[1] = NS_AssignButton(this, 1, \switch).maxWidth_(30);
        
        controls[2] = NS_Control(\rq, ControlSpec(0.01,1,\exp), 2 ** (-1/6))
        .addAction(\synth,{ |c| synths[0].set(\rq, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);
       
        controls[3] = NS_Control(\trim, ControlSpec(-9,9,\db), 0)
        .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\mix, ControlSpec(0,1,\lin), 1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]), assignButtons[0] ),
                HLayout( NS_ControlSwitch(controls[1], ["16vb","8vb","nat","8va","16va"],5), assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]).round_(0.001), assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]), assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4]), assignButtons[4] ),
                HLayout( NS_ControlButton(controls[5], ["▶","bypass"]), assignButtons[5] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(horizontal:true),
            OSC_Switch(columns: 5,numPads:5),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal:true),
                OSC_Button(width:"20%")
            ])
        ],randCol: true).oscString("Vocoder")
    }
}
