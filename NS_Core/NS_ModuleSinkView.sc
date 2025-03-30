NS_ModuleSlot {
    var <controls;
    var <>module;

    *new {
        ^super.new.init
    }

    init {
        controls = List.newClear(3);

        controls[0] = NS_Control(\showHide,ControlSpec(0,0,\lin,1),0)
        .addAction(\sink,{ |c| 0.postln; /*this.toggleVisible*/ }, false);

        controls[1] = NS_Control(\free,ControlSpec(0,0,\lin,1),0)
        .addAction(\sink,{ |c| 1.postln; this.free }, false);

        controls[2] = NS_Control(\ctrl,ControlSpec(0,1,\lin,1),0)
        .addAction(\sink,{ |c| 2.postln }, false);
    }

    loadModule { |className|
     //   module = className.new(strip, slotIndex);
    }

    toggleVisible {
        module.toggleVisible
    }

  // these need review/refactoring!

    free {
       // if(guiButton.value > 0,{
        //NSFW.controllers[guiButton.value - 1].removeModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1);
     //   });
     //   guiButton.value_(0);
        module.free;
        module = nil;
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
        
        modSlot.controls.do({ |ctrl| 
            if(ctrl.label == "free",{ 
                ctrl.addAction(\clearGui,{ this.free })
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
                    // .maxHeight_(25).maxWidth_(15)
                    s: 1
                ],
                [
                    NS_ControlButton(                       // gui Button
                        controls[2],
                        [["Ø", Color.white, Color.black]]
                        //++

                    ),
                    //.maxHeight_(25).maxWidth_(15),
                    s: 1
                ]
            )
        );

        view.layout.spacing_(0).margins_(NS_Style.viewMargins);
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
