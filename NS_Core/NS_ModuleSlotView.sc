NS_ModuleSlotView : NS_Widget {

    *new { |strip, slotIndex|
        ^super.new.init(strip, slotIndex)
    }

    init { |strip, slotIndex|
        var nsControl = strip.controls[slotIndex + 3];

        // this needs some work, perhaps an extra NS_Control for saving
        var ctrlMenu = NS_Controller.subclasses.collect({ |ctrl|
            MenuAction(ctrl.asString, { |menu, checked|
                var moduleOrNil = nsControl.value;
                var pageIndex   = strip.stripId.first;
                var stripIndex  = strip.stripId.last.digit;

                pageIndex = if(pageIndex.isAlpha,{ pageIndex },{ pageIndex.digit });

                moduleOrNil = moduleOrNil !? { ("NS_" ++ moduleOrNil).asSymbol.asClass };

                if(checked,{
                    ctrl.addModuleFragment(pageIndex, stripIndex, slotIndex, moduleOrNil)
                },{
                    ctrl.removeModuleFragment(pageIndex, stripIndex, slotIndex)
                });
                menu.checked_(checked)
            }).checkable_(true)
        });

        var slotSink = NS_ControlSink(nsControl)
        .addRightClickAction({ 
            Menu( 
                NS_ModuleListView(nsControl),
                Menu( *ctrlMenu ).title_("send to controller")
            ).front
        });
        
        view = View().layout_( 
            HLayout(
                [
                    slotSink,
                    s: 8
                ],
                [
                    Button()
                    .fixedWidth_(15)
                    .states_([["S", NS_Style.textDark, NS_Style.yellow]])
                    .action_({ 
                        strip.slots[slotIndex] !? { strip.slots[slotIndex].toggleVisible }
                    }),
                    s:1
                ],
                [
                    Button()
                    .fixedWidth_(15)
                    .states_([["X", NS_Style.textDark, NS_Style.red]])
                    .action_({ nsControl.value_("") }),
                    s:1
                ]
            )
        );

        view.layout.spacing_(0).margins_(0);
    }
}
