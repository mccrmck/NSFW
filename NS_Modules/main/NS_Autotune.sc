NS_Autotune : NS_SynthModule {
    classvar <isSource = false;
    var buttons, transpose;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_autotune,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;
                var track = Pitch.kr(sig);
                var pitch = 20.max(track[0]);
                var quantMidi = pitch.cpsmidi.round;
                var pitchDif = quantMidi - pitch.cpsmidi;
                sig = SelectX.ar(track[1].lag(0.01),[
                    sig,
                    Mix(PitchShiftPA.ar(sig, pitch, pitchDif.midiratio * \harm.kr([0,1.5,2]).varlag(1,-10), 1))
                ]);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(3);
        this.makeWindow("Autotune", Rect(0,0,270,135));

        synths.add( Synth(\ns_autotune,[\bus,bus],modGroup) );

        buttons = [
            '5U'  , [0,7,12],
            '5D'  , [0,-5,-12],
            '5UD' , [0,7,-5],

            'maj0', [0,4,7],
            'maj3', [0,3,8],
            'maj5', [0,5,9],

            'min0', [0,3,7],
            'min3', [0,4,9],
            'min5', [0,5,8],
        ];

        transpose = [[1],[0.5,1],[1,0.5]].allTuples;

        controls.add(
            NS_Switch(buttons[0,2..],{ |switch| 
                var val = switch.value;
                var harm = buttons[val * 2 + 1];
                if(val > 2,{
                    harm = harm.midiratio * transpose.choose
                },{
                    harm = harm.midiratio;
                });

                synths[0].set(\harm,harm)
            },3)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \switch);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)  
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

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
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                controls[0], assignButtons[0],
                HLayout( controls[1], assignButtons[1], controls[2], assignButtons[2] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
           OSC_Switch(columns: 3, mode: 'slide', numPads: 9), 
            OSC_Panel(height: "20%",widgetArray: [
                OSC_Fader(horizontal:true),
                OSC_Button(width:"20%")
            ])
        ],randCol: true).oscString("Autotune")
    }
}
