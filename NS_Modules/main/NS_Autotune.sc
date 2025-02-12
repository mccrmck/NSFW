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
        this.makeWindow("Autotune", Rect(0,0,210,150));

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

        controls[0] = NS_Control(\harm,ControlSpec(0,(buttons.size/2)-1,'lin',1),0)
        .addAction(\synth,{ |c|
            var val = c.value;
            var harm = buttons[val * 2 + 1];
            harm = if( val > 2,{ harm.midiratio * transpose.choose },{ harm.midiratio });
            synths[0].set(\harm,harm)
        });
        assignButtons[0] = NS_AssignButton(this, 0, \switch);

        controls[1] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                NS_ControlSwitch(controls[0],buttons[0,2..],3), assignButtons[0],
                HLayout( NS_ControlFader(controls[1]).round_(0.01), assignButtons[1] ),
                HLayout( NS_ControlButton(controls[2],["▶","bypass"]), assignButtons[2] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel([
           OSC_Switch(9, 3), 
            OSC_Panel([OSC_Fader(false), OSC_Button(width:"20%")], columns: 2, height: "20%")
        ],randCol: true).oscString("Autotune")
    }
}
