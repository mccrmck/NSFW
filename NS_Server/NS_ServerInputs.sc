NS_ServerInput : NS_ControlModule {

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_serverInput,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                sig = sig * \thru.kr(0);
                ReplaceOut.ar(\outBus.kr,sig);
            }).add;

            SynthDef(\ns_serverInputFader,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0,0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));

                ReplaceOut.ar(\bus.kr, sig)
            }).add;

            SynthDef(\ns_serverInputSend,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                Out.ar(\outBus.kr,sig);
            }).add
        }
    }

    *new {
        ^super.new.init
    }

    init {
        this.initControlArrays(11);

        //synths.add( Synth) // put this on a control somewhere...

        controls[0] = NS_Control(\hpf,ControlSpec(20,320,\exp),40)
        .addAction(\synth,{ |c| /*inSynth.set(\hpFreq, c.value) */ });
        assignButtons[0] = NS_AssignButton(this, 0, \knob);

        controls[1] = NS_Control(\gate,ControlSpec(-72,-32,\db),-72)
        .addAction(\synth,{ |c| /* inSynth.set(\gateThresh, c.value)*/ });
        assignButtons[1] = NS_AssignButton(this, 1, \knob);

        controls[2] = NS_Control(\gain,\boostcut)
        .addAction(\synth,{ |c| /* fader.set(\gain, c.value.dbamp)*/ });
        assignButtons[2] = NS_AssignButton(this, 2, \knob);

        controls[3] = NS_Control(\comp,\db,-12)
        .addAction(\synth,{ |c| /*fader.set(\compThresh, c.value)*/ });
        assignButtons[3] = NS_AssignButton(this, 3, \knob);

        controls[4] = NS_Control(\atk,ControlSpec(0.001,0.25),0.01)
        .addAction(\synth,{ |c| /*fader.set(\atk, c.value)*/ });
        assignButtons[4] = NS_AssignButton(this, 4, \knob);

        controls[5] = NS_Control(\rls,ControlSpec(0.001,0.25),0.1)
        .addAction(\synth,{ |c| /*fader.set(\rls, c.value)*/ });
        assignButtons[5] = NS_AssignButton(this, 5, \knob);

        controls[6] = NS_Control(\ratio,ControlSpec(1,20),4)
        .addAction(\synth,{ |c| /*fader.set(\ratio, c.value)*/ });
        assignButtons[6] = NS_AssignButton(this, 6, \knob);

        controls[7] = NS_Control(\knee,ControlSpec(0,1),0.01)
        .addAction(\synth,{ |c| /*fader.set(\knee, c.value)*/ });
        assignButtons[7] = NS_AssignButton(this, 7, \knob);

        controls[8] = NS_Control(\mUpGain,\boostcut)
        .addAction(\synth,{ |c| /*fader.set(\muGain, c.value)*/ });
        assignButtons[8] = NS_AssignButton(this, 8, \knob);

        controls[9] = NS_Control(\amp,\db)
        .addAction(\synth,{ |c| /*fader.set(\amp, c.value.dbamp)*/ });
        assignButtons[9] = NS_AssignButton(this, 9, \fader);

        controls[10] = NS_Control(\mute,ControlSpec(0,1,'lin',1))
        .addAction(\synth,{ |c| /* fader.set(\mute, c.value)*/ });
        assignButtons[10] = NS_AssignButton(this, 10, \button);
    }

}

NS_ServerInputView {
    var <view;

    *new { |serverInput|
        ^super.new.init(serverInput)
    }

    init { |input|

        view = View().layout_(
            VLayout(
                GridLayout.rows(
                    [
                        // hpf
                        NS_ControlKnob(input.controls[0]).stringColor_(NS_Style.textLight), 
                        // gate
                        NS_ControlKnob(input.controls[1]).stringColor_(NS_Style.textLight),
                    ],
                    [[
                        Button().states_([
                            ["EQ", NS_Style.playGreen, NS_Style.bGroundDark],
                            ["EQ", NS_Style.bGroundDark, NS_Style.playGreen]
                        ]),
                        columns: 2
                    ]],
                    [
                        // gate
                        NS_ControlKnob(input.controls[2]).stringColor_(NS_Style.textLight),
                        // compThresh
                        NS_ControlKnob(input.controls[3]).stringColor_(NS_Style.textLight),
                    ],
                    [
                        // atk
                        NS_ControlKnob(input.controls[4]).stringColor_(NS_Style.textLight),
                        // rls
                        NS_ControlKnob(input.controls[5]).stringColor_(NS_Style.textLight)
                    ], 
                    [
                        // ratio
                        NS_ControlKnob(input.controls[6]).stringColor_(NS_Style.textLight),
                        // knee
                        NS_ControlKnob(input.controls[7]).stringColor_(NS_Style.textLight)
                    ]
                ),
                // mUpGain
                NS_ControlKnob(input.controls[8]).stringColor_(NS_Style.textLight),
                // amp
                NS_ControlFader(input.controls[9], 'vert').stringColor_(NS_Style.textLight),
                input.assignButtons[9],
                // mute
                NS_ControlButton(input.controls[10],[
                    ["M", NS_Style.bGroundDark, NS_Style.muteRed],
                    [NS_Style.play, NS_Style.playGreen, NS_Style.bGroundDark]
                ]),
                input.assignButtons[10],
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
    }

    asView { ^view }
}
