NS_ServerInput : NS_ControlModule { // maybe this needs to be a SynthModule eventually?
    var nsServer, <inBus;

    var stripBus, outBus;
    var stripGroup, inGroup, faderGroup;
    var inSynth, fader;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_inputMono,{
                var sig = SoundIn.ar(\inBus.kr());
                var thresh = \gateThresh.kr(-72);
                var sDur = SampleRate.ir * 0.01;

                sig = HPF.ar(sig,\hpFreq.kr(40)); // hpf
                sig = sig * FluidAmpGate.ar(sig,10,10,thresh,thresh-5,sDur,sDur,sDur,sDur).lagud(0.01,0.1); // gate
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),1);
                Out.ar(\outBus.kr, sig )
            }).add;

            SynthDef(\ns_inputStereo,{
                var inBus = \inBus.kr();
                var sig = SoundIn.ar([inBus,inBus + 1]).sum * -3.dbamp;
                var thresh = \gateThresh.kr(-72);
                var sDur = SampleRate.ir * 0.01;

                sig = HPF.ar(sig,\hpFreq.kr(40)); // hpf
                sig = sig * FluidAmpGate.ar(sig,10,10,thresh,thresh-5,sDur,sDur,sDur,sDur).lagud(0.01,0.1); // gate
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
                Out.ar(\outBus.kr, sig )
            }).add;

            SynthDef(\ns_inFader,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr, 1) * \gain.kr(1);

                // compressor 
                var amp = Amplitude.ar(sig, \atk.kr(0.01), \rls.kr(0.1)).max(-100.dbamp).ampdb;
                amp = ((amp - \compThresh.kr(-12)).max(0) * (\ratio.kr(4).reciprocal - 1)).lag(\knee.kr(0.01)).dbamp;
                sig = sig * amp * \muGain.kr(0).dbamp;

                sig = ReplaceBadValues.ar(sig); // ReplaceBadValues
                SendPeakRMS.ar(sig,10,3,'/inSynth',0); // SendPeakRMS

                Out.ar(\sendBus.kr,sig ! numChans); // send to ChannelStrips

                sig = sig * (1 - \mute.kr(0,0.01)); // mute
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01)); // fader

                // to OutChannelStrips
                // this is temporarily commented out while I figure out a routing matrix
                // Out.ar(\outBus.kr, sig ! numChans); // this goes to 4 send synths
            }).add;

            SynthDef(\ns_bellEQ,{
                var sig = In.ar(\bus.kr,1);
                var gain = In.kr(\gain.kr(0),1);

                sig = MidEQ.ar(sig,\freq.kr(440),\rq.kr(1),gain);
                sig = NS_Envs(sig,\gate.kr(1),\pauseGate.kr(1),1);

                ReplaceOut.ar(\bus.kr,sig)
            }).add

        }
    }

    *new { |nsServer, inBus|
        ^super.new.init(nsServer, inBus)
    }

    init { |server_, in|
        this.initControlArrays(11);
        nsServer = server_;
        inBus = in.asInteger;

        stripBus = Bus.audio(nsServer.server, 1);
        outBus   = Bus.audio(nsServer.server, NSFW.numChans);
        // eqBus = Bus.control(server, 30).setn(0!30);

        stripGroup = Group(nsServer.inGroup, \addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        // eqGroup    = Group(stripGroup,\addToTail);
        faderGroup = Group(stripGroup,\addToTail);

        inSynth = Synth(\ns_inputMono,[\inBus,inBus,\outBus,stripBus],inGroup);
        fader   = Synth(\ns_inFader,[
            \inBus, stripBus,
            \sendBus, nsServer.inputBusses[inBus], // pretty sure this could be better...(assumes consecutive busses)
            \outBus,outBus
        ],faderGroup);

        controls[0] = NS_Control(\hpf,ControlSpec(20,320,\exp),40)
        .addAction(\synth,{ |c| inSynth.set(\hpFreq, c.value)  });
        assignButtons[0] = NS_AssignButton(this, 0, \knob);

        controls[1] = NS_Control(\gate,ControlSpec(-72,-32,\db),-72)
        .addAction(\synth,{ |c| inSynth.set(\gateThresh, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \knob);

        controls[2] = NS_Control(\gain,\boostcut)
        .addAction(\synth,{ |c| fader.set(\gain, c.value.dbamp) });
        assignButtons[2] = NS_AssignButton(this, 2, \knob);

        controls[3] = NS_Control(\comp,\db,-12)
        .addAction(\synth,{ |c| fader.set(\compThresh, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \knob);

        controls[4] = NS_Control(\atk,ControlSpec(0.001,0.25),0.01)
        .addAction(\synth,{ |c| fader.set(\atk, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \knob);

        controls[5] = NS_Control(\rls,ControlSpec(0.001,0.25),0.1)
        .addAction(\synth,{ |c| fader.set(\rls, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \knob);

        controls[6] = NS_Control(\ratio,ControlSpec(1,20),4)
        .addAction(\synth,{ |c| fader.set(\ratio, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \knob);

        controls[7] = NS_Control(\knee,ControlSpec(0,1),0.01)
        .addAction(\synth,{ |c| fader.set(\knee, c.value) });
        assignButtons[7] = NS_AssignButton(this, 7, \knob);

        controls[8] = NS_Control(\mUpGain,\boostcut)
        .addAction(\synth,{ |c| fader.set(\muGain, c.value) });
        assignButtons[8] = NS_AssignButton(this, 8, \knob);

        controls[9] = NS_Control(\amp,\db)
        .addAction(\synth,{ |c| fader.set(\amp, c.value.dbamp) });
        assignButtons[9] = NS_AssignButton(this, 9, \fader);

        controls[10] = NS_Control(\mute,ControlSpec(0,1,'lin',1))
        .addAction(\synth,{ |c| fader.set(\mute, c.value) });
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
                    [[ 
                        UserView().minHeight_(15).drawFunc_({ |v|
                            var w = v.bounds.width;
                            var h = v.bounds.height;
                            var r = w.min(h) / 2;
                            Pen.fillColor_(NS_Style.bGroundLight);
                            Pen.addRoundedRect(Rect(0,0,w,h), r, r);
                            Pen.fill;
                            Pen.stringCenteredIn(
                                "input: %".format(input.inBus.asString),
                                Rect(0,0,w,h),
                                Font(*NS_Style.defaultFont),
                                NS_Style.textDark
                            );
                            Pen.stroke;
                        }),
                        columns: 2
                    ]],
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
