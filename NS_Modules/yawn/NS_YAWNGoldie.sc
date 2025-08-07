NS_YAWNGoldie : NS_SynthModule {

    init {
        var netAddr = NetAddr("127.0.0.1",9999);

        this.initModuleArrays(5);
       
        5.do({ |i|
            controls[i] = NS_Control("cue" ++ i, ControlSpec(0,1,'lin',1))
            .addAction(\cue,{ |c|
                netAddr.sendMsg("/test", c.value)
            }, false)
        });

        { this.makeModuleWindow }.defer;
        loaded = true;
    }

    makeModuleWindow {
        this.makeWindow("YAWN GOLDIE", Rect(0,0,180,120));

        win.layout_(
            VLayout(
                *controls.collect({ |ctrl|
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
        ^OpenStagePanel({ OpenStageButton() } ! 5, randCol: true).oscString("YAWNGoldie")
    }
}
