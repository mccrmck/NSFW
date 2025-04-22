NS_Last8 : NS_SynthModule {
    classvar <isSource = false;
    var buffer, busses;

    *initClass {
        ServerBoot.add{ |server|
            var numChans = NSFW.numChans(server);

            SynthDef(\ns_last8Rec,{
                var sig      = In.ar(\bus.kr,numChans);
                var rec      = RecordBuf.ar(sig,\bufnum.kr,run: \rec.kr(1));
                NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
            }).add;

            SynthDef(\ns_last8Play,{
                var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var trig     = \trig.tr();
                var rate     = BufRateScale.kr(bufnum) * \rate.kr(1);
                var pos      = Phasor.ar(TDelay.ar(T2A.ar(trig),0.02),rate,0,frames,\startPos.kr(0) * frames);
                var duckTime = SampleRate.ir * 0.02 * rate;
                var duck     = pos > (frames - duckTime);

                sig = BufRd.ar(numChans,bufnum,pos);
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0,duck + trig);

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add;
        }
    }

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        this.initModuleArrays(5);

        synths = Array.newClear(2);

        busses = (
            mixBus:  Bus.control(server, 1).set(1),
            rateBus: Bus.control(server, 1).set(1),
            posBus:  Bus.control(server, 1).set(0)
        );

        buffer = Buffer.alloc(server, server.sampleRate * 8, numChans);

        nsServer.addSynthDef(
            ("ns_last8Play" ++ numChans).asSymbol,
            {
                var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var trig     = \trig.tr();
                var rate     = BufRateScale.kr(bufnum) * \rate.kr(1);
                var pos      = Phasor.ar(
                    TDelay.ar(T2A.ar(trig),0.02),
                    rate,
                    0,
                    frames,
                    \startPos.kr(0) * frames
                );
                var duckTime = SampleRate.ir * 0.02 * rate;
                var duck     = pos > (frames - duckTime);

                sig = BufRd.ar(numChans,bufnum,pos);
                sig = sig * Env([1,0,1],[0.02,0.02]).ar(0, duck + trig);

                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }
        );

        nsServer.addSynthDefCreateSynth(
            modGroup,
            ("ns_last8Rec" ++ numChans).asSymbol,
            {
                var sig = In.ar(\bus.kr,numChans);
                var rec = RecordBuf.ar(sig,\bufnum.kr, run: \rec.kr(1));
                NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
            },
            [\bus, strip.stripBus, \bufnum, buffer],
            { |synth| synths.put(0, synth) }
        );

        controls[0] = NS_Control(\rate, ControlSpec(0.5,2,\exp), 1)
        .addAction(\synth,{ |c| busses['rateBus'].set( c.value ) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\pos, ControlSpec(0,1,\lin), 0)
        .addAction(\synth,{ |c| busses['posBus'].set( c.value ) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\trig, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| synths[1].set(\trig, c.value ) });
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(30);

        controls[3] = NS_Control(\mix, ControlSpec(0,1,\lin), 1)
        .addAction(\synth,{ |c| busses['mixBus'].set( c.value ) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| 
            var val = c.value.asInteger;
            if(val == 0,{
                synths[1].set(\gate,0); // needs an if(pause) condition
                synths[1] = nil
            },{
                synths[1] = Synth(("ns_last8Play" ++ numChans).asSymbol,[
                    \bufnum,   buffer,
                    \rate,     busses['rateBus'].asMap,
                    \startPos, busses['posBus'].asMap,
                    \mix,      busses['mixBus'].asMap,
                    \bus,      strip.stripBus
                ], modGroup, \addToTail)
            });
            this.gateBool_(val);
            synths[0].set(\rec, 1 - val);

        });
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(30);

        this.makeWindow("Last8", Rect(0,0,180,120));

        win.layout_(
            VLayout( 
                HLayout( NS_ControlFader(controls[0]),                   assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1]),                   assignButtons[1] ),
                HLayout( NS_ControlButton(controls[2], ["trig","trig"]), assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3]),                   assignButtons[3] ),
                HLayout( NS_ControlButton(controls[4], ["▶","bypass"]),  assignButtons[4] ),
            )
        );

        win.layout.spacing_(NS_Style.modSpacing).margins_(NS_Style.modMargins)
    }

    freeExtra {
        buffer.free;
        busses.do(_.free)
    }

    *oscFragment {       
        ^OSC_Panel([
            OSC_Fader(),
            OSC_Fader(),
            OSC_Button('push'),
            OSC_Panel([
                OSC_Fader(false),
                OSC_Button(width: "20%")
            ], columns: 2)
        ], randCol: true).oscString("Last8")
    }
}
