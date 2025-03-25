NS_ServerInputMeterView {
    var <view;

    *new { |numIns|
        ^super.new.init(numIns)
    }

    init { |numIns|

        var meters = numIns.collect({ NS_LevelMeter().value_(1.0.rand) });

        view = View().minWidth_(120).layout_(
            VLayout( meters ) 
        );

        view.layout.margins_(NS_Style.viewSpacing).spacing_(NS_Style.viewMargins)
    }

    asView { ^view }
}
