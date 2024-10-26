NS_EnvFollow : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_envFollow,{
                var numChans = NSFW.numChans;
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
        this.makeWindow("EnvFollow", Rect(0,0,270,270));

        synths.add( Synth(\ns_envFollow,[\bus, bus],modGroup) );

        controls.add(
            NS_XY("atk",ControlSpec(0.01,1.0,\exp),"rls",ControlSpec(0.01,2,\exp),{ |xy|
                synths[0].set(\up, xy.x, \down, xy.y)
            },[0.1,0.1]).round_([0.01,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("gain",\boostcut,{ |f| synths[0].set(\gain, f.value.dbamp) },'horz',0)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("trim",\boostcut,{ |f| synths[0].set(\trim, f.value.dbamp) },'horz',0)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(45)  
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

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
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(45);

        controls.add(
            DragSink()
            .maxWidth_(45)
            .align_(\center).background_(Color.white).string_("in")
            .receiveDragHandler_({ |drag|
                var dragObject = View.currentDrag;

                if(dragObject.isInteger and: {dragObject < NSFW.numInBusses},{

                    drag.object_(dragObject);
                    drag.align_(\left).string_("in:" + dragObject.asString);
                    synths[0].set( \ampIn, NS_ServerHub.servers[strip.modGroup.server.name].inputBusses[dragObject] )
                },{
                    "drag Object not valid".warn
                })
            })
        );

        win.layout_(
            HLayout(
                VLayout(
                    VLayout( controls[0], assignButtons[0] ),
                    HLayout( controls[1], assignButtons[1] ),
                    HLayout( controls[2], assignButtons[2] ),
                ),
                VLayout( controls[5], controls[3], assignButtons[3], controls[4], assignButtons[4] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    saveExtra { |saveArray|
        saveArray.add([ controls[5].object ]);
        ^saveArray
    }

    loadExtra { |loadArray|
        var sink = controls[5];
        var val  = loadArray[0];

        if(val.notNil,{
            sink.object_(val);
            sink.align_(\left).string_("in:" + val.asString);
            synths[0].set( \ampIn, NS_ServerHub.servers[strip.modGroup.server.name].inputBusses[val] )
        })
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader(horizontal:true),
            OSC_Fader(horizontal:true),
            OSC_Panel( widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])
        ],randCol:true).oscString("EnvFollow")
    }
}
