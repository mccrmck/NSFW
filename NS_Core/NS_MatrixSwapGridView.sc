NS_MatrixSwapGridView : NS_Widget {

    *new { |swapGrid|
        ^super.new.init(swapGrid)
    }

    init { |swapGrid|
        var numPages  = NS_MatrixServer.numPages;
        var numStrips = NS_MatrixServer.numStrips;

        view = View().layout_(
            HLayout(
                *numStrips.collect({ |stripIndex|
                    NS_ControlSwitch(
                        swapGrid.controls[stripIndex],
                        numPages.collect({ |page| "%:%".format(page, stripIndex) })
                    ).minWidth_(30)
                })
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins);
    }
}
