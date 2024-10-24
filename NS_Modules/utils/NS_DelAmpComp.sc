NS_DelAmpComp : NS_SynthModule {
    classvar <isSource = false;
    var delBus, ampBus;
    var numBoxes;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_delAmpComp,{
                var numChans = NSFW.numChans;
                var sig      = In.ar(\bus.kr,numChans);
              
                sig = DelayL.ar(sig,0.2,In.kr(\delay.kr(),numChans));
                sig = sig * In.kr(\ampComp.kr(),numChans);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            }).add;
        }
    }

    init {
        var chans = NSFW.numChans;
        this.initModuleArrays(chans * 2 + 1);
        this.makeWindow("DelAmpComp", Rect(0,0,420,600));

        delBus = Bus.control(modGroup.server,chans).value_(0);
        ampBus = Bus.control(modGroup.server,chans).value_(1);

        synths.add( Synth(\ns_delAmpComp,[\delay, delBus, \ampComp, ampBus, \bus,bus],modGroup) );


        chans.do({ |channel|
            controls.add(
                NS_Fader(channel,ControlSpec(0,0.1,'lin'),{ |f| delBus.subBus(channel).set( f.value ) },'horz',0).round_(0.001)
            );
            assignButtons[channel * 2] = NS_AssignButton(this, channel * 2, \fader).maxWidth_(45);

            controls.add(
                NS_Fader(channel,\db,{ |f| ampBus.subBus(channel).set( f.value.dbamp ) },'horz',0)
            );
            assignButtons[channel * 2 + 1] = NS_AssignButton(this, channel * 2 + 1, \fader).maxWidth_(45);
        });

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\thru, val)
            })
        );
        assignButtons[chans * 2] = NS_AssignButton(this, chans * 2, \button).maxWidth_(45);

        numBoxes = chans.collect({ |channel|
            NumberBox()
            .minWidth_(60)
            .align_(\center)
            .decimals_(3)
            .action_({ |nb|
                var meters = nb.value.clip(0,34.3);
                var milliSecs = meters/343;

                controls[channel * 2].valueAction_(milliSecs)
            })
        });

        win.layout_(
            VLayout(
                GridLayout.columns(
                    [ StaticText().string_("delay") ] ++ chans.collect({ |i| HLayout( controls[i * 2], assignButtons[i * 2] ) }),
                    [ StaticText().string_("m").align_(\center)] ++ chans.collect({ |i| numBoxes[i] }),
                    [ StaticText().string_("amp") ] ++ chans.collect({ |i| HLayout( controls[i * 2 + 1], assignButtons[i * 2 + 1] ) }),
                ),
                HLayout(controls[chans * 2], assignButtons[chans * 2]),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        delBus.free;
        ampBus.free;
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_Button()
        ],randCol:true).oscString("DelAmpComp")
    }
}
