NS_VarDelay : NS_SynthModule {
    classvar <isSource = true;
    var buffer;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_varDelay,{
                var sig = In.ar(\bus.kr,numChans);
                var buffer = \buffer.kr(0 ! numChans);
                var clip = \clip.kr(1);

                var tap = DelTapWr.ar(buffer,sig + LocalIn.ar(numChans));

                sig = DelTapRd.ar(buffer,tap,\dTime.kr(0.2,0.05) + SinOsc.ar(\sinFreq.kr(0.05) * ({ 0.9.rrand(1) } ! numChans)).range(-0.02,0),2); 
                sig = sig + PinkNoise.ar(0.0001);
                sig = Clip.ar(sig,clip.neg,clip);

                LocalOut.ar(sig.rotate(1) * \feedB.kr(0.95));

                sig = LeakDC.ar(sig);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(0), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("VarDelay",Rect(0,0,240,150));

        buffer = Buffer.allocConsecutive(NSFW.numChans(modGroup.server), modGroup.server, modGroup.server.sampleRate);
        synths.add( Synth(\ns_varDelay,[\buffer, buffer, \bus, bus],modGroup));

        controls[0] = NS_Control(\dtime, ControlSpec(0.01,1,\lin),0.2)
        .addAction(\synth,{ |c| synths[0].set(\dTime, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\clip, ControlSpec(1,0.01,\lin),0.2)
        .addAction(\synth,{ |c| synths[0].set(\clip, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\sinHz, ControlSpec(0.01,40,\exp),0.05)
        .addAction(\synth,{ |c| synths[0].set(\sinHz, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\feedB, ControlSpec(0.5,1.05,\exp),0.95)
        .addAction(\synth,{ |c| synths[0].set(\feedB, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),0)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);


        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1])                , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3])                , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4])                , assignButtons[4] ),
                HLayout( NS_ControlButton(controls[5],["▶","bypass"]), assignButtons[5] ),
            )
        );

        win.layout.spacing_(4).margins_(4);
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width:"20%")], columns: 2)
        ],randCol: true).oscString("VarDelay")
    }
}
