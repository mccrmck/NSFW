NS_Freedom2Live : NS_SynthModule {
    classvar <isSource = true;
    var durBus, atkBus, rlsBus, curveBus, rqBus, strumBus, ampBus;
    var arpPat;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_freedom2Live,{
                var filtEnv = XLine.kr(0.8,1.2,Rand(0.1,0.8));
                var sig = LFTri.ar([1,2] * \freq.kr(80)).sum;

                sig = sig * SinOsc.kr(\tremFreq.kr(0.03),Rand(pi/2,pi)).range(0.25,0.5);
                sig = RLPF.ar(sig,\filtFreq.kr(8000) * filtEnv,\rq.kr(1));
                sig = (sig * 2).tanh;
                sig = sig * Env.perc(\atk.kr(0.1),\rls.kr(1),1,\curve.kr(-3)).ar(2);
                sig = LeakDC.ar(sig);

                sig = Pan2.ar(sig,\pan.kr(0),\amp.kr(0.2));
                Out.ar(\outBus.kr,sig);       
            }).add
        }
    }

    init {
        this.initModuleArrays(8);

        this.makeWindow("Freedom2Live", Rect(0,0,270,210));

        TempoClock.default.tempo = 92.5/60;

        arpPat = this.pattern(modGroup, bus);

        durBus    = Bus.control(modGroup.server,1).set(1);
        atkBus    = Bus.control(modGroup.server,1).set(0.01);
        rlsBus    = Bus.control(modGroup.server,1).set(1);
        curveBus  = Bus.control(modGroup.server,1).set(-4);
        rqBus     = Bus.control(modGroup.server,1).set(0.5);
        strumBus  = Bus.control(modGroup.server,1).set(0);
        ampBus    = Bus.control(modGroup.server,1).set(1);

        controls.add(
            NS_Fader("dur",ControlSpec(0.5,2,\exp),{ |f| durBus.set( f.value ) },'horz',initVal:1)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);
        
        controls.add(
            NS_Fader("atk",ControlSpec(0.01,1,\exp),{ |f| atkBus.set( f.value ) },'horz',initVal:0.01)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("rls",ControlSpec(0.1,2,\exp),{ |f| rlsBus.set( f.value ) },'horz',initVal:1)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("crv",ControlSpec(-10,10,\lin),{ |f| curveBus.set( f.value ) },'horz',initVal:-4)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("rq",ControlSpec(0.05,1,\exp),{ |f| rqBus.set( f.value ) },'horz',initVal:0.5)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("str",ControlSpec(-0.5,0.5,\lin),{ |f| strumBus.set( f.value ) },'horz',initVal:0)
        );
        assignButtons[5] = NS_AssignButton(this, 5, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("amp",\db,{ |f| ampBus.set(f.value.dbamp) },'horz',initVal:0)
        );
        assignButtons[6] = NS_AssignButton(this, 6, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                if(val == 1,{
                    arpPat.play
                },{
                    arpPat.stop
                })
            })
        );
        assignButtons[7] = NS_AssignButton(this, 7, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3] ),
                HLayout( controls[4], assignButtons[4] ),
                HLayout( controls[5], assignButtons[5] ),
                HLayout( controls[6], assignButtons[6], controls[7], assignButtons[7])
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    pattern { |grp, outBus|
        ^Pdef(\liveFree, 
            Pbind(
                \server,     grp.server,
                \instrument, \ns_freedom2Live,
                \group,      grp.nodeID,
                \dur,        Pfunc{ durBus.getSynchronous },
                \freq,       Pseq( this.data, inf ).midicps,
                \atk,        Pfunc{ atkBus.getSynchronous },
                \rls,        Pfunc{ rlsBus.getSynchronous },
                \curve,      Pfunc{ curveBus.getSynchronous },
                \tremFreq,   Pwhite(0.5,2,inf),
                \filtFreq,   Pfunc{ |event| event.freq.last },
                \rq,         Pfunc{ rqBus.getSynchronous },
                \strum,      Pfunc{ strumBus.getSynchronous } + Pbrown(-0.1,0.1,0.01),
                \pan,        Pgauss(0.0,0.3,inf),
                \amp,        Pfunc{ ampBus.getSynchronous } * -12.dbamp,
                \outBus,     bus,
            )
        )
    }

    data {
        ^[
            [58, 63, 68, 72, 80],
            [63, 70, 77, 80, 89],
            [60, 68, 75, 77, 87],
            [56, 65, 70, 75, 82],
            [59, 64, 69, 74, 81],
            [64, 71, 74, 81, 86],
            [62, 69, 71, 76, 83],
            [57, 62, 64, 71, 76],
            [53, 57, 60, 67, 72],
            [48, 55, 57, 65, 69],
            [53, 60, 67, 69, 79],
            [55, 60, 65, 69, 77],
            [53, 58, 61, 67, 73],
            [49, 55, 60, 65, 72],
            [61, 67, 70, 77, 82],
            [58, 65, 67, 73, 79],
            [57, 62, 67, 77, 81],
            [55, 62, 65, 69, 77],
            [53, 57, 62, 67, 74],
            [50, 55, 57, 65, 69],
            [53, 57, 64, 65, 72, 77],
            [53, 60, 64, 69, 76],
            [52, 57, 60, 65, 72],
            [48, 53, 55, 64, 67],
            [47, 52, 54, 62, 66],
            [42, 50, 52, 59, 64],
            [50, 54, 59, 64, 71],
            [52, 59, 62, 78, 74],
            [52, 58, 62, 65, 74],
            [50, 53, 58, 64, 70],
            [53, 62, 64, 70, 76],
            [58, 64, 65, 74, 77],
            [58, 63, 67, 72, 79],
            [55, 60, 63, 70, 75],
            [51, 58, 60, 67, 72],
            [48, 55, 58, 63, 70],
            [62, 67, 70, 77, 82],
            [58, 65, 67, 74, 79],
            [55, 62, 65, 70, 77],
            [53, 58, 62, 67, 74],
            [61, 66, 69, 72, 77, 84],
            [54, 60, 65, 69, 72, 81],
            [54, 57, 65, 72, 78],
            [53, 54, 60, 65, 69, 77],
            [57, 63, 66, 72, 78],
            [60, 66, 72, 75, 81, 87],
            [57, 63, 69, 72, 78, 84],
            [59, 63, 69, 73, 78, 85],
            [62, 65, 70, 74, 81, 86],
            [64, 69, 72, 77, 84],
            [57, 58, 64, 65, 72, 77],
            [58, 64, 67, 72, 79],
            [60, 53, 68, 74, 80],
            [56, 62, 67, 72, 79],
            [53, 56, 60, 67, 72],
            [55, 60, 62, 68, 74],
            [58, 65, 66, 72, 78],
            [54, 60, 65, 70, 77],
            [48, 54, 58, 65, 70],
            [53, 58, 60, 66, 72],
            [53, 58, 61, 67, 73],
            [46, 53, 55, 61, 67],
            [49, 55, 58, 65, 70],
            [55, 61, 65, 70, 77],
            [58, 65, 69, 74, 81],
            [62, 69, 70, 77, 82],
            [57, 62, 65, 70, 77],
            [53, 58, 62, 69, 74],
            [54, 61, 65, 70, 77],
            [58, 65, 66, 73, 78],
            [49, 54, 58, 65, 70],
            [41, 49, 66, 58, 66],
            [41, 50, 53, 58, 65],
            [53, 58, 62, 69, 74],
            [50, 57, 58, 65, 70],
            [58, 65, 69, 74, 81],
            [60, 65, 68, 75, 80],
            [65, 72, 75, 80, 87],
            [63, 68, 72, 77, 84],
            [68, 75, 77, 84, 89],
            [70, 75, 78, 85, 90],
            [73, 78, 82, 87, 94],
            [75, 80, 84, 89, 96],
            [66, 73, 75, 82, 87],
            [68, 73, 77, 82, 89],
            [70, 77, 79, 85, 91],
            [61, 67, 70, 75, 82],
            [58, 65, 68, 73, 80],
            [59, 66, 69, 74, 81],
            [62, 69, 71, 78, 83],
            [57, 62, 66, 71, 78],
            [57, 62, 64, 71, 76],
            [62, 69, 72, 77, 84],
            [57, 62, 65, 72, 77],
            [60, 65, 69, 74, 81],
            [57, 65, 67, 74, 79],
            [61, 65, 66, 72, 73, 78],
            [60, 65, 70, 72, 77],
            [58, 60, 65, 66, 72],
            [54, 58, 60, 65, 70],
            [53, 58, 61, 67, 73],
            [49, 54, 58, 65, 72],
            [53, 58, 61, 67, 73],
            [61, 67, 72, 77, 84],
            [60, 65, 67, 69, 76, 81],
        ]  
    }

    freeExtra {
        arpPat.clear;
        durBus.free;
        atkBus.free;
        rlsBus.free;
        curveBus.free;
        rqBus.free;
        strumBus.free;
        ampBus.free;
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false,widgetArray:[
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(horizontal: true),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width: "20%")
            ]),
        ],randCol:true).oscString("Free2Live")
    }
}
