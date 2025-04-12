NS_EnvGen : NS_SynthModule {
    classvar <isSource = false;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(8);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_envGen" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var tFreq = \tFreq.kr(0.01);
                var rFreq = \rFreq.kr(0.25);
                var rMult = tFreq * \rMult.kr(1);

                var ramp = Select.kr(\which.kr(0),[
                    0,
                    LFSaw.kr(rFreq).range(0,rMult),
                    LFTri.kr(rFreq).range(0,rMult)
                ]);
                var trig = Impulse.kr(tFreq + ramp);
                var env = \env.kr(Env.perc(0.01,0.99,1,-4).asArray);
                env = EnvGen.ar(env,trig,timeScale: \tScale.kr(0.5));

                sig = sig * env;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\tFreq,ControlSpec(0.01,8,\exp),0.01)
        .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\tScale,ControlSpec(0.01,8,\exp),0.5)
        .addAction(\synth,{ |c| synths[0].set(\tScale, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\ramp,ControlSpec(0,2,\lin,1),0)
        .addAction(\synth,{ |c| synths[0].set(\which, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \switch).maxWidth_(30);

        controls[3] = NS_Control(\window,ControlSpec(0,2,\lin,1),0)
        .addAction(\synth,{ |c| 
            var env = switch(c.value.asInteger,
                0,{ Env.perc(0.01,0.99,1,4.neg).asArray },
                1,{ Env([0,1,0],[0.5,0.5],'wel').asArray },
                2,{ Env.perc(0.99,0.01,1,4).asArray }
            );

            synths[0].set(\env, env) 
        });
        assignButtons[3] = NS_AssignButton(this, 3, \switch).maxWidth_(30);

        controls[4] = NS_Control(\rampHz,ControlSpec(0.1,5,\exp),0.25)
        .addAction(\synth,{ |c| synths[0].set(\rFreq, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\rampMul,ControlSpec(1,10,\exp),1)
        .addAction(\synth,{ |c| synths[0].set(\rMult, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(30);

        controls[6] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(30);

        controls[7] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[7] = NS_AssignButton(this, 7, \button).maxWidth_(30);

        this.makeWindow("EnvGen", Rect(0,0,240,210));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]),                                 assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]),                                 assignButtons[1] ),
                HLayout( NS_ControlSwitch(controls[2], ["impulse","saw","tri"], 3),    assignButtons[2] ),
                HLayout( NS_ControlSwitch(controls[3], ["perc","welch","revPerc"], 3), assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4]),                                 assignButtons[4] ),
                HLayout( NS_ControlFader(controls[5]),                                 assignButtons[5] ),
                HLayout( NS_ControlFader(controls[6]),                                 assignButtons[6] ),
                HLayout( NS_ControlButton(controls[7], ["▶","bypass"]),                assignButtons[7] ),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(horizontal: false),
            OSC_Fader(horizontal: false),
            OSC_Switch(3),
            OSC_Switch(3),
            OSC_Fader(horizontal: false),
            OSC_Fader(horizontal: false),
            OSC_Panel([OSC_Fader(false, false), OSC_Button(height:"20%")])     
        ], columns: 7, randCol: true).oscString("EnvGen")
    }
}
