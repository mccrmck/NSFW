NS_ModuleSlotView {
    var slotSink;
    var <view;

    *new { |strip, slotIndex|
        ^super.new.init(strip, slotIndex)
    }

    init { |strip, slotIndex|
        slotSink = DragBoth()
        .align_(\left)
        .stringColor_(NS_Style.textDark)
        .background_(NS_Style.bGroundLight)
        .canReceiveDragHandler_({ View.currentDrag.isString })
        .receiveDragHandler_({ |drag|
            var moduleString = View.currentDrag;
            var className = ("NS_" ++ moduleString).asSymbol.asClass;
            if( className.respondsTo('isSource'),{            // do I still need this check?
                //   modSlot.free;
                //  this.loadModule(moduleString);
                //  modSlot.loadModule(className)
                strip.addModule(className, slotIndex);
                this.setObjectString(moduleString)
            })
        });

        view = View().layout_( 
            HLayout(
                [slotSink, s: 10],
                [Button()
                .minWidth_(15)
                .states_([["S", Color.black, Color.yellow]])
                .action_({ 
                    if(strip.slots[slotIndex].notNil,{
                        strip.slots[slotIndex].toggleVisible 
                    })
                }),
                s:1],
                [Button()
                .minWidth_(15)
                .states_([["X", Color.black, Color.red]])
                .action_({ strip.freeModule(slotIndex); this.setObjectString(nil) }),
                s:1],
                [Button()
                .minWidth_(15)
                .states_([["Ø", Color.white, Color.black]]) // will have other states, I guess
                .action_({ /* controlGui stuff */ }),
                s:1]
                //[NS_ControlButton(controls[0], [["S", Color.black, Color.yellow]]), s: 1],
                //[NS_ControlButton(controls[1], [["X", Color.black, Color.red]]), s: 1],
                //[NS_ControlButton(controls[2], [["Ø", Color.white, Color.black]] /* ++ */), s: 1]
                // old stuff re: gui button
                // var butIndex = but.value;
                // if(module.notNil,{

                //     // this has to be rethought if/when I use multiple controllers
                //     case
                //     { butIndex == 0 }{
                //         NSFW.controllers.wrapAt(butIndex - 1).removeModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1)
                //     }
                //     { butIndex == 1 }{
                //         NSFW.controllers[butIndex - 1].addModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1, module.class)
                //     }
                //     { 
                //         NSFW.controllers.wrapAt(butIndex - 1).removeModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1);
                //         NSFW.controllers[butIndex - 1].addModuleFragment(strip.pageIndex, strip.stripIndex, slotIndex + 1, module.class)
                //     };
                // })

            )
        );

        view.layout.spacing_(0).margins_([2,0]);
    }

    setObjectString { |string|
        slotSink.object_(string);
        slotSink.string_(string)
    }

    asView { ^view }
}
