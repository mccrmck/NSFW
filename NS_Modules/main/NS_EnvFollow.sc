NS_EnvFollow : NS_SynthModule {
    classvar <isSource = false;
    var dragSink;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(7);
       
        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_envFollow" ++ numChans).asSymbol,
            {
                var sr = SampleRate.ir;
                var sig = In.ar(\bus.kr, numChans);
                var amp = In.ar(\ampIn.kr, numChans).sum * numChans.reciprocal.sqrt;
                amp = amp * \gain.kr(1);
                amp = FluidLoudness.kr(
                    amp,
                    [\loudness],
                    windowSize: sr * 0.4,
                    hopSize: sr * 0.1
                ).dbamp;

                sig = sig * LagUD.kr(amp,\up.kr(0.1),\down.kr(0.1));
                sig = sig * \trim.kr(1);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
            [\bus, strip.stripBus],
            { |synth| 
                synths.add(synth);

                controls[0] = NS_Control(\atk, ControlSpec(0.01,1,\exp), 0.1)
                .addAction(\synth, { |c| synths[0].set(\up, c.value) });

                controls[1] = NS_Control(\rls, ControlSpec(0.01,2,\exp), 0.1)
                .addAction(\synth, { |c| synths[0].set(\down, c.value) });

                controls[2] = NS_Control(\gain, \boostcut, 0)
                .addAction(\synth, { |c| synths[0].set(\gain, c.value.dbamp) });

                controls[3] = NS_Control(\trim, \boostcut, 0)
                .addAction(\synth, { |c| synths[0].set(\trim, c.value.dbamp) });

                controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
                .addAction(\synth,{ |c| synths[0].set(\mix, c.value) });

                controls[5] = NS_Control(\sidechain,\string,"in")
                .addAction(\synth,{ |c|

                    //synths[0].set( \ampIn, nsServer.inputBusses[dragObject] )
                },false);

                controls[6] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c| this.gateBool_(c.value); synths[0].set(\thru, c.value) });

                { this.makeModuleWindow }.defer;
                loaded = true;
            }
        )
    }

    makeModuleWindow {
        this.makeWindow("EnvFollow", Rect(0,0,180,150));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlFader(controls[1]),
                NS_ControlFader(controls[2]),
                NS_ControlFader(controls[3]),
                NS_ControlFader(controls[4]),
                NS_ControlSink(controls[5]),
                NS_ControlButton(controls[6], ["▶","bypass"]), 
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    saveExtra { |saveArray|
        saveArray.add([ dragSink.object ]);
        ^saveArray
    }

    loadExtra { |loadArray|
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var val      = loadArray[0];

        if(val.notNil,{
            dragSink.object_(val);
            dragSink.align_(\left).string_("in:" + val.asString);
            synths[0].set( \ampIn, nsServer.options.inputBusses[val] )
        })
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "20%")], columns: 2)
        ], randCol: true).oscString("EnvFollow")
    }
}
