NS_YAWNGoldie : NS_SynthModule {
    var <netAddr;
    var ip = "127.0.0.1", port = "8000";

    // for safety's sake, only send messages to release loops and start/stop 
    // the playhead. Use instead marker actions within the REAPER session:
    // "!43102" to set loop points to current region
    // "!_SWS_SETREPEAT" to turn on looping

    init {
        var netAddr = NetAddr(ip, port.asInteger);

        this.initModuleArrays(8);
        
        controls[0] = NS_Control(\ip, \string, "127.0.0.1")
        .addAction(\synth,{ |c| 
            ip = c.value; 
            netAddr.disconnect;
            netAddr = NetAddr(ip, port.asInteger)
        });

        controls[1] = NS_Control(\port, \string, "8000")
        .addAction(\synth,{ |c| port = c.value; netAddr.port_(port.asInteger) });

        controls[2] = NS_Control(\improOneExit, ControlSpec(0, 1, 'lin', 1))
        .addAction(\synth,{ |c| 
            netAddr.sendMsg("/repeat", (1 - c.value).asInteger) 
        }, false);

        controls[3] = NS_Control(\elevenExit, ControlSpec(0, 1, 'lin', 1))
        .addAction(\synth,{ |c| 
            netAddr.sendMsg("/repeat", (1 - c.value).asInteger) 
        }, false);

        controls[4] = NS_Control(\improTwoExit, ControlSpec(0, 1, 'lin', 1))
        .addAction(\synth,{ |c| 
            netAddr.sendMsg("/repeat", (1 - c.value).asInteger)
        }, false);

        controls[5] = NS_Control(\cueTokamak, ControlSpec(0, 1, 'lin', 1))
        .addAction(\synth,{ |c|
            netAddr.sendMsg("/marker", 26)
        }, false);

        controls[6] = NS_Control(\tokamakExit, ControlSpec(0, 1, 'lin', 1))
        .addAction(\synth,{ |c|
            netAddr.sendMsg("/repeat", (1 - c.value).asInteger)
        }, false);

        controls[7] = NS_Control(\playPause, ControlSpec(0, 1, 'lin', 1))
        .addAction(\synth,{ |c| 
            netAddr.sendMsg("/play", c.value.asInteger)
        }, false);

        { this.makeModuleWindow }.defer;
        loaded = true;
    }

    makeModuleWindow {
        this.makeWindow("YAWN GOLDIE", Rect(0,0,180,120));

        win.layout_(
            VLayout(
                *[
                    HLayout(
                        NS_ControlText(controls[0]).maxHeight_(30),
                        NS_ControlText(controls[1]).maxHeight_(30)
                    )
                ] ++
                controls[2..].collect({ |ctrl|
                    NS_ControlButton(ctrl,[
                        [ctrl.label, NS_Style('textDark'), NS_Style('bGroundLight')],
                        [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')] 
                    ])
                })
            )
        );

        win.layout.spacing_(NS_Style('modSpacing')).margins_(NS_Style('modMargins'))
    }

    *oscFragment {       
        ^OpenStagePanel([
            OpenStageButton(label: "improOneExit"),
            OpenStageButton(label: "elevenExit"),
            OpenStageButton(label: "improTwoExit"),
            OpenStageButton(label: "cueTokamak"),
            OpenStageButton(label: "tokamakExit"),
            OpenStageButton('push', label: "play/pause")
        ], randCol: true).oscString("YAWNGoldie")
    }
}
