NS_ModuleList {
  var <view;

  *new {
    ^super.new.init
  }

  init {
    var path = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "NS_Modules/";
    var folderNames = PathName(path).entries.collect({ |entry| entry.folderName });
    var modDrags = folderNames.collect({ |folder|
      PathName(path +/+ folder).entries.collect({ |entry| 
        if(entry.isFile,{
          var module = entry.fileNameWithoutExtension.split($_)[1];

          DragSource()
          .background_(Color.white)
          .object_([module])
          .dragLabel_(module)
          .string_(module)
        })
      })
    });

    var moduleLists = modDrags.collect({ |dragArray,index|
      var array = [
        StaticText()
        .string_(folderNames[index])
        .stringColor_(Color.white)
        .maxHeight_(30)
        .align_(\center)
      ] ++ dragArray;
      View().layout_( VLayout( *array ).spacing_(2).margins_(0)).visible_(false) 
    });

    var checks = folderNames.collect({ |name, index|
      CheckBox()
      .action_({ |cb|
        moduleLists[index].visible_(cb.value)
      })
    });

    var checkViews = folderNames.collect({ |name, index|
      View().layout_(
        HLayout(
          checks[index],
          StaticText()
          .string_(name)
          .align_(\left)
          .stringColor_(Color.white)
        )
      )
    });

    var listView = View().layout_(
      VLayout( *moduleLists ).margins_(0)
    );

    var scroller = Slider()
    .maxWidth_(15).thumbSize_(15)
    .background_(Color.clear).knobColor_(Color.clear)
    .value_(1)
    .action_({ |sl|
      var val = sl.value;
      var bounds = listView.bounds;
      val = (1 - val) * bounds.height;
      listView.moveTo(bounds.left,val.neg)
    });

    view = View()
    //.background_(Color.fromHexString("#fcb314"))
    .background_(Color.white.alpha_(0.15))
    .layout_(
      VLayout(
        GridLayout.rows( *checkViews.clump(2) ),
        View().layout_(
          HLayout(
            listView,
            scroller,
          )
        )
      )
    );

    checks[ folderNames.collect({ |name| name.asSymbol }).indexOf('main') ].valueAction_(true);
    view.layout.spacing_(2).margins_(2);
  }

  asView { ^view }
}
