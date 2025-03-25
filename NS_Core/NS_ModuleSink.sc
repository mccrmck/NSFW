NS_ModuleSink {
    var <strip, <slotIndex;
    var <view, <modSink, <guiButton;
    var <>module;

    *new { |channelStrip, sinkIndex|
        ^super.newCopyArgs(channelStrip, sinkIndex).init
    }

    init {
        modSink = DragBoth()
        .align_(\left).background_(Color.white)
        .receiveDragHandler_({ |drag|
            var moduleString = View.currentDrag;
            var className = ("NS_" ++ moduleString).asSymbol.asClass;
            if( className.respondsTo('isSource'),{ 
                if(module.notNil,{ module.free });
                drag.object_(moduleString);
                drag.string_(moduleString);
                module = className.new(strip, slotIndex);
            })
        });

        guiButton = Button().maxHeight_(25).maxWidth_(15)
        .states_(
            [["Ø", Color.white, Color.black]] ++
            NSFW.controllers.collect({ |ctrl|
                [ ctrl.modSinkLetter, ctrl.modSinkColor[0], ctrl.modSinkColor[1] ]
            })
        )
        .action_({ |but|
            var butIndex = but.value;
            if(module.notNil,{

                // this has to be rethought if/when I use multiple controllers
                case
                { butIndex == 0 }{
                    NSFW.controllers.wrapAt(butIndex - 1).removeModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1)
                }
                { butIndex == 1 }{
                    NSFW.controllers[butIndex - 1].addModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1, module.class)
                }
                { 
                    NSFW.controllers.wrapAt(butIndex - 1).removeModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1);
                    NSFW.controllers[butIndex - 1].addModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1, module.class)
                };
            })
        });

        view = View().layout_( 
            HLayout(
                modSink,
                Button().maxHeight_(25).maxWidth_(15)
                .states_([["S", Color.black, Color.yellow]])
                .action_({ |but|
                    if(module.notNil,{ module.toggleVisible })
                }),
                Button().maxHeight_(25).maxWidth_(15)
                .states_([["X", Color.black, Color.red]])
                .action_({ |but|
                    this.free
                }),
                guiButton
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins);
    }

    asView { ^view }

    free {
        if(guiButton.value > 0,{
            NSFW.controllers[guiButton.value - 1].removeModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1);
        });
        guiButton.value_(0);
        module.free;
        module = nil;
        modSink.object_( nil );
        modSink.string_("");
    }

    save {
        var saveArray = List.newClear(0);
        saveArray.add( module.class);
        saveArray.add( module.save );
        saveArray.add( guiButton.value );
        ^saveArray
    }

    load { |loadArray, group|
        var className = loadArray[0];
        var string    = className.asString.split($_)[1];
        modSink.object_( string );
        modSink.string_( string );
        module = className.new(strip, slotIndex);
        module.load( loadArray[1] );
        guiButton.value_( loadArray[2]  )
    }
}

NS_InModuleSink {
    var <strip;
    var <view, <modSink, <guiButton;
    var <>module;

    *new { |channelStrip|
        ^super.newCopyArgs(channelStrip).init
    }

    init {
        modSink = DragBoth()
        .align_(\center).background_(Color.white).string_("in")
        .receiveDragHandler_({ |drag|
            var dragObject = View.currentDrag;
            var className  = ("NS_" ++ dragObject).asSymbol.asClass;

            // clean this up
            if(className.respondsTo('isSource'),{
                if(className.isSource == true,{
                    if(module.notNil,{ module.free });
                    drag.object_(dragObject);
                    drag.align_(\left).string_("in:" + dragObject.asString);
                    module = className.new(strip, -1);
                })
            },{
                if(dragObject.isInteger,{
                    if(module.notNil,{ module.free }); 
                    drag.object_(dragObject);
                    drag.align_(\left).string_("in:" + dragObject.asString);
                    module = dragObject; 
                    strip.inSynth.set( \inBus, NS_ServerHub.servers[strip.group.server.name].inputBusses[dragObject] )
                })
            })
        });

        guiButton = Button().maxHeight_(25).maxWidth_(15)
        .states_(
            [["Ø", Color.white, Color.black]] ++
            NSFW.controllers.collect({ |ctrl|
                [ ctrl.modSinkLetter, ctrl.modSinkColor[0], ctrl.modSinkColor[1] ]
            })
        )
        .action_({ |but|
            var butIndex = but.value;
            if(module.notNil and: { module.isInteger.not },{

                case
                { butIndex == 0 }{
                    NSFW.controllers.wrapAt(butIndex - 1).removeModuleFragment(strip.pageIndex, strip.stripIndex, 0)
                }
                { butIndex == 1 }{
                    NSFW.controllers[butIndex - 1].addModuleFragment(strip.pageIndex, strip.stripIndex, 0, module.class)
                }
                { 
                    NSFW.controllers.wrapAt(butIndex - 1).removeModuleFragment(strip.pageIndex, strip.stripIndex, 0);
                    NSFW.controllers[butIndex - 1].addModuleFragment(strip.pageIndex, strip.stripIndex, 0, module.class)
                };
            })
        });

        view = View().layout_( 
            HLayout(
                modSink,
                Button().maxHeight_(45).maxWidth_(15)
                .states_([["S", Color.black, Color.yellow]])
                .action_({ |but|
                    if(module.notNil,{ module.toggleVisible })
                }),
                Button().maxHeight_(45).maxWidth_(15)
                .states_([["X", Color.black, Color.red]])
                .action_({ |but|
                    this.free
                }),
                guiButton
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins);
    }

    asView { ^view }

    free {
        if(guiButton.value > 0,{
            NSFW.controllers[guiButton.value - 1].removeModuleFragment(strip.pageIndex, strip.stripIndex, 0);
        });
        guiButton.value_(0);
        strip.inSynth.set(\inBus,strip.stripBus);
        module.free;
        module = nil;
        modSink.object = nil;
        modSink.align_(\center).string_("in");
    }

    save {
        var saveArray = List.newClear(0);
        if(module.isInteger,{
            saveArray.add( module )
        },{
            saveArray.add( module.class);
            saveArray.add( module.save );
            saveArray.add( guiButton.value );
        });
        ^saveArray
    }

    load { |loadArray|
        if(loadArray.size > 1,{
            var className = loadArray[0];
            var string    = className.asString.split($_)[1];

            modSink.object_( string );
            modSink.align_(\left).string_("in:" + string);
            module = className.new(strip, -1);
            module.load( loadArray[1] );
            guiButton.value_( loadArray[2] )

        },{
            var integer = loadArray[0];
            module = integer; 
            modSink.object_( integer );
            modSink.align_(\left).string_("in:" + integer);
            strip.inSynth.set(\inBus,NS_ServerHub.servers[strip.group.server.name].inputBusses[integer])
        })
    }
}
