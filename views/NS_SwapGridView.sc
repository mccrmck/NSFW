NS_SwapGridView : SCViewHolder {

    *new { |swapGrid|
        ^super.new.init(swapGrid)
    }

    init { |swapGrid|
        var numPages  = NS_Server.numPages;
        var numStrips = NS_Server.numStrips;

        view = View().layout_(
            HLayout(
                *numStrips.collect({ |stripIndex|
                    NS_ControlSwitch(
                        swapGrid.controls[stripIndex.asSymbol],
                        numPages.collect({ |page| "%:%".format(page, stripIndex) })
                    ).minWidth_(30)
                })
            ).nsMarginsSpacing(0)
        )
    }
}
