NS_ModuleSlotView : SCViewHolder {

    *new { |strip, slotIndex|
        ^super.new.init(strip, slotIndex)
    }

    init { |strip, slotIndex|
        // is there a better way to do this?
        var nsControl = strip.controls[("module" ++ slotIndex).asSymbol];

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
                ).spacing_(0).margins_(0)
            )
        });

        view = View().layout_( 
            HLayout(
                slotSink,
                NS_Button([
                    [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
                ])
                .fixedSize_(20)
                .addLeftClickAction({ 
                    strip.slots[slotIndex] !? { strip.slots[slotIndex].toggleView }
                }),
                NS_Button([
                    [NS_Style('clear'), NS_Style('textDark'), NS_Style('red')]
                ])
                .fixedSize_(20)
                .addLeftClickAction({ nsControl.resetValue }),
            )
        );

        view.layout.spacing_(0).margins_(0);
    }
}
