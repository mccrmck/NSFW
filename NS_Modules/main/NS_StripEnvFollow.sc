NS_StripEnvFollow : NS_SynthModule {
    classvar <isSource = false;
    var followBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_stripEnvTrack,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                //var amp = Amplitude.ar(sig.sum * numChans.reciprocal,\atk.kr(0.01),\rls.kr(0.1));
                var amp = FluidLoudness.kr(sig,[\loudness],windowSize:SampleRate.ir*0.4,hopSize:SampleRate.ir*0.1).dbamp;
                NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                Out.kr(\followBus.kr,amp)
            }).add;

            SynthDef(\ns_stripEnvFollow,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);

                sig = sig * In.kr(\ampComp.kr(1)); // should this be multichannel?!?!
                sig = sig * \trim.kr(1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add; 
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("StripEnvFollow", Rect(0,0,270,120));

        followBus = Bus.control(modGroup.server,1).set(0);

        synths.add( Synth(\ns_stripEnvTrack,[\bus,bus,\followBus, followBus],strip.inGroup,\addToTail) );
        synths.add( Synth(\ns_stripEnvFollow,[\bus,bus,\ampComp,followBus],modGroup) );

        controls.add(
            NS_Fader("atk",ControlSpec(0.01,0.1),{ |f| synths[0].set(\atk, f.value) },'horz',0.01).round_(0.01)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("rls",ControlSpec(0.01,0.5),{ |f| synths[0].set(\rls, f.value) },'horz',0.1).round_(0.01)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("trim",\boostcut,{ |f| synths[1].set(\trim, f.value.dbamp) },'horz',0)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[1].set(\mix, f.value) },'horz',initVal:1)  
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[1].set(\thru, val)
            })
        );
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3], controls[4], assignButtons[4] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        followBus.free; 
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Panel( widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])

        ],randCol:true).oscString("EnvFollow")
    }
}
