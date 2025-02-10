NS_DynKlank : NS_SynthModule {
    classvar <isSource = false;
    var notes;
    var octaveBus, bandAmpBus, bandMuteBus, ringTimeBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_dynKlank,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans).sum * numChans.reciprocal;
                var freq = (60,61..71).midicps * 2;
                var octave = In.kr(\octave.kr,12);
                var bandAmp = In.kr(\bandAmp.kr,12);
                var bandMute = In.kr(\bandMute.kr,12);
                var ringTime = In.kr(\ringTime.kr,12);

                sig = sig  * -18.dbamp * \trim.kr(1);
                sig = DynKlank.ar(`[ freq * octave.lag(1), bandAmp.lag(0.1) * bandMute.varlag(4), ringTime.lag(1) ],sig);

                sig = sig.tanh * \gain.kr(1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add     
        }
    }

    init {
        this.initModuleArrays(52);
        this.makeWindow("DynKlank", Rect(0,0,810,400));

        notes = ["C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"];

        octaveBus   = Bus.control(modGroup.server,12).setn(1!12);
        bandAmpBus  = Bus.control(modGroup.server,12).setn(0!12);
        bandMuteBus = Bus.control(modGroup.server,12).setn(0!12);
        ringTimeBus = Bus.control(modGroup.server,12).setn(0.25!12);

        synths.add( Synth(\ns_dynKlank,[\octave,octaveBus,\bandAmp,bandAmpBus,\bandMute, bandMuteBus,\ringTime,ringTimeBus,\bus,bus],modGroup) );

        notes.do({ |note, index|

            controls[index * 4] = NS_Control(note + "dB",\amp,0)
            .addAction(\synth,{ |c| bandAmpBus.subBus(index).set( c.value ) });
            assignButtons[index * 4] = NS_AssignButton(this, index * 4, \fader).maxWidth_(45);

            controls[index * 4 + 1] = NS_Control(note + "dcy", ControlSpec(0.1,1.5,\lin), 0.25)
            .addAction(\synth,{ |c| ringTimeBus.subBus(index).set( c.value ) });
            assignButtons[index * 4 + 1] = NS_AssignButton(this, index * 4 + 1, \fader).maxWidth_(45);

            controls[index * 4 + 2] = NS_Control(note + "oct",ControlSpec(0,4,\lin,1),2)
            .addAction(\synth,{ |c| octaveBus.subBus(index).set([4,2,1,0.5,0.25].at(c.value)) });
            assignButtons[index * 4 + 2] = NS_AssignButton(this, index * 4 + 2, \switch).maxWidth_(30);

            controls[index * 4 + 3] = NS_Control(note + "on",ControlSpec(0,1,\lin,1),0)
            .addAction(\synth,{ |c| bandMuteBus.subBus(index).set(c.value) });
            assignButtons[index * 4 + 3] = NS_AssignButton(this, index * 4 + 3, \button).maxWidth_(30);
        });

        controls[48] = NS_Control(\trim,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });
        assignButtons[48] = NS_AssignButton(this, 48, \fader).maxWidth_(30);

        controls[49] = NS_Control(\gain,ControlSpec(-12,12,\db),0)
        .addAction(\synth,{ |c| synths[0].set(\gain, c.value.dbamp) });
        assignButtons[49] = NS_AssignButton(this, 49, \fader).maxWidth_(30);


        controls[50] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[50] = NS_AssignButton(this, 50, \fader).maxWidth_(30);

        controls[51] = NS_Control(\bypass,ControlSpec(0,1,\lin,1),0)
        .addAction(\synth,{ |c| 
            var val = c.value;
            strip.inSynthGate_(val);
            synths[0].set(\thru, val)
        });
        assignButtons[51] = NS_AssignButton(this, 51, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                GridLayout.rows(
                    *notes.collect({ |n, i|
                        var index = (i * 4).asInteger;
                        GridLayout.columns(
                            [ [NS_ControlFader(controls[index], 'vert'), rows: 3],     assignButtons[index] ],
                            [ [NS_ControlFader(controls[index + 1], 'vert'), rows: 3], assignButtons[index + 1] ],
                            [ NS_ControlSwitch(controls[index + 2],["16va","8va","nat","8vb","16vb"]).maxWidth_(30), assignButtons[index + 2],
                            NS_ControlButton(controls[index + 3],["▶","X"]).maxWidth_(30), assignButtons[index + 3] // [["▶",Color.green,Color.black],["X",Color.black,Color.red]]
                        ]).margins_(0)
                    }).clump(6),
                ),
                HLayout( 
                    NS_ControlFader(controls[48]), assignButtons[48],
                    NS_ControlFader(controls[49]), assignButtons[49],
                    NS_ControlFader(controls[50]), assignButtons[50],
                    NS_ControlButton(controls[51], ["▶","bypass"]).maxWidth_(45), assignButtons[51],
                )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        octaveBus.free;
        bandAmpBus.free;
        bandMuteBus.free;
        ringTimeBus.free;
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Panel(widgetArray: { OSC_Button() } ! 6),
            OSC_Panel(widgetArray: { OSC_Button() } ! 6),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal:true),
                OSC_Button(width:"20%")
            ])
        ],randCol: true).oscString("DynKlank")
    }
}
