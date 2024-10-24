NS_RingModIn : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_ringModIn,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var mod = In.ar(\modIn.kr,numChans);
                sig = sig * \carAmp.kr(1);
                mod = mod * \modAmp.kr(1);
                sig = sig * mod;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add
        }
    }

    init {
        this.initModuleArrays(3);
        this.makeWindow("RingModIn", Rect(0,0,240,240));

        synths.add( Synth(\ns_ringModIn,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("carAmp",ControlSpec(0.1,100,\exp),"modAmp",ControlSpec(0.1,100,\exp),{ |xy| 
                synths[0].set(\carAmp,xy.x, \modAmp, xy.y);
            },[1,1]).round_([0.1,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal:1).maxWidth_(45)
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

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
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        controls.add(
            DragSink()
            .maxWidth_(45)
            .align_(\center).background_(Color.white).string_("in")
            .receiveDragHandler_({ |drag|
                var dragObject = View.currentDrag;

                if(dragObject.isInteger and: {dragObject < NSFW.numInBusses},{

                    drag.object_(dragObject);
                    drag.align_(\left).string_("in:" + dragObject.asString);
                    synths[0].set( \modIn, NS_ServerHub.servers[strip.modGroup.server.name].inputBusses[dragObject] )
                },{
                    "drag Object not valid".warn
                })
            })
        );

        win.layout_(
            HLayout(
                VLayout( controls[0], assignButtons[0] ),
                VLayout( controls[3], controls[1], assignButtons[1], controls[2], assignButtons[2] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Panel("15%",horizontal:false,widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
            ])
        ],randCol:true).oscString("RingModIn")
    }
}
