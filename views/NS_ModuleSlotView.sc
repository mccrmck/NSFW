NS_ModuleSlotView : SCViewHolder {

    *new { |strip, slotIndex|
        ^super.new.init(strip, slotIndex)
    }

    init { |strip, slotIndex|
        // is there a better way to do this?
        var nsControl = strip.controlDict[("module" ++ slotIndex).asSymbol];

        var slotSink = NS_ControlSink(nsControl)
        .addRightClickAction({ |cSink, view, x, y|
            var ctrlButtons = NS_Controller.subclasses.collect({ |ctrl|

                // for now these are stateless/won't be saved - must fix
                NS_Button(ctrl.asString ! 2)
                .addLeftClickAction({ |b, v, x, y|
                    var moduleOrNil = nsControl.value;
                    var pageIndex   = strip.stripId.first;
                    var stripIndex  = strip.stripId.last.digit;

                    pageIndex = if(pageIndex.isAlpha,{ pageIndex },{ pageIndex.digit });

                    moduleOrNil = moduleOrNil !? { ("NS_" ++ moduleOrNil).asSymbol.asClass };

                    if(b.value == 1,{
                        ctrl.addModuleFragment(pageIndex, stripIndex, slotIndex, moduleOrNil)
                    },{
                        ctrl.removeModuleFragment(pageIndex, stripIndex, slotIndex)
                    });
                })
            });

            NS_ContextMenu(
                view,
                Rect(120, -120, 180, 150),
                VLayout(
                    *[NS_ModuleListView(nsControl)] ++ ctrlButtons;
                ).nsMarginsSpacing(0)
            )
        });

        view = View().layout_( 
            HLayout(
                slotSink.minWidth_(105),
                NS_Button.show.fixedSize_(20)
                .addLeftClickAction({ 
                    strip.slots[slotIndex] !? { strip.slots[slotIndex].toggleView }
                }),
                NS_Button.clear.fixedSize_(20)
                .addLeftClickAction({ nsControl.resetValue }),
            ).nsMarginsSpacing('inner')
        )
    }
}
