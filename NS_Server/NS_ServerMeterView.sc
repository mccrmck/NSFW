NS_ServerOutMeterView : NS_Widget {

    *new { |numChans = 2|
        ^super.new.init(numChans)
    }

    init { |numChans|

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var rect = Rect(0,0,w,h);
            var rad = NS_Style.radius;

            Pen.fillColor_( NS_Style.highlight );
            Pen.addRoundedRect(rect, rad, rad);
            Pen.fill;
        })
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

NS_ServerOutMeter {

    *initClass {
        ServerBoot.add { |server|

            SynthDef(\ns_serverOutMeter,{
                var sig = In.ar(0, server.options.numOutputBusChannels);
                var trigFreq = 30;
                // consider .ar here
                SendPeakRMS.kr(sig, 30, 3, "/" ++ server.name ++ "OutLevels")
            }).add
            
        }
    }

    *new {
        ^super.new.init
    }

    init {
        
    }


}
