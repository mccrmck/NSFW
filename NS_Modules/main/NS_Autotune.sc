NS_Autotune : NS_SynthModule {
    classvar <isSource = false;
    var buttons, transpose;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_autotune,{
                var numChans = NSFW.numOutChans;
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;
                var track = Pitch.kr(sig);
                var pitch = 20.max(track[0]);
                var quantMidi = pitch.cpsmidi.round;
                var pitchDif = quantMidi - pitch.cpsmidi;
                sig = SelectX.ar(track[1].lag(0.01),[
                    sig,
                    Mix(PitchShiftPA.ar(sig,pitch, pitchDif.midiratio * \harm.kr([0,1.5,2]).varlag(1,-10), 1))
                ]);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(11);
        this.makeWindow("Autotune", Rect(0,0,298,120));

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

        buttons.pairsDo({ |k, v, i|
        var string = k.asString;
        var index = (i / 2).asInteger;
            controls.add(
                Button()
                .maxWidth_(45)
                .states_([[ string,Color.black,Color.white],[string,Color.white,Color.black]])
                .action_({ |but|
                    if(but.value == 1,{
                        var value;
                        if(index > 2,{
                            value = v.midiratio * transpose.choose
                        },{
                            value = v.midiratio;
                        });
                        synths[0].set(\harm,value)
                    })
                })
            );
            assignButtons[index] = NS_AssignButton(this, index, \button).maxWidth_(45)
        });

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:1)  
        );
        assignButtons[9] = NS_AssignButton(this, 9, \fader).maxWidth_(45);

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
        assignButtons[10] = NS_AssignButton(this, 10, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                GridLayout.columns(
                    controls[0..2],
                    assignButtons[0..2],
                    controls[3..5],
                    assignButtons[3..5],
                    controls[6..8],
                    assignButtons[6..8],
                ),
                HLayout( controls[9], assignButtons[9], controls[10], assignButtons[10]),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 3 ),
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 3 ),
            OSC_Panel(widgetArray: { OSC_Button(mode: 'push') } ! 3 ),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal:true),
                OSC_Button(width:"20%")
            ])
        ],randCol: true).oscString("Autotune")
    }
}
