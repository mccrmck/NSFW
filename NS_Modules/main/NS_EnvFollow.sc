NS_EnvFollow : NS_SynthModule {
    classvar <isSource = false;
    var dragSink;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_envFollow,{
                var sr = SampleRate.ir;
                var sig = In.ar(\bus.kr, numChans);
                var amp = In.ar(\ampIn.kr, numChans).sum * numChans.reciprocal.sqrt;
                amp = amp * \gain.kr(1);
                amp = FluidLoudness.kr(amp,[\loudness],windowSize: sr * 0.4, hopSize: sr * 0.1).dbamp;

                sig = sig * LagUD.kr(amp,\up.kr(0.1),\down.kr(0.1));
                sig = sig * \trim.kr(1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("EnvFollow", Rect(0,0,240,150));

        synths.add( Synth(\ns_envFollow,[\bus, bus],modGroup) );

        controls[0] = NS_Control(\atk, ControlSpec(0.01,1,\exp), 0.1)
        .addAction(\synth, { |c| synths[0].set(\up, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\rls, ControlSpec(0.01,2,\exp), 0.1)
        .addAction(\synth, { |c| synths[0].set(\down, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\gain, \boostcut, 0)
        .addAction(\synth, { |c| synths[0].set(\gain, c.value.dbamp) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\trim, \boostcut, 0)
        .addAction(\synth, { |c| synths[0].set(\trim, c.value.dbamp) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| strip.inSynthGate_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        dragSink = DragSink()
        .align_(\center).background_(Color.white).string_("in")
        .receiveDragHandler_({ |drag|
            var dragObject = View.currentDrag;

            if(dragObject.isInteger and: {dragObject < NSFW.servers[modGroup.server.name].options.inChannels},{

                drag.object_(dragObject);
                drag.align_(\left).string_("in:" + dragObject.asString);
                synths[0].set( \ampIn, NSFW.servers[modGroup.server.name].inputBusses[dragObject] )
            },{
                "dragObject not valid".warn
            })
        });

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                           , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1])                           , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                           , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3])                           , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4])                           , assignButtons[4] ),
                HLayout( dragSink, NS_ControlButton(controls[5], ["▶","bypass"]), assignButtons[5] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    saveExtra { |saveArray|
        saveArray.add([ dragSink.object ]);
        ^saveArray
    }

    loadExtra { |loadArray|
        var val = loadArray[0];

        if(val.notNil,{
            dragSink.object_(val);
            dragSink.align_(\left).string_("in:" + val.asString);
            synths[0].set( \ampIn, NSFW.servers[strip.modGroup.server.name].inputBusses[val] )
        })
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width:"20%")], columns: 2)
        ],randCol:true).oscString("EnvFollow")
    }
}
