NS_ServerHub {
  classvar win;
  classvar moduleList, inModules, inView;

  *makeWindow {
    var numIns = 8;
    var visModuleIndex = 0;
    var bounds = Window.availableBounds;
    var gradient = Color.rand; /*Color.fromHexString("#7b14ba")*/
    win = Window("NSFW Server Hub", Rect(bounds.width - 300,bounds.height,700, 375));
    win.drawFunc = {
      Pen.addRect(win.view.bounds);
      Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
    };

    inModules  = numIns.collect({ |i| 
      View()
      .background_(Color.white.alpha_(0.15))
      .layout_(
        VLayout(
          HLayout(
            StaticText().string_("inBus:").stringColor_(Color.white),
            DragSource().align_(\center).string_(i).background_(Color.white).maxWidth_(45), 
            StaticText().string_("outSend:").stringColor_(Color.white),
            NumberBox().align_(\center).maxWidth_(45)
          ),
          NS_InputModule()
        )
      ).visible_( i < 2 )
    });

    moduleList = NS_ModuleList();

    inView = View().layout_(
      HLayout(
        GridLayout.rows(
          *inModules.clump(2)
        ),
        VLayout(
          Button()
          .maxWidth_(15)
          .maxHeight_(300)
          .states_([["↑"]])
          .action_({ |but|
            inModules[visModuleIndex * 2].visible_(false);
            inModules[visModuleIndex * 2 + 1].visible_(false);
            visModuleIndex = (visModuleIndex - 1).clip(0, (numIns / 2) - 1);
            inModules[visModuleIndex * 2].visible_(true);
            inModules[visModuleIndex * 2 + 1].visible_(true);
          }),
          Button()
          .maxWidth_(15)
          .maxHeight_(300)
          .states_([["↓"]])
          .action_({ |but|
            inModules[visModuleIndex * 2].visible_(false);
            inModules[visModuleIndex * 2 + 1].visible_(false);
            visModuleIndex = (visModuleIndex + 1).clip(0, (numIns / 2) - 1);
            inModules[visModuleIndex * 2].visible_(true);
            inModules[visModuleIndex * 2 + 1].visible_(true);
          }),
        )
      ).margins_(0)
    );

    win.layout_(
      HLayout(
        moduleList,
        VLayout(
          HLayout(
            Button()
            .states_([["show module list"],["show module list"]])
            .value_(1)
            .action_({ |but| 
              if(but.value == 0,{
                moduleList.view.visible_(false)
              },{
                moduleList.view.visible_(true)
              })
            }),
            Button().states_([["save setup"]]),
            Button().states_([["load setup"]]),
            Button().states_([["recording maybe?"]]),
          ),
          HLayout( NS_Switch(["nsfw_0","nsfw_1","active","servers"],{ |switch| },'horz'), NS_AssignButton() ),
          inView
        )
      )
    );

    win.front

  }
}

NS_InputModule : NS_SynthModule {
  var <view;
  var <rms, localResponder;

  *initClass {
    StartUp.add{
      SynthDef(\ns_inputMono,{
        var sig = SoundIn.ar(\inBus.kr());

        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\inAmp.kr(0));

        sig = Squish.ar(sig,sig,\dbThresh.kr(-12), \compAtk.kr(0.01), \compRls.kr(0.1), \ratio.kr(2), \knee.kr(0.01),\dbMakeUp.kr(0));
        SendPeakRMS.ar(sig,10,3,'/inSynth',0);

        Out.ar(\outBus.kr, sig!2 )
      }).add;

      SynthDef(\ns_inputStereo,{
        var inBus = \inBus.kr();
        var sig = SoundIn.ar([inBus,inBus + 1]);

        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\inAmp.kr(0));

        sig = Squish.ar(sig,sig.sum * -3.dbamp,\dbThresh.kr(-12), \compAtk.kr(0.01), \compRls.kr(0.1), \ratio.kr(2), \knee.kr(0.01),\dbMakeUp.kr(0));
        SendPeakRMS.ar(sig.sum * -3.dbamp,10,3,'/inSynth',0);

        Out.ar(\outBus.kr, sig )
      }).add;
    }
  }

  init {
    this.initModuleArrays(7);

    localResponder.free;
    // localResponder = OSCFunc({ |msg|

    //     if( msg[2] == 0,{
    //         { 
    //             rms[0].value = msg[4].ampdb.linlin(-80, 0, 0, 1);
    //             rms[0].peakLevel = msg[3].ampdb.linlin(-80, 0, 0, 1,\min)
    //         }.defer
    //     })
    // }, '/inSynth', argTemplate: [synths[0].nodeID]);

    controls.add(
      NS_Fader("inAmp",\db,{ |f| synths[0].set(\inAmp, f.value.dbamp) },'horz',-inf).round_(0.1).stringColor_(Color.white).maxWidth_(300)
    );
    assignButtons[0] = NS_AssignButton().maxWidth_(45);

    // compressor section
    controls.add(
      NS_Fader("thresh",\db,{ |f| synths[0].set(\dbThresh, f.value) },'horz',initVal: -12).round_(0.1).stringColor_(Color.white).maxWidth_(300)
    );
    assignButtons[1] = NS_AssignButton().maxWidth_(45);

    controls.add(
      NS_Knob("atk",ControlSpec(0.001,0.25),{ |k| synths[0].set(\compAtk, k.value) },false,0.01).round_(0.01).maxWidth_(45).stringColor_(Color.white)
    );
    assignButtons[2] = NS_AssignButton().maxWidth_(45);

    controls.add(
      NS_Knob("rls",ControlSpec(0.001,0.25),{ |k| synths[0].set(\compRls, k.value) },false,0.1).round_(0.01).maxWidth_(45).stringColor_(Color.white)
    );
    assignButtons[3] = NS_AssignButton().maxWidth_(45);

    controls.add(
      NS_Knob("ratio",ControlSpec(1,8,\lin),{ |k| synths[0].set(\ratio, k.value) },false,2).round_(0.1).maxWidth_(45).stringColor_(Color.white)
    );
    assignButtons[4] = NS_AssignButton().maxWidth_(45);

    controls.add(
      NS_Knob("knee",ControlSpec(0,1,\lin),{ |k| synths[0].set(\knee, k.value) },false,0.01).round_(0.01).maxWidth_(45).stringColor_(Color.white)
    );
    assignButtons[5] = NS_AssignButton().maxWidth_(45);

    controls.add(
      NS_Fader("trim",\boostcut,{ |k| synths[0].set(\dbMakeUp, k.value) },'horz').round_(0.1).stringColor_(Color.white).maxWidth_(300)
    );
    assignButtons[6] = NS_AssignButton().maxWidth_(45);

    rms = 2.collect({
      LevelIndicator().minWidth_(15).style_(\led).numTicks_(11).numMajorTicks_(3)
      .stepWidth_(2).drawsPeak_(true).warning_(0.9).critical_(1.0)
    });

    view = View().layout_(
      HLayout(
        rms[0],
        VLayout( 
          // rms[0]
          HLayout( controls[0], assignButtons[0] ),
          HLayout( controls[1], assignButtons[1] ),
          HLayout( controls[2], controls[3], controls[4], controls[5] ),
          HLayout( assignButtons[2], assignButtons[3], assignButtons[4], assignButtons[5] ),
          HLayout( controls[6], assignButtons[6] ),
        )
      )
    );

    view.layout.spacing_(2).margins_(0);
  }

  //addMonoInput { |nsServer| synths.add( Synth(\ns_inputMono,[\inBus, ,\outBus,bus],nsServer.inGroup) ) } // which busses? 
  //addStereoInput { |nsServer| synths.add( Synth(\ns_inputStereo,[\inBus, ,\outBus,bus],nsServer.inGroup) ) } // which busses? 
  // must set busses for ChannelStrips also...


  asView { ^view }

}
