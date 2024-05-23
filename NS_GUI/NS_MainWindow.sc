NS_MainWindow {
  var <>win;
  var <pages;

  *new {
    ^super.new.init
  }

  init {
    var bounds = Window.availableBounds;
    var path = "/Users/mikemccormick/Library/Application Support/SuperCollider/Extensions/NSFW/NS_Modules/";
    var moduleList = PathName(path).entries.collect({ |entry| 
      entry.fileNameWithoutExtension.split($_)[1]
    });
    var moduleDrags = moduleList.collect({ |module| DragSource().object_(module) });

    var inputText = StaticText().string_("ins:").maxWidth_(30);
    var inputDrags = 8.collect({ |i| DragSource().object_(i).maxHeight_(30).maxWidth_(30).align_(\center) });
    var saveBut = Button();
    var loadBut = Button();

    var inMixer = View(win)
    .background_(Color.rand)
    .maxHeight_(270)
    .layout_(
      HLayout(
        *8.collect({ |i|
          VLayout(
            NS_Fader(inMixer, "in: %".format(i),\amp,{ |f| f.value.postln }),
            NS_AssignButton()
          )
        })
      )
    );

    var outMixer = View(win)
    .background_(Color.rand)
    .maxHeight_(270)
    .layout_(
      HLayout(
        *8.collect({ |i|
          VLayout(
          DragSink(),
            NS_Fader(outMixer, "out: %".format(i),\amp,{ |f| f.value.postln }),
            NS_AssignButton(),
            PopUpMenu().items_((0..7))
          )
        })
      )
    );

    win = Window("NSFW",bounds.width_(bounds.width * 0.75));

    pages = 6.collect({ NS_PageView(win) });

    win.layout_(
      HLayout(
        View(win)
        .maxWidth_(1170)
        .background_(Color.rand)
        .layout_(
          VLayout(
            HLayout(inputText, *(inputDrags ++ saveBut ++ loadBut) ),
            GridLayout.rows(
              pages[0..2],
              pages[3..5]
            ),
            HLayout(
              inMixer,
              outMixer,
            )
          )
        ),
        ScrollView()
        .background_(Color.rand)
        .maxWidth_(360)
        .layout_(
          VLayout(
            *moduleDrags
          )
        )
      )
  );

    win.layout.spacing_(0).margins_(0!4);
    win.front;
  }
}

NS_PageView {
  var <pageNum;
  var <view;

  *new { |parent, pageNum|
    ^super.newCopyArgs(pageNum).init(parent)
  }

  init { |parent|
    view = View(parent)
    .maxWidth_(420)
    .layout_(
      VLayout(
        HLayout( 
          *4.collect({ |i| NS_StripView(view) })
        ),
        DragBoth().object_("Page:" + pageNum).align_(\center)
      )
    );

    view.layout.spacing_(0).margins_(2!4);
  }

  asView { ^view }
}

NS_StripView {
  var <view;

  *new { |parent|
    ^super.new.init(parent)
  }

  init { |parent|

    view = View(parent)
    .layout_(
      VLayout(
        DragBoth().string_("in").align_(\center),
        VLayout( *5.collect({ |i| NS_ModuleSink(view) }) ),
        NS_Fader(parent, nil,\amp,{ |f| f.value.postln }).maxHeight_(120),
        NS_AssignButton(),
        HLayout(
          Button().states_([["M"],["U"]]), 
          NS_AssignButton(),
        )
      )
    );

    view.layout.spacing_(0).margins_(2!4);
    }

  asView { ^view }
}

NS_ModuleSink {
  var <view;

  *new { |parent|
    ^super.new.init(parent)
  }

  init { |parent|

    view = View(parent)
    .layout_(
      HLayout(
        DragSink().maxHeight_(240).align_(\left).mouseDownAction_({ |a| a.postln }),
        Button().maxWidth_(15).states_([["X", Color.black, Color.red]])
      )
    );

    view.layout.spacing_(0).margins_(2!4);
  }

  asView { ^view }
}
