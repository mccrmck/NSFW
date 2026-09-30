NS_ServerOutMeterView : SCViewHolder {

    *new { |outMeter|
        ^super.new.init(outMeter)
    }

    init { |outMeter|
        var meters, meterStack;
        var numMeters = outMeter.numChannels;

        if(numMeters > 8) {
            meters = numMeters.collect { |i| NS_LevelMeter(i, \vert) };
            meterStack = HLayout(*meters)
        } {
            meters = numMeters.collect { |i| NS_LevelMeter(i, \horz) };
            meterStack = VLayout(*meters)
        };

        view = UserView()
        .layout_(
            VLayout(
                NS_Button(["startMeter", "stopMeter"])
                .addLeftClickAction({ |b|
                    if(b.value == 1) 
                    { outMeter.addResponder(meters) }
                    { outMeter.freeResponder(meters) }
                }),
                meterStack.nsMarginsSpacing('inner')
            ).nsMarginsSpacing('inner')
        )
    }
}
