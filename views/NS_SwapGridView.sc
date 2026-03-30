NS_SwapGridView : SCViewHolder {

    *new { |swapGrid|
        ^super.new.init(swapGrid)
    }

    init { |swapGrid|
        var numPages  = NS_Server.numPages;
        var numStrips = NS_Server.numStrips;

        view = NS_ContainerView()
        .layout_(
            VLayout(
                NS_Header("swap grid"),
                NS_HDivider(),
                [
                    HLayout(
                        *numStrips.collect({ |stripIndex|
                            NS_ControlSwitch(
                                swapGrid.controls[stripIndex.asSymbol],
                                numPages.collect({ |page| "%:%".format(page, stripIndex) })
                            )
                        })
                    ).nsMarginsSpacing('inner'),
                    s: 1
                ]
            ).nsMarginsSpacing('view')
        )
    }
}
