NS_PadSynth : NS_SynthModule {
    classvar <isSource = true;
    var root, chord;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_padSynth,{
                var numChans = NSFW.numOutChans;
                var freq = \freq.kr([36,43,52].midicps);
                var fFreq = LFNoise2.kr(0.3).range(1000,3000);
                var width = LFNoise2.kr(0.4).range(0.1,0.4);
                //var sig = VarSaw.ar(freq * LFNoise2.kr(0.1!3).range(-0.1,0.1).midiratio,width: width,mul:\gain.kr(0.2).lag(0.1)).fold2.sum;
                var sig = Pulse.ar(freq.lag(0.1) * LFNoise2.kr(0.1!3).range(-0.1,0.1).midiratio,width: width,mul: \gain.kr(0.2).lag(0.1)).fold2.sum;
                sig = sig + PinkNoise.ar(\noiseAmp.kr(0));
                sig = sig * -12.dbamp;

                sig = RLPF.ar(sig.tanh,fFreq,\rq.kr(0.5));
                sig = sig + CombC.ar(sig, 0.4,LFNoise2.kr(0.1).range(0.2,0.4),2,0.5);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(7);
        this.makeWindow("PadSynth", Rect(0,0,240,300));

        synths.add( Synth(\ns_padSynth,[\bus,bus],modGroup) );

        root  = 36;
        chord = [0,7,16];

        controls.add(
            NS_Switch(["C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"],{ |switch| 
                var notes = (36..47);
                root = notes[switch.value];
                synths[0].set(\freq, (chord + root).midicps * ([1] ++ ({ [0.5,1,2 ].choose }!2)) )
            })
        );
        assignButtons[0] = NS_AssignButton(this, 0, \switch).maxWidth_(45);

        controls.add(
            NS_Switch(["maj","min"],{ |switch| 
                var chords = [ [0,19,28], [0,19,27] ];
                chord = chords[switch.value];
                synths[0].set( \freq, (chord + root).midicps * ([1] ++ ({ [0.5,1,2 ].choose }!2)) )
            })
        );
        assignButtons[1] = NS_AssignButton(this, 1, \switch).maxWidth_(45);

        controls.add(
            NS_Fader("rq",ControlSpec(0.1,1.0,\exp),{ |f| synths[0].set(\rq, f.value) },initVal: 0.5)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("noiz",\amp,{ |f| synths[0].set(\noiseAmp, f.value) },initVal: 0)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("gain",\amp,{ |f| synths[0].set(\gain, f.value) },initVal:0.2)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1)  
        );
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(45);

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
        assignButtons[6] = NS_AssignButton(this, 6, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout( controls[0], assignButtons[0] ),
                VLayout( controls[1], assignButtons[1], controls[2], assignButtons[2] ),
                VLayout( controls[3], assignButtons[3] ),
                VLayout( controls[4], assignButtons[4] ),
                VLayout( controls[5], assignButtons[5], controls[6], assignButtons[6] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Switch(mode: 'slide',numPads:12),
            OSC_Switch(mode: 'slide',numPads:2),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal:true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("PadSynth")
    }
}
