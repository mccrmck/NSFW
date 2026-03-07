NS_SwapGridView : NS_Widget {

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
            )
        );

        view.layout.spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'));
    }
}
