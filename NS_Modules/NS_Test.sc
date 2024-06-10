NS_Test : NS_SynthModule {
    classvar <isSource = true;

    *initClass {
        StartUp.add{
            SynthDef(\ns_Test,{
                var freq = LFNoise2.kr(1).range(80,5000);
                var sig = Select.ar(\which.kr(0),[SinOsc.ar(freq,mul: AmpCompA.kr(freq,80)),PinkNoise.ar()]);
                sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(0));

                XOut.ar(\bus.kr,\mix.kr(1), sig!2 )
            }).add
        }
    }

    init {
        this.initModuleArrays(2);

        this.makeWindow("Test", Rect(900,700,240,60));

        synths.add(Synth(\ns_Test,[\bus,bus],modGroup));

        controls.add( 
            NS_Switch(win,["sine","pink"],{ |switch| synths[0].set(\which,switch.value.indexOf(1) ) },'horz').maxHeight_(30)
        );
        assignButtons[0] = NS_AssignButton().maxHeight_(30).setAction(this, 0, \switch);

        controls.add(
            NS_Fader(win,"amp",\amp,{ |f| synths[0].set(\amp, f.value) },'horz').maxHeight_(30)
        );
        assignButtons[1] = NS_AssignButton().maxHeight_(30).setAction(this, 1, \fader);

        win.layout_(
            GridLayout.rows(
                [ controls[0], assignButtons[0] ],
                [ controls[1], assignButtons[1] ]
            )
        );

        win.layout.spacing_(2).margins_(4)
    }

    makeOSCFragment { }
}
