NS_Pan {

    *new { |sig, numChans, pan, width = 2, orientation = 0.5|
        ^super.new.init(sig, numChans, pan, width, orientation)
    }

    init { |sig, numChans, pan, width, orientation|
        if(numChans > 2,{
            // this is to avoid assymetrical spatial images (ie. width == 3; orientation == 0.5 )
            if(orientation == 0,{
                if(width.even,{ width = (width + 1) })
            },{
                if(width.odd,{ width = (width - 1) })
            });

            // I *think* pan needs to be offset with 1/numChans when orientation == 0, but it needs to be tested
            
            ^PanAz.ar(numChans, sig, pan.clip(-1,1), width: width.clip(1,numChans), orientation: orientation)
        },{
            // this doesn't really work - consider the following possible inputs:
            // static values
            // bipolar oscillators
            ^Pan2.ar(sig,pan.fold(0,1).linlin(0,1,-1.1,1.1).clip2);
        })
    }
}
