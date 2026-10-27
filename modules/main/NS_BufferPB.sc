NS_BufferPB : NS_SynthModule{
    var buffer;

    buildSynthModule {
        var synthName = ("ns_bufferPBmono" ++ numChans).asSymbol;
        var launchSynth = {
            synths.put(0, 
                Synth(synthName, [
                    \bus, modBus, 
                    \bufnum, buffer, 
                    \start, controlDict['start'].value,
                    \dur, controlDict['\remainDur'].value,
                    \rate, controlDict['rate'].value,
                ], modGroup, \addToTail)
            )
        };

        synths = Array.newClear(1);

        nsServer.addSynthDef(
            synthName,
            {
                var bufnum = \bufnum.kr;
                var frames = BufFrames.kr(bufnum) - 1;
                var start  = \start.kr(0) * frames;
                var rate   = BufRateScale.kr(bufnum) * \rate.kr(1);
                var end    = (start + (\dur.kr(1) * frames)).clip(0, frames);
                var pos    = Phasor.ar(DC.ar(0) + \trig.tr, rate, start, end, start);
                var sig    = BufRd.ar(1, bufnum, pos);
                var gate   = pos > (end - (SampleRate.ir * 0.02 * rate));
                sig = sig * Env([1, 0, 1], [0.02, 0.02]).ar(0, gate + \trig.tr);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            },
            // action here?
        );

        controlDict.addAll(
            NS_ControlString(\buffer, "")
            .addAction(\synth, { |c|
                buffer.free;

                if(c.value.size > 0,{

                    // could add some logic here to accommodate channel counts >=2
                    buffer = Buffer.readChannel(
                        nsServer.server, c.value, channels: [0], 
                        action: { |buf|
                            synths[0] !? 
                            { synths[0].set(\bufnum, buf, \trig, 1) } ??
                            {
                                if(controlDict['bypass'].value == 1){
                                    launchSynth.()
                                }
                            }
                        }
                    );
                })
            }, false),

            NS_ControlFloat(\start, ControlSpec(0, 0.99), 0)
            .addAction(\synth, { |c| synths[0].set(\start, c.value) }),

            NS_ControlFloat(\remainDur, ControlSpec(0.01, 1, \exp), 1)
            .addAction(\synth, { |c| synths[0].set(\dur, c.value) }),

            NS_ControlFloat(\rate, ControlSpec(0.25, 4, \exp), 1)
            .addAction(\synth, { |c| synths[0].set(\rate, c.value) }),

            NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
            .addAction(\synth, { |c| synths[0].set(\mix, c.value) }),

            NS_ControlInt(\bypass, 0, 1, 0)
            .addAction(\synth, { |c| 
                this.gateBool_(c.value); 

                if(c.value == 1)
                { buffer !? { launchSynth.() } } 
                { synths[0] !? { synths[0].set(\gate, 0); synths[0] = nil } }
            }),
        );

        loaded = true;
    }

    nsModuleLayout {
        ^VLayout(
            NS_Waveform(controlDict['buffer'], controlDict['start']),
            NS_ControlFader(controlDict['start']),
            NS_ControlFader(controlDict['remainDur']),
            NS_ControlFader(controlDict['rate']),
            NS_ControlFader(controlDict['mix']),
            NS_ControlButton.bypass(controlDict['bypass']),
        )
    }

    free { super.free; buffer.free }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageFader(),
            OpenStageFader(),
            OpenStageFader(),
            OpenStagePanel().widgetArray_([
                OpenStageFader().snap_(false), 
                OpenStageButton().width_("20%")
            ]).columns_(2)
        ]).randCol.label_("BufferPB")
    }
}
