NS_Pan {

    // oriention = 0 when the front is a vertex,
    // oriention = 0.5 when the front bisects a side
    *new { |sig, numChans, pan, width(2), orientation(0.5)|
        ^super.new.init(sig, numChans, pan, width, orientation)
    }

    init { |sig, numChans, pan, width, orientation|
        width = width.asInteger;
        if(numChans > 2,{
            // test offseting with 1/numChans when orientation == 0, from an old note

            ^PanAz.ar(
                numChans, sig, pan, 
                width: width.clip(1, numChans), orientation: orientation
            )
        },{
            ^Pan2.ar(sig,pan.clip2);
        })
    }
}
