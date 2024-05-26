NS_MainWindow {
    var <>win;
    var <header, <pages, <outMixer, <outFX, <swapGrid, <moduleList;

    *new {
        ^super.new.init
    }

    init {
        var bounds = Window.availableBounds;
        var mainWidth = 1260;
        var moduleWidth = 180;
        var headerPanel, mainPanel, controlPanel, expandButton, modulePanel;

        win = Window("NSFW",bounds);
        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, Color.fromHexString("#7b14ba"));
        };

        headerPanel  = View(win).maxWidth_(mainWidth);
        mainPanel    = View(win).maxWidth_(mainWidth);
        controlPanel = View(win).maxWidth_(mainWidth).maxHeight_(180);
        modulePanel  = View(win).maxWidth_(moduleWidth).visible_(false).minWidth_(150);

        header       = NS_WindowHeader(headerPanel);
        pages        = 6.collect({ NS_PageView(mainPanel) });
        outMixer     = NS_MainOutMixer(controlPanel);
        swapGrid     = NS_SwapGrid(controlPanel);
        moduleList   = NS_ModuleList(modulePanel);
        expandButton = Button(modulePanel).maxHeight_(300).maxWidth_(15)
        .states_([["▶\n\n\n\n▶\n\n\n\n▶",Color.fromHexString("#7b14ba"),Color.black],["▶\n\n\n\n▶\n\n\n\n▶",Color.fromHexString("#7b14ba"),Color.black]])
        .action_({ |but|
            var val = but.value;
            if(val == 1,{
                modulePanel.visible_(true);
            },{
                modulePanel.visible_(false);
                win.view.resizeTo(mainWidth,bounds.height);
            })
        });

        win.layout_(
            HLayout(
                VLayout(
                    headerPanel.layout_(
                        HLayout( header )
                    ),
                    mainPanel.layout_(
                        GridLayout.rows(
                            pages[0..2],
                            pages[3..5]
                        ),
                    ),
                    controlPanel.layout_(
                        HLayout(  outMixer, swapGrid, expandButton )
                    )
                ),
                modulePanel.layout_(
                    VLayout( moduleList )
                )
            ),
        );

        win.layout.spacing_(0).margins_(0);
        headerPanel.layout.spacing_(0).margins_([8,12,8,2]);
        mainPanel.layout.spacing_(0).margins_([4,8,4,0]);
        controlPanel.layout.spacing_(0).margins_([8,0,8,8]);
        modulePanel.layout.spacing_(0).margins_([0,8,8,8]);
        win.view.maxWidth_(mainWidth);
        win.front;
    }
}

NS_WindowHeader {
    var <view;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|

        view = View(parent).layout_(
            HLayout(
                VLayout(
                    HLayout(
                        StaticText().string_("mono ins:").stringColor_(Color.white).maxWidth_(75),
                        HLayout( *8.collect({ |i| 
                            DragSource().object_(["Input",i]).string_(i).dragLabel_(i.asString)
                            .maxHeight_(30).maxWidth_(30).align_(\center) 
                        }))
                    ),
                    HLayout(
                        StaticText().string_("stereo ins:").stringColor_(Color.white).maxWidth_(75),
                        HLayout( *4.collect({ |i|
                            var chans = [i*2,i*2+1];
                            DragSource().object_(["Input",chans]).string_(i).dragLabel_("[%,%]".format(*chans))
                            .maxHeight_(30).maxWidth_(62).align_(\center)
                        }))
                    )
                ),
                Button(),
                Button(),
            )
        );

        view.layout.spacing_(2).margins_(0);
    }

    asView { ^view }
}

NS_PageView {
    var <view;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|
        view = View(parent).layout_(
            HLayout( 
                *4.collect({ |i| NS_StripView(view) })
            )
        );

        view.layout.spacing_(0).margins_([4,0]);
    }

    asView { ^view }
}

NS_StripView {
    var <view;
    var <inModule;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|
        var inSink = DragBoth().string_("in").align_(\center)
        .receiveDragHandler_({ |drag|
            var dragString = View.currentDrag[0].asString;
            var className = ("NS_" ++ dragString).asSymbol.asClass;
            if( className.respondsTo('isSource'),{
                if(className.isSource == true,{
                    inModule.free;
                    if(dragString == "Input",{
                    var inChans = View.currentDrag[1];
                        drag.string_(dragString ++ ": %".format(inChans) );
                        inModule = className.new(bus: inChans.asArray[0], numInChans: inChans.asArray.size )
                    },{
                        drag.string_(dragString);
                        inModule = className.new()
                    });
                })          
            })
        });

        view = View(parent).layout_(
            VLayout(
                HLayout(
                    inSink,
                    Button().maxWidth_(15).states_([["X", Color.black, Color.red]])
                    .action_({ |but|
                        inModule.free;
                        inSink.string_("in")
                    })
                ).margins_([0,4]),
                VLayout( *5.collect({ |i| NS_ModuleSink(view) }) ),
                NS_Fader(parent, nil,\amp,{ |f| f.value.postln }).maxHeight_(190),
                NS_AssignButton(),
                HLayout(
                    Button().states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]]), 
                    NS_AssignButton(),
                ),
                PopUpMenu().items_((0..3)),
            )
        );

        view.layout.spacing_(0).margins_(2);
    }

    asView { ^view }
}

NS_ModuleSink {
    var <view;
    var <module;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|
        var modSink;

        modSink = DragBoth().align_(\left)
        .receiveDragHandler_({ |drag|
            var dragString = View.currentDrag.asString;
            var className = ("NS_" ++ dragString).asSymbol.asClass;
            if( className.respondsTo('isSource'),{ 
                module.free;
                drag.string_(dragString);
                    module = className.new()
            })
        });

        view = View(parent).layout_( 
            HLayout(
                modSink,
                Button().maxHeight_(45).maxWidth_(15)
                .states_([["X", Color.black, Color.red],["X", Color.black, Color.red]])
                .action_({ |but|
                    module.free;
                    modSink.string_("")
                });
            )
        );

        view.layout.spacing_(0).margins_([0,2]);
    }

    asView { ^view }
}

NS_ModuleList {
    var <view;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|
        var path = "/Users/mikemccormick/Library/Application Support/SuperCollider/Extensions/NSFW/NS_Modules/";
        var moduleList = PathName(path).entries.collect({ |entry| 
            if(entry.isFile,{
                entry.fileNameWithoutExtension.split($_)[1]
            })
        });

        view = ScrollView(parent).canvas_(
            View()
            .background_(Color.fromHexString("#fcb314"))
            .layout_(
                VLayout(
                    *moduleList.collect({ |module| DragSource().object_(module) })
                )
            )
        );

        view.canvas.layout.spacing_(2).margins_(2);
    }

    asView { ^view }
}

NS_SwapGrid {
    var <view;
    var <buttons;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|

        buttons = 4.collect({ |column|
          var switch = NS_Switch(view,""!6,{ |switch|
                var index = switch.value.indexOf(1);
                6.do({ |page|
                    parent.parents.last // TopView
                    .children[1].children[page] // PageView
                    .children[column].background_(Color.clear)
                });
                parent.parents.last // TopView
                .children[1].children[index] // PageView
                .children[column].background_(Color.fromHexString("#fcb314"))
            }).maxWidth_(90);
            switch.buttons.do({ |but| but.maxHeight_(240)});
            switch
        });

        view = View(parent)
        .layout_(
            HLayout(
                *4.collect({ |i|
                    VLayout(
                        buttons[i],
                        NS_AssignButton(view).maxWidth_(45)
                    )
                })
            )
        );

        4.do({ |i| buttons[i].valueAction_(0) });
        buttons.do({ |switch| switch.layout.spacing_(2).margins_(2) });
        view.layout.spacing_(2).margins_([2,0]);
    }

    asView { ^view }
}

NS_MainOutMixer {
    var <view;

    *new { |parent|
        ^super.new.init(parent)
    }

    init { |parent|

        view = View(parent).layout_(
            HLayout(
                *4.collect({ |i|
                    VLayout(
                        StaticText().string_("out: %".format(i)).align_(\center).stringColor_(Color.white),
                        HLayout(
                            VLayout(
                                NS_ModuleSink(view),
                                NS_ModuleSink(view),
                                NS_ModuleSink(view),
                                NS_ModuleSink(view),
                                PopUpMenu().items_(["0-1","2-3","4-5","6-7"]),
                                HLayout(
                                    Button().states_([["M",Color.red,Color.black],["▶",Color.green,Color.black]]), 
                                    NS_AssignButton(),
                                ),
                            ),
                            VLayout( 
                                NS_Fader(view,nil,\db,{ |f| f.value.postln }).maxWidth_(45),
                                NS_AssignButton().maxWidth_(45)
                            )
                        )
                    ).margins_([2,0])
                })
            )
        );

        view.layout.spacing_(0).margins_(0);
    }

    asView { ^view }
}


//NS_Mixer
