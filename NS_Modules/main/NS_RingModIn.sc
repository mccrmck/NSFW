NS_RingModIn : NS_SynthModule {
    classvar <isSource = false;
    var dragSink;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(5);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_ringModIn" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr, numChans);
                var mod = In.ar(\modIn.kr, numChans);
                sig = sig * \carAmp.kr(1);
                mod = mod * \modAmp.kr(1);
                sig = sig * mod * \trim.kr(1);
                sig = sig.tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| synths.add(synth) }
        );

        controls[0] = NS_Control(\carAmp,\amp,1)
        .addAction(\synth,{ |c| synths[0].set(\carAmp, c.value) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\modAmp,\amp,1)
        .addAction(\synth,{ |c| synths[0].set(\modAmp, c.value) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\trim,\boostcut,0)
        .addAction(\synth,{ |c| synths[0].set(\trim, c.value.dbamp) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(30);

        dragSink = DragSink()
        .align_(\center)
        .background_(Color.white).string_("in")
        .receiveDragHandler_({ |drag|
            var dragObject = View.currentDrag;

            if(dragObject.isInteger and: {dragObject < nsServer.options.inChannels},{

                drag.object_(dragObject);
                drag.align_(\left).string_("in:" + dragObject.asString);
                synths[0].set(\modIn, nsServer.inputBusses[dragObject] )
            },{
                "drag Object not valid".warn
            })
        });

        this.makeWindow("RingModIn", Rect(0,0,240,150));

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0]),                            assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]),                            assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2]),                            assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]),                            assignButtons[3] ),
                HLayout( dragSink, NS_ControlButton(controls[4], ["▶","bypass"]), assignButtons[4] ),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
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
            synths[0].set( \modIn, NSFW.servers[strip.modGroup.server.name].inputBusses[val] )
        })
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2)
        ], randCol: true).oscString("RingModIn")
    }
}
