NS_Repper : NS_SynthModule {
    var tapGroup, repGroup;
    var busses;

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(7);

        tapGroup = Group(modGroup);
        repGroup = Group(tapGroup, \addAfter);

        busses = (
            send:  Bus.audio(modGroup.server,1),           // sumBus
            dTime: Bus.control(modGroup.server,1).set(0.1),
            atk:   Bus.control(modGroup.server,1).set(0.01),
            rls:   Bus.control(modGroup.server,1).set(2),
            curve: Bus.control(modGroup.server,1).set(0),
            env:   Bus.control(modGroup.server,1).set(0),
            mix:   Bus.control(modGroup.server,1).set(0.5),
        );

        nsServer.addSynthDef(
            ("ns_repper" ++ numChans).asSymbol,
            {
                var sig = In.ar(\inBus.kr,1); // sum bus, only needs one channel
                var dTime = \dTime.kr(0.2) * Rand(0.75,1);
                var atk = \atk.kr(0.01);
                var rls = \rls.kr(2);
                var dur = (atk + rls) * Rand(0.75,1);
                var lineDown = XLine.kr(dTime, dTime * 2, dur );
                var lineUp = XLine.kr(dTime, dTime / 2,dur);
                var direction = Select.kr(\which.kr(0),[dTime, lineDown, lineUp]);
                sig = sig * Env([0,1,1,0],[0.01,0.98,0.01]).ar(gate:1, timeScale: dTime );

                sig = CombC.ar(sig,1,direction,inf);
                sig = LeakDC.ar(sig);
                sig = sig.tanh;
                sig = NS_Pan(sig, numChans, \pan.kr(0), numChans/4);
                sig = sig * Env.perc(atk,rls,1,\curve.kr(-2)).ar(2);

                Out.ar(\outBus.kr,sig * \mix.kr(0.5) )
            }
        );
      
        nsServer.addSynthDefCreateSynth(
            tapGroup,
            ("ns_repperTap" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                Out.ar(\sendBus.kr,sig.sum * numChans.reciprocal.sqrt );
                ReplaceOut.ar(\bus.kr,sig * (1 - \mix.kr(0.5) ));
            },
            [\bus, strip.stripBus, \sendBus, busses['send'], \mix, busses['mix'].asMap],
            { |synth|
                synths.add(synth);

                this.gateBool_(true);

                controls[0] = NS_Control(\dTime, ControlSpec(0.02,1,\exp), 0.1)
                .addAction(\synth,{ |c| busses['dTime'].set( c.value ) });

                controls[1] = NS_Control(\synth, ControlSpec(0,2,\lin,1), 0)
                .addAction(\synth,{ |c|
                    Synth(("ns_repper" ++ numChans).asSymbol,[
                        \inBus,  busses['send'],
                        \outBus, strip.stripBus,
                        \dTime,  busses['dTime'].getSynchronous,
                        \which,  c.value.asInteger,
                        \atk,    busses['atk'].getSynchronous,
                        \rls,    busses['rls'].getSynchronous,
                        \curve,  busses['curve'].getSynchronous,
                        \pan,    1.0.rand2,
                        \mix,    busses['mix'].asMap
                    ], repGroup)
                }, false);

                // these next two arguments are clunky, should also be on busses?
                controls[2] = NS_Control(\envDur, ControlSpec(2,8,\exp), 2)
                .addAction(\synth,{ |c| 
                    var envDur = c.value;
                    busses['env'].set(envDur);
                    if(controls[3].value == 0,{  // decay/swell
                        busses['atk'].value_(0.01);
                        busses['rls'].value_(envDur);
                        busses['curve'].value_(4.neg);
                    },{
                        busses['atk'].value_(envDur);
                        busses['rls'].value_(0.01);
                        busses['curve'].value_(4);
                    })
                });

                controls[3] = NS_Control(\decaySwell, ControlSpec(0,1,\lin,1), 0)
                .addAction(\synth,{ |c|  
                    var envDur = busses['env'].getSynchronous;
                    if(c.value == 0,{
                        busses['atk'].value_(0.01);
                        busses['rls'].value_(envDur);
                        busses['curve'].value_(4.neg);
                    },{
                        busses['atk'].value_(envDur);
                        busses['rls'].value_(0.01);
                        busses['curve'].value_(4);
                    })
                });

                controls[4] = NS_Control(\mix, ControlSpec(0,1,\lin),0.5)
                .addAction(\synth,{ |c| busses['mix'].set( c.value ) });

                { this.makeModuleWindow }.defer;
                loaded = true;
            } 
        )
    }

    makeModuleWindow {
        this.makeWindow("Repper",Rect(0,0,240,120));

        win.layout_(
            VLayout(
                NS_ControlFader(controls[0]),
                NS_ControlSwitch(controls[1], ["flat", "down", "up"], 3),
                NS_ControlFader(controls[2]),
                NS_ControlButton(controls[3], ["decay", "swell"], 2),
                NS_ControlFader(controls[4]),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra {
        this.gateBool_(false);
        tapGroup.free;
        repGroup.free;
        busses.do(_.free)
    }

    // this needs a rewrite
    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Panel([OSC_Fader(false), OSC_Button(width: "33%")], columns: 2),
            OSC_Switch(3, 3, height: "30%"),
            OSC_Fader(),
        ], randCol: true).oscString("Repper")
    }
}
