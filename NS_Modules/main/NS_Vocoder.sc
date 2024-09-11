NS_Vocoder : NS_SynthModule {
    classvar <isSource = false;

    // based on Eli Fieldsteel's Mini Tutorial: 12
    *initClass {
        ServerBoot.add{
            SynthDef(\ns_vocoder,{
                var numChans = NSFW.numOutChans;
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
        this.makeWindow("Vocoder", Rect(0,0,270,150));

        synths.add( Synth(\ns_vocoder,[\bus,bus],modGroup) );

        controls.add(
            NS_Fader("port",ControlSpec(0.0,0.5,\lin),{ |f| synths[0].set(\port,f.value)},'horz',0.01 )
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Switch(["16vb","8vb","nat","8va","16va"],{ |switch| synths[0].set(\octave,[0.25,0.5,1,2,4].at(switch.value))},'horz')
        );
        assignButtons[1] = NS_AssignButton(this,1,\switch).maxWidth_(45);

        controls.add(
            NS_Fader("rq",ControlSpec(0.01,1,\exp),{ |f| synths[0].set(\rq,f.value)},'horz',2 ** (-1/6) ).round_(0.001)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("trim",ControlSpec(-9,9,\db),{ |f| synths[0].set(\trim, f.value.dbamp) },'horz',initVal:0)  
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)  
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })       
        );
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3] ),
                HLayout( controls[4], assignButtons[4],controls[5], assignButtons[5] ),
            )
        );

        controls[1].valueAction_(2);
        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(horizontal:true),
            OSC_Switch(numPads:5),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal:true),
                OSC_Button(width:"20%")
            ])
        ],randCol: true).oscString("Vocoder")
    }
}
