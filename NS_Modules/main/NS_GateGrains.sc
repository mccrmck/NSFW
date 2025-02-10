NS_GateGrains : NS_SynthModule {
    classvar <isSource = false;
    var buffer;

    // inspired by/adapted from the FluidAmpGate helpfile example
    *initClass {
        ServerBoot.add{
            SynthDef(\ns_gateGrains,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr,numChans).sum * numChans.reciprocal;
                var thresh = \thresh.kr(-18);
                var width = \width.kr(0.5);
                var bufnum = \bufnum.kr;
                var sliceDur = SampleRate.ir * 0.01;
                var gate = FluidAmpGate.ar(sig,10,10,thresh,thresh-5,sliceDur,sliceDur,sliceDur,sliceDur);
                var phase = Phasor.ar(DC.ar(0),1 * gate,0,BufFrames.kr(bufnum) - 1);
                var trig = Impulse.ar(\tFreq.kr(8)) * (1-gate);
                var pan = Demand.ar(trig,0,Dwhite(width.neg,width));
                var pos = \pos.kr(0) + Demand.ar(trig,0,Dwhite(-0.002,0.002));

                var rec = BufWr.ar(sig,bufnum,phase);

                // i could get fancy and add gain compensation based on overlap? must test...
                sig = GrainBuf.ar(numChans,trig,\grainDur.kr(0.1),bufnum,\rate.kr(1),pos.clip(0,1),4,pan);
                sig = sig.tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(8);
        this.makeWindow("GateGrains", Rect(0,0,270,210));

        fork {
            buffer = Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 2);
            modGroup.server.sync;
            synths.add( Synth(\ns_gateGrains,[\bufnum,buffer,\bus,bus],modGroup) );
        };

        controls[0] = NS_Control(\grainDur,ControlSpec(0.01,1,\exp),0.1)
        .addAction(\synth,{ |c| synths[0].set(\grainDur, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\tFreq,ControlSpec(4,80,\exp),8)
        .addAction(\synth,{ |c| synths[0].set(\tFreq, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\pos,ControlSpec(0,1,\lin),0)
        .addAction(\synth,{ |c| synths[0].set(\pos, c.value) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\rate,ControlSpec(0.25,2,\exp),1)
        .addAction(\synth,{ |c| synths[0].set(\rate, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);
        
        controls[4] = NS_Control(\thresh,ControlSpec(-72,-18,\db),-18)
        .addAction(\synth,{ |c| synths[0].set(\thresh, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader);

        controls[5] = NS_Control(\width,ControlSpec(0,1,\lin),0.5)
        .addAction(\synth,{ |c| synths[0].set(\width, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(30);

        controls[6] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(30);

        controls[7] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| /*strip.inSynthGate_(c.value);*/ synths[0].set(\thru, c.value) });
        assignButtons[7] = NS_AssignButton(this, 7, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1])                , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3])                , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4]).round_(1)      , assignButtons[4] ),
                HLayout( NS_ControlFader(controls[5])                , assignButtons[5] ),
                HLayout( NS_ControlFader(controls[6])                , assignButtons[6] ),
                HLayout( NS_ControlButton(controls[7],["▶","bypass"]), assignButtons[7] ),   
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra { buffer.free }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Panel(height:"50%",widgetArray:[
                OSC_XY(snap:true),
                OSC_XY(snap:true),
            ]),
            OSC_Fader(horizontal: true),
            OSC_Fader(horizontal: true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("GateGrains")
    }
}
