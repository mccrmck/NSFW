NS_SpecDelay : NS_SynthModule {
    classvar <isSource = false;
    classvar maxDelay = 0.75;
    var fftBufSize, fftBufL, fftBufR, delsBufL, delsBufR, fbBuf;

    *initClass {
        StartUp.add{
            SynthDef(\ns_specDelay,{
                var sig = In.ar(\bus.kr,2) + PinkNoise.ar(0.0001); // to prevent denormals...
                var feedB = \fbBuf.kr;

                var sigL = FFT(\fftBufL.kr(0), sig[0], 0.5); // helpfile, spluta uses hopsize == 0.25
                var sigR = FFT(\fftBufR.kr(0), sig[1], 0.5);

                sigL = PV_BinDelay(sigL, maxDelay, \delayBufL.kr(0), feedB, 0.5);
                sigR = PV_BinDelay(sigR, maxDelay, \delayBufR.kr(0), feedB, 0.5);
                sig = [ IFFT(sigL), IFFT(sigR) ];

                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_XOut( \bus.kr, sig, \mix.kr(0.5), \thru.kr(0) )
            }).add;
        }
    }

    init {
        var cond = CondVar();
        this.initModuleArrays(5);

        this.makeWindow("SpecDelay",Rect(0,0,270,300));

        Routine({

            fftBufSize = 256;
            fftBufL  = Buffer.alloc(modGroup.server, fftBufSize, 1, { cond.signalOne });
            cond.wait { fftBufL.numFrames == fftBufSize };
            fftBufR  = Buffer.alloc(modGroup.server, fftBufSize, 1, { cond.signalOne });
            cond.wait { fftBufR.numFrames == fftBufSize };
            delsBufL = Buffer.alloc(modGroup.server, fftBufSize / 2, 1, { cond.signalOne });
            cond.wait { delsBufL.numFrames == (fftBufSize / 2) };
            delsBufR = Buffer.alloc(modGroup.server, fftBufSize / 2, 1, { cond.signalOne });
            cond.wait { delsBufR.numFrames == (fftBufSize / 2) };
            fbBuf    = Buffer.alloc(modGroup.server, fftBufSize / 2, 1, { cond.signalOne });
            cond.wait { fbBuf.numFrames == (fftBufSize / 2) };

            modGroup.server.sync;

            synths.add( Synth(\ns_specDelay,[\bus,bus,\fftBufL,fftBufL,\fftBufR,fftBufR,\delayBufL,delsBufL,\delayBufR,delsBufR,\fbBuf,fbBuf],modGroup) );

            controls.add(
                MultiSliderView()
                .size_( fftBufSize / 4 )
                .thumbSize_(4)
                .elasticMode_(1)
                .action_({ |ms|
                    var valL = (ms.value * maxDelay);
                    var zero = 0.dup(fftBufSize / 4);
                    var valR = valL.collect({ |i| (i + 0.05.rand2).clip(0,maxDelay) });
                    delsBufL.setn(0,valL ++ zero);
                    delsBufR.setn(0,valR ++ zero );
                })
            );
            assignButtons[0] = NS_AssignButton().setAction(this,0,\fader); //needs to be multiSlider

            controls.add(
                MultiSliderView()
                .size_( fftBufSize / 4 )
                .thumbSize_(4)
                .elasticMode_(1)
                .action_({ |ms|
                    var val = ms.value ++ 0.dup(fftBufSize / 4);
                    fbBuf.setn(0, val)
                })   
            );
            assignButtons[1] = NS_AssignButton().setAction(this,1,\fader); // needs to be multiSlider

            controls.add(
                Button()
                .states_([["rand",Color.black,Color.white]])
                .action_({ |but|
                    if(but.value == 1,{
                        var zero = 0.dup(fftBufSize / 4);
                        var valL = Array.fill(fftBufSize / 4,{ (maxDelay.asFloat/2).rand });
                        var valR = valL.collect({ |i| (i + 0.05.rand2).clip(0,maxDelay) });
                        var feedB = Array.fill(fftBufSize / 4,{ 0.4.rrand(0.8) });
                        { controls[0].valueAction_(valL / maxDelay) }.defer;
                        delsBufL.setn(0, valL ++ zero);          
                        delsBufR.setn(0, valR ++ zero);
                        { controls[1].valueAction_(feedB) }.defer;
                        fbBuf.setn(0, feedB + zero);
                    })
                })
            );
            assignButtons[2] = NS_AssignButton().maxWidth_(60).setAction(this,2,\button);

            // maybe add some other "presets" here? staircase, horseshoe, etc.

            controls.add(
                Button()
                .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
                .action_({ |but|
                    var val = but.value;
                    strip.inSynthGate_(val);
                    synths[0].set(\thru, val)
                })
            );
            assignButtons[3] = NS_AssignButton().maxWidth_(60).setAction(this,3,\button);

            controls.add(
                NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },'horz',initVal:0.5)
            );
            assignButtons[4] = NS_AssignButton().maxWidth_(60).setAction(this, 4, \fader);

            win.layout_(
                HLayout(
                    VLayout(
                        HLayout( StaticText().string_("delayTimes").align_(\center), assignButtons[0] ),
                        controls[0],
                        HLayout( StaticText().string_("feedback %").align_(\center), assignButtons[1] ),
                        controls[1],
                        HLayout( controls[2], assignButtons[2], controls[3], assignButtons[3] ),
                        HLayout( controls[4], assignButtons[4] )
                    )
                )
            );

            win.layout.spacing_(4).margins_(4);
        }).play(AppClock)
    }

    freeExtra {
        fftBufL.free;
        fftBufR.free;
        delsBufL.free;
        delsBufR.free;
        fbBuf.free; 
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false, widgetArray:[
            OSC_MultiFader(snap:true,numFaders: 64),
            OSC_MultiFader(snap:true,numFaders: 64),
            OSC_Button(height: "20%",mode: 'push'),
            OSC_Panel(height: "20%",widgetArray:[
                OSC_Fader(width: "75%",horizontal:true),
                OSC_Button()
            ])
        ],randCol: true).oscString("SpecDelay")
    }
}
