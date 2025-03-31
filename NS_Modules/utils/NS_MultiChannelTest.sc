//NS_MultiChannelTest : NS_SynthModule {
//    classvar <isSource = true;
//    var numBox, currentChan = 0;
//
//    *initClass {
//        ServerBoot.add{ |server|
//                var numChans = NSFW.numChans(server);
//            SynthDef(\ns_multiChannelTest,{
//                var sig = PinkNoise.ar();
//                var pan = SelectX.kr(\which.kr(1),[ LFSaw.kr(\rate.kr(0.05)).range(0,2), \chan.kr(0) ]);
//                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
//                sig = NS_Pan(sig, numChans, pan, 1, 0);
//                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0))
//            }).add
//        }
//    }
//
//    init {
//        this.initModuleArrays(4);
//        this.makeWindow("MultiChannelTest", Rect(0,0,240,90));
//
//        synths.add(Synth(\ns_multiChannelTest,[\bus,bus],modGroup));
//
//        numBox = NumberBox()
//        .string_("0")
//        .align_(\center)
//        .action_({ |nb|
//            synths[0].set(\which,1,\chan,(nb.value * 2) / NSFW.numChans(modGroup.server))
//        });
//
//        controls.add(
//            Button()
//            .maxWidth_(45)
//            .states_([["<",Color.black,Color.white]])
//            .action_({
//                var val = (numBox.value - 1).asFloat.wrap(0,NSFW.numChans(modGroup.server));
//                numBox.valueAction = val
//            })
//        );
//        assignButtons[0] = NS_AssignButton(this, 0, \button).maxWidth_(45);
//
//        controls.add(
//            Button()
//            .maxWidth_(45)
//            .states_([[">",Color.black,Color.white]])
//            .action_({
//                var val = (numBox.value + 1).asFloat.wrap(0,NSFW.numChans(modGroup.server));
//                numBox.valueAction = val
//            })
//        );
//        assignButtons[1] = NS_AssignButton(this, 1, \button).maxWidth_(45);
//
//
//        controls.add(
//            NS_Fader("rate",ControlSpec(0.01,0.1,'lin'),{ |f| synths[0].set(\which,0,\rate, f.value) },'horz',0.05)
//        );
//        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);
//
//        controls.add(
//            Button()
//            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
//            .action_({ |but|
//                var val = but.value;
//                strip.inSynthGate_(val);
//                synths[0].set(\thru, val)
//            })
//        );
//        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);
//
//        win.layout_(
//            VLayout(
//                HLayout( assignButtons[0], controls[0], numBox, controls[1], assignButtons[1]  ),
//                HLayout( controls[2], assignButtons[2] ),
//                HLayout( controls[3], assignButtons[3] ),
//            )
//        );
//
//        win.layout.spacing_(2).margins_(4)
//    }
//
//    *oscFragment {       
//        ^OSC_Panel([
//            OSC_Panel({ OSC_Button() } ! 2),
//            OSC_Fader(false),
//            OSC_Button()
//        ],randCol: true).oscString("MCTest")
//    }
//}
