NS_ServerOutMeter : SCViewHolder {

    *new { |numChans = 2|
        ^super.new.init(numChans)
    }

    init { |numChans|

        view = NS_ContainerView()
        .layout_(
            VLayout(
                StaticText()
                .string_("outputs")
                .align_(\center)
                .stringColor_(NS_Style.textDark),
                VLayout(
                    *numChans.collect({ |i|
                        NS_LevelMeter(i).value_(1.0.rand)
                    })
                )
            )
        );

      //  view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins);
    }
}
