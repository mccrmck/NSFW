NS_YAWNGoldie : NS_SynthModule {
    var <netAddr;
    var ip = "127.0.0.1", port = "8000";

    // for safety's sake, only send messages to release loops and start/stop 
    // the playhead. Use instead marker actions within the REAPER session:
    // "!43102" to set loop points to current region
    // "!_SWS_SETREPEAT" to turn on looping

    buildSynthModule {
        var netAddr = NetAddr(ip, port.asInteger);

        controlDict.addAll(
            NS_ControlString(\ip, "127.0.0.1")
            .addAction(\synth,{ |c| 
                ip = c.value; 
                netAddr.disconnect;
                netAddr = NetAddr(ip, port.asInteger)
            }),

            NS_ControlString(\port, "8000")
            .addAction(\synth,{ |c| 
                port = c.value; netAddr.port_(port.asInteger) 
            }),

            NS_ControlInt(\improOneExit, 0, 1, 0)
            .addAction(\synth,{ |c| 
                netAddr.sendMsg("/repeat", (1 - c.value)) 
            }, false),

            NS_ControlInt(\elevenExit, 0, 1, 0)
            .addAction(\synth,{ |c| 
                netAddr.sendMsg("/repeat", (1 - c.value)) 
            }, false),

            NS_ControlInt(\improTwoExit, 0, 1, 0)
            .addAction(\synth,{ |c| 
                netAddr.sendMsg("/repeat", (1 - c.value))
            }, false),

            NS_ControlInt(\cueTokamak, 0, 1, 0)
            .addAction(\synth,{ |c| netAddr.sendMsg("/marker", 26) }, false),

            NS_ControlInt(\tokamakExit, 0, 1, 0)
            .addAction(\synth,{ |c|
                netAddr.sendMsg("/repeat", (1 - c.value))
            }, false),

            NS_ControlInt(\playPause, 0, 1, 0)
            .addAction(\synth,{ |c| 
                netAddr.sendMsg("/play", c.value)
            }, false),
        );

        loaded = true;
    }

    nsModuleLayout {
        ^VLayout(
            HLayout(
                NS_ControlText(controlDict['ip']).maxHeight_(30),
                NS_ControlText(controlDict['port']).maxHeight_(30)
            ),
            NS_ControlButton(controlDict['improOneExit'], "improOneExit" ! 2),
            NS_ControlButton(controlDict['elevenExit'], "elevenExit" ! 2),
            NS_ControlButton(controlDict['improTwoExit'], "improTwoExit" ! 2),
            NS_ControlButton(controlDict['cueTokamak'], "cueTokamak" ! 2),
            NS_ControlButton(controlDict['playPause'], "playPause" ! 2),
        )
    }

    *oscFragment {       
        ^OpenStagePanel().widgetArray_([
            OpenStageButton().label_("improOneExit"),
            OpenStageButton().label_("elevenExit"),
            OpenStageButton().label_("improTwoExit"),
            OpenStageButton().label_("cueTokamak"),
            OpenStageButton().label_("tokamakExit"),
            OpenStageButton().mode_('tap').label_("play/pause")
        ]).randCol.label("YAWNGoldie")
    }
}
