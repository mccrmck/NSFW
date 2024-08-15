NS_Pan {

    *new { |sig, numChans, pan, width = 2, orientation = 0|
        ^super.new.init(sig, numChans, pan, width, orientation)
    }

    init { |sig, numChans, pan, width, orientation|
        width = width.asInteger;
        if(numChans > 2,{
            // this is to avoid assymetrical spatial images (ie. width == 3; orientation == 0.5 )
            if(orientation == 0,{
                if(width.even,{ width = (width + 1) })
            },{
                if(width.odd,{ width = (width - 1) })
            });

            // check how this works with static values, bipolar oscillators, should be okay...
            // I *think* PanAz needs to be offset with 1/numChans when orientation == 0, but it needs to be tested

            ^PanAz.ar(numChans, sig, pan.clip2, width: width.clip(1,numChans), orientation: orientation)
        },{
            ^Pan2.ar(sig,pan.clip2);
        })
    }
}
