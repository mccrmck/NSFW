NS_RingMod2 : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_ringMod2,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var freq = \freq.kr(40).lag(0.05);
                sig = sig * SinOsc.ar(freq + SinOsc.ar(\modFreq.kr(40),mul: \modMul.kr(1)) );

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        var gui = List.newClear(0);
        this.initModuleArrays(5);
        this.makeWindow("RingMod2", Rect(0,0,270,120));

        synths.add( Synth(\ns_ringMod2,[\bus,bus],modGroup) );

        controls.put(0,
            NS_Control(\freq,ControlSpec(1,3500,\exp),40)
            .addAction(\synth,{ |c| synths[0].set(\freq, c.value) })
        );
        gui.add(
            NS_Fader(controls[0].label,controls[0].spec,{ |f| controls[0].value_( f.value, \qtGui  ) },'horz').round_(1)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.put(1,
            NS_Control(\modFreq,ControlSpec(1,3500,\exp),4)
            .addAction(\synth,{ |c| synths[0].set(\modFreq, c.value) })
        );
        gui.add(
            NS_Fader(controls[1].label,controls[1].spec,{ |f| controls[1].value_( f.value, \qtGui ) },'horz').round_(1)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.put(2,
            NS_Control(\modMul,ControlSpec(1,3500,\amp))
            .addAction(\synth,{ |c| synths[0].set(\modMul, c.value) })
        );
        gui.add(
            NS_Fader(controls[2].label,controls[2].spec,{ |f| controls[2].value_( f.value, \qtGui  ) },'horz').round_(1)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);


        controls.put(3,
            NS_Control(\mix,ControlSpec(0,1,\lin),1)
            .addAction(\synth,{ |c| synths[0].set(\mix, c.value) })
        );
        gui.add(
            NS_Fader(controls[3].label,controls[3].spec,{ |f| controls[3].value_( f.value, \qtGui ) },'horz')
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.put(4,
            NS_Control(\bypass,ControlSpec(0,1,'lin',1),0)
            .addAction(\synth,{ |c| synths[0].set(\thru, c.value); /*strip.inSynthGate_( c.value ) */  })
        );
        gui.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but| controls[2].value_( but.value, \qtGui ) })
        );
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(45);

        controls[0].addAction(\qtGui,{ |c| { gui[0].value_(c.value) }.defer });
        controls[1].addAction(\qtGui,{ |c| { gui[1].value_(c.value) }.defer });
        controls[2].addAction(\qtGui,{ |c| { gui[2].value_(c.value) }.defer });
        controls[3].addAction(\qtGui,{ |c| { gui[3].value_(c.value) }.defer });
        controls[4].addAction(\qtGui,{ |c| { gui[4].value_(c.value) }.defer });

        win.layout_(
            VLayout(
                HLayout( gui[0], assignButtons[0] ),
                HLayout( gui[1], assignButtons[1] ),
                HLayout( gui[2], assignButtons[2] ),
                HLayout( gui[3], assignButtons[3] ),
                HLayout( gui[4], assignButtons[4] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader("15%",snap:true),
            OSC_Panel("15%",horizontal:false,widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
            ])
        ],randCol:true).oscString("RingMod2")
    }
}
