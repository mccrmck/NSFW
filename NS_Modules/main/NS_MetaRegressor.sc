NS_MetaRegressor : NS_SynthModule {
    classvar <isSource = false;

    *initClass {
        ServerBoot.add{

        }
    }

    init {
        this.initModuleArrays(4);
        this.makeWindow("RingMod", Rect(0,0,400,400));

       // synths.add( Synth(\ns_ringMod,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("A",ControlSpec(0,1,'lin'),"A",ControlSpec(0,1,'lin'),{ |xy| 
            },[0.5,0.5]).round_([0.01,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("B",ControlSpec(0,1,'lin'),"B",ControlSpec(0,1,'lin'),{ |xy| 
            },[0.5,0.5]).round_([0.01,0.01])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        win.layout_(
            HLayout(
                VLayout( 
                    controls[0], assignButtons[0], 
                    controls[1], assignButtons[1],
                ),
                VLayout(
                    NS_Switch((0..7),{}), NS_AssignButton(),
                    Button().states_([["randomize"]]), Button().states_([["add point"]]),
                    Button().states_([["train"]]), Button().states_([["not predicting"],["predicting"]])   
                ),
                VLayout(
                    *16.collect{DragSink().minWidth_(90)},
                ).spacing_(0).margins_(0),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    // *oscFragment {       
    //     ^OSC_Panel(widgetArray:[
    //         OSC_XY(snap:true),
    //         OSC_Fader("15%",snap:true),
    //         OSC_Panel("15%",horizontal:false,widgetArray: [
   //             OSC_Fader(),
   //             OSC_Button(height:"20%")
   //         ])
   //     ],randCol:true).oscString("RingMod")
   // }
}
