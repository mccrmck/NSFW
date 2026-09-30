NS_BTrack : NS_SynthModule {
    var buffer;
    var bufCued = false;

    buildSynthModule {

        nsServer.addSynthDef(
            ("ns_bTrack" ++ numChans).asSymbol,
            {
                var bufnum = \bufnum.kr;
                var sig = VDiskIn.ar(numChans, bufnum, sendID: 1);
                sig = NS_Envs(sig, \gate.kr(1), \pauseGate.kr(1), \amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
            },
        );

        controlDict.addAll(
            NS_ControlString(\buffer, "")
            .addAction_(\synth, { |c|
                var path = c.value;
                buffer.free;

                if(path > 0) {
                    buffer = Buffer.cueSoundFile(
                        nsServer.server, path, 0, 2 ** 15, { bufCued = true }
                    );
                }
            }, false),

            NS_ControlFloat(\mix, ControlSpec(0, 1), 1)
            .addAction(\synth,{ |c| synths[0].set(\mix, c.value) }),

            NS_ControlInt(\bypass, 0, 1, 0)
            .addAction(\synth,{ |c| 
                this.gateBool_(c.value);
                synths[0].set(\thru, c.value)
            })
        )
    }

    // OSCFunc that should update the NS_Waveform
    addResponder {
        // only add responder if modView.visible == true
        // ...but because this is on the main Window, add it every time

    }
    removeResponder {}

    freeExtra {
        buffer.free

    }

    nsModuleLayout {
        ^HLayout(
            NS_Waveform(),
            VLayout(
                NS_Button(["load file"]),
                NS_ControlText(controlDict['buffer']),
                NS_Button([NS_Style('play'), NS_Style('stop')]),
            )
        )
    }

    //*oscFragment {}
}
