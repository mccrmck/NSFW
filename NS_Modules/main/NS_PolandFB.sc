NS_PolandFB : NS_SynthModule {
    classvar <isSource = true;
    var waveBus, feedBuf;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_polandFB,{
                var numChans = NSFW.numOutChans;
                var wave     = In.kr(\waveBus.kr(),40);
                var fbBuf    = \bufnum.kr;

                var sig;
                var noise = Dwhite(-1, 1) * \noiseAmp.kr(0.05);
                var osc = DemandEnvGen.ar(Dseq(wave,inf),\oscFreq.kr(40).reciprocal / 40,5,0,levelScale: \oscAmp.kr(0));
                var in = Dbufrd(fbBuf);

                in = in + noise + osc;
                in = in.wrap2(\wrap.kr(5));
                in = in.round( 2 ** (\bits.kr(24).neg) );

                sig = Dbufwr(in, fbBuf);
                sig = Duty.ar(SampleDur.ir * \sRate.kr(2), 0, sig);
                sig = LeakDC.ar(sig);
                sig = (sig * \trim.kr(1)).clip2;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(7);
        this.makeWindow("PolandFB",Rect(0,0,360,300));

        waveBus = Bus.control(modGroup.server,40).setn( 40.collect({|i| (i/40 * 2pi).sin }) );
        feedBuf = Buffer.alloc(modGroup.server,1);

        synths.add( Synth(\ns_polandFB,[\wave,waveBus, \bufnum, feedBuf, \bus,bus],modGroup) );

        controls.add(
            NS_XY("noiseAmp",ControlSpec(0,0.5,\amp),"oscAmp",ControlSpec(0,0.5,\amp),{ |xy| 
                synths[0].set(\noiseAmp,xy.x, \oscAmp, xy.y);
            },[0.05,0.04]).round_([0.01,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("sRate",ControlSpec(100,24000,\lin),"bits",ControlSpec(0.5,16,\lin),{ |xy| 
                var rate = modGroup.server.sampleRate / xy.x;
                synths[0].set(\sRate,rate, \bits, xy.y);
            },[24000,16]).round_([1,0.1])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        controls.add(
            NS_Fader("oscFreq",ControlSpec(0.1,250,\exp),{ |f| synths[0].set(\oscFreq, f.value)},'horz',initVal:40)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("wrap",ControlSpec(0.5,5,\exp),{ |f| synths[0].set(\wrap, f.value)},'horz',initVal:5)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);
        
        controls.add(
            NS_Fader("trim",ControlSpec(0.5,4,\amp),{ |f| synths[0].set(\trim, f.value)},'horz',initVal:1)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("amp",ControlSpec(0,1,\amp),{ |f| synths[0].set(\amp, f.value)},initVal:1).maxWidth_(45)
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
                VLayout(
                    HLayout(
                        VLayout( controls[0], assignButtons[0] ),
                        VLayout( controls[1], assignButtons[1] ),
                    ),
                    HLayout( controls[2], assignButtons[2] ),
                    HLayout( controls[3], assignButtons[3] ),
                    HLayout( controls[4], assignButtons[4] ),
                ),
                VLayout( controls[5], assignButtons[5], controls[6], assignButtons[6] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {
        ^OSC_Panel(horizontal:false, widgetArray:[
            OSC_Panel(height:"50%",widgetArray:[
                OSC_XY(snap:true),
                OSC_XY(snap:true),
            ]),
            OSC_Fader(horizontal:true,snap:true),
            OSC_Fader(horizontal:true,snap:true),
            OSC_Fader(horizontal:true,snap:true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width: "20%")
            ]),
        ],randCol:true).oscString("PolandFB")

    }
}
