NS_ModuleSlot {
    var strip, slotIndex;
    var <controls;
    var <>module;
    var <view;

    *new { |strip, index|
        ^super.newCopyArgs(strip, index).init
    }

    init {
        controls = List.newClear(3);

        controls[0] = NS_Control(\showHide, ControlSpec(0,0,\lin,1), 0)
        .addAction(\sink,{ |c| if(module.notNil, {module.toggleVisible}) }, false);

        controls[1] = NS_Control(\free, ControlSpec(0,0,\lin,1), 0)
        .addAction(\sink,{ |c| 1.postln; this.free }, false);

        // this should be dynamic based on the controllers available?
        controls[2] = NS_Control(\ctrl, ControlSpec(0,1,\lin,1), 0)
        .addAction(\sink,{ |c| 2.postln }, false);

        view = NS_ModuleSlotView(this)

    }

    loadModule { |className|
        module = className.new(strip, slotIndex);
    }

    // these need review/refactoring!

    free {
       // if(guiButton.value > 0,{
        //NSFW.controllers[guiButton.value - 1].removeModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1);
     //   });
     //   guiButton.value_(0);
        module.free;
        module = nil;
        view.free
    }
    
 //   save {
 //       var saveArray = List.newClear(0);
 //       saveArray.add( module.class);
 //       saveArray.add( module.save );
 //       saveArray.add( guiButton.value );
 //       ^saveArray
 //   }

 //   load { |loadArray, group|
 //       var className = loadArray[0];
 //       var string    = className.asString.split($_)[1];
 //       modSink.object_( string );
 //       modSink.string_( string );
 //       module = className.new(strip, slotIndex);
 //       module.load( loadArray[1] );
 //       guiButton.value_( loadArray[2]  )
 //   }
}

NS_ModuleSlotView {
    var modSlot, slotSink;
    var <view;

    *new { |modSlot|
        ^super.newCopyArgs(modSlot).init
    }

    init {
        var controls = modSlot.controls;

        slotSink = DragBoth()
        .align_(\left)
        .background_(NS_Style.bGroundLight)
        .canReceiveDragHandler_({ View.currentDrag.isString })
        .receiveDragHandler_({ |drag|
            var moduleString = View.currentDrag;
            var className = ("NS_" ++ moduleString).asSymbol.asClass;
            if( className.respondsTo('isSource'),{            // do I still need this check?
                if(modSlot.module.notNil,{ modSlot.free });
                this.loadModule(moduleString);
                modSlot.loadModule(className)
            })
        });

        view = View()
        .layout_( 
            HLayout(
                [ slotSink, s: 500],
                [
                    NS_ControlButton(
                        controls[0],
                        [["S", Color.black, Color.yellow]]
                    ),
                    s: 1
                ],
                [
                    NS_ControlButton(
                        controls[1],
                        [["X", Color.black, Color.red]]
                    ),
                    s: 1
                ],
                [
                    NS_ControlButton(                       // gui Button
                        controls[2],
                        [["Ø", Color.white, Color.black]]
                        //++

                    ),
                    s: 1
                ]
            )
        );

        view.layout.spacing_(0).margins_([2,0]);
    }

    loadModule { |string|
        slotSink.object_(string);
        slotSink.string_(string)
    }

    // consider a new name for this, free would imply destroying the view
    free {
        slotSink.object_( nil );
        slotSink.string_("");
    }

    asView { ^view }
}
