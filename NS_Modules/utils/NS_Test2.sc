NS_Test2 : NS_SynthModule {
    classvar <isSource = true;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_test,{
                var numChans = NSFW.numChans;
                var freq = LFDNoise3.kr(1).range(80,8000);
                var sig = Select.ar(\which.kr(0),[SinOsc.ar(freq,mul: AmpCompA.kr(freq,80)),PinkNoise.ar()]);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add
        }
    }

    init {
        var gui = List.newClear();
        this.initModuleArrays(3);
        this.makeWindow("Test", Rect(0,0,240,60));

        synths.add( Synth(\ns_test,[\bus,bus],modGroup) );

        controls.put(0, 
            NS_Control(\which,ControlSpec(0,1,'lin',1),0)
            .addAction(\synth,{ |c| synths[0].set(\which, c.value) })
        );
        gui.add(
            NS_Switch(["sine","pink"],{ |switch| controls[0].value_(switch.value) },'horz').maxHeight_(30)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \switch).maxWidth_(45);

        controls.put(1,
            NS_Control(\amp,\amp.asSpec)
            .addAction(\synth,{ |c| synths[0].set(\amp, c.value) })
        );
        gui.add(
            NS_Fader(controls[1].label,controls[1].spec,{ |f| controls[1].value_( f.value ) },'horz').maxHeight_(30),
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.put(2,
             NS_Control(\bypass,ControlSpec(0,1,'lin',1),0)
            .addAction(\synth,{ |c| synths[0].set(\thru, c.value) })
        );
        gui.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                controls[2].value_( but.value, \qtGui )
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        controls[0].addAction(\qtGui,{ |c| { gui[0].value_(c.value) }.defer });
        controls[1].addAction(\qtGui,{ |c| { gui[1].value_(c.value) }.defer });
        controls[2].addAction(\qtGui,{ |c| { gui[2].value_(c.value) }.defer });

        win.layout_(
            HLayout(
                VLayout(
                    HLayout( gui[0], assignButtons[0] ),
                    HLayout( gui[1], assignButtons[1] ),
                ),
                VLayout( gui[2], assignButtons[2] ))
            );

        win.layout.spacing_(2).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(horizontal:false,widgetArray:[
            OSC_Switch(numPads:2),
            OSC_Fader(horizontal:true),
            OSC_Button()
        ],randCol: true).oscString("Test")
    }
}
