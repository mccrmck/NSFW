NS_ModuleSlotView : NS_Widget {

    *new { |strip, slotIndex|
        ^super.new.init(strip, slotIndex)
    }

    init { |strip, slotIndex|

      var slotSink = NS_ControlSink(strip.controls[slotIndex + 3]);

      var ctrlMenu = NS_Controller.subclasses.collect({ |ctrl|
          MenuAction(ctrl.asString, { |menu, checked|
              var moduleOrNil = strip.controls[slotIndex + 3].value;
              var pageIndex = strip.stripId.split($:)[0].asInteger;
              var stripIndex = strip.stripId.split($:)[1].asInteger;

              moduleOrNil = moduleOrNil !? { ("NS_" ++ moduleOrNil).asSymbol.asClass };

              if(checked,{
                  ctrl.addModuleFragment(pageIndex, stripIndex, slotIndex, moduleOrNil)
              },{
                  ctrl.removeModuleFragment(pageIndex, stripIndex, slotIndex)
              });
              menu.checked_(checked)
          }).checkable_(true)
      });

      view = View().layout_( 
          HLayout(
              [slotSink, s: 10],
              [
                  Button()
                  .minWidth_(15)
                  .states_([["S", NS_Style.textDark, NS_Style.yellow]])
                  .action_({ 
                      strip.slots[slotIndex] !? { strip.slots[slotIndex].toggleVisible }
                  }),
                  s:1
              ],
              [
                  Button()
                  .minWidth_(15)
                  .states_([["X", NS_Style.textDark, NS_Style.red]])
                  .action_({ strip.controls[slotIndex + 3].value_("") }),
                  s:1
              ],
              [
                  Button()
                  .minWidth_(15)
                  .states_([["Ø", NS_Style.textLight, NS_Style.bGroundDark]])
                  .action_({ 
                      Menu(
                          *[MenuAction.separator("send to:")] ++ ctrlMenu
                      ).front
                  }),
                  s:1
              ]
          )
      );

      view.layout.spacing_(0).margins_([2,0]);
  }
}
