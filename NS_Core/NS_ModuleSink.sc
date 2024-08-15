NS_ModuleSink {
    var <strip, <slotIndex;
    var <view, <modSink;
    var <>module;

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |chStrip|
        strip = chStrip;
        modSink = DragBoth().align_(\left).background_(Color.white);

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
              //  Button().maxHeight_(45).maxWidth_(15)
              //  .states_([["G", Color.black, Color.cyan]])
              //  .action_({ |but|
              //  });

            )
        );

        view.layout.spacing_(0).margins_([0,2]);
    }

    // this can be moved to the .init function, I think...
    onReceiveDrag { |slotGroup, slot| // this can also be reduced to just slot -> strip.slotGroups[slotIndex]
        slotIndex = slot;
        modSink.receiveDragHandler_({ |drag|
            var moduleString = View.currentDrag[0];
            var className = ("NS_" ++ moduleString).asSymbol.asClass;
            if( className.respondsTo('isSource'),{ 
                if(module.notNil,{ module.free });
                drag.object_(View.currentDrag);
                drag.string_(moduleString);
                module = className.new(slotGroup, strip.stripBus, strip);
                
                // this will only work for channelStrips, not OutChannelStrips -> must fix!
                NSFW.controllers.do({ |ctrl| ctrl.addModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1, className) }) // the +1 is give space for the inModule 

            })
        })
    }

    asView { ^view }

    free {
        module.free;
        module = nil;
        modSink.string_("");
        NSFW.controllers.do({ |ctrl| ctrl.removeModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1) })
    }

    save {
        var saveArray = List.newClear(0);
        saveArray.add( module.class);
        saveArray.add( module.save );
        ^saveArray
    }

    load { |loadArray, group|
        var className = loadArray[0];
        var string    = className.asString.split($_)[1];

        modSink.string_( string );
        module = className.new(group, strip.stripBus, strip);
        module.load(loadArray[1])
    }
}

NS_InModuleSink {
    var <strip;
    var <view, <modSink;
    var <>module;

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |chStrip|
        strip = chStrip;
        modSink = DragBoth().align_(\center).background_(Color.white).string_("in");
        this.onReceiveDrag;

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
                });
            )
        );

        view.layout.spacing_(0).margins_([0,2]);
    }

    onReceiveDrag {
        modSink.receiveDragHandler_({ |drag|
            var dragObject = View.currentDrag[0];
            var className  = ("NS_" ++ dragObject).asSymbol.asClass;

            if(className.respondsTo('isSource'),{
                if(className.isSource == true,{
                    if(module.notNil,{ module.free });
                    drag.object_(View.currentDrag);
                    drag.align_(\left).string_("in:" + dragObject.asString);
                    module = className.new( strip.inGroup, strip.stripBus, strip );
                    NSFW.controllers.do({ |ctrl| ctrl.addModuleFragment(strip.pageIndex, strip.stripIndex, 0, className) })
                })
            },{
                if(dragObject.isInteger,{
                    if(module.notNil,{ module.free }); 
                    module = dragObject.asInteger; 
                    drag.object_(View.currentDrag);
                    drag.string_("in:" + dragObject.asString);
                    strip.inSynth.set(\inBus,NS_ServerHub.servers[strip.modGroup.server.name].inputBusses[dragObject])
                })
            })
        })
    }

    asView { ^view }

    free {
        strip.inSynth.set(\inBus,strip.stripBus);
        module.free;
        module = nil;
        modSink.align_(\center).string_("in");
        NSFW.controllers.do({ |ctrl| ctrl.removeModuleFragment(strip.pageIndex, strip.stripIndex, 0) })
    }

    save {
        var saveArray = List.newClear(0);
        if(module.isInteger,{
            saveArray.add( module )
        },{
            saveArray.add( module.class);
            saveArray.add( module.save );
        });
        ^saveArray
    }

    load { |loadArray|
        if(loadArray.size > 1,{
            var className = loadArray[0];
            var string    = className.asString.split($_)[1];

            //modSink.object = 
            modSink.align_(\left).string_("in:" + string);
            module = className.new(strip.inGroup, strip.stripBus, strip);
            module.load(loadArray[1])

        },{
            var integer = loadArray[0];
            module = integer; 
            //modSink.object_();
            modSink.string_("in:" + integer);
            strip.inSynth.set(\inBus,NS_ServerHub.servers[strip.modGroup.server.name].inputBusses[integer])


        })
    }
}
