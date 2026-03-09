NS_ServerOutMeterView : SCViewHolder {

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var numOutChans = nsServer.options.outChannels;

        var meterStack = if(numOutChans > 16,{
            GridLayout.columns( *nsServer.outMeter.outLevelMeters.clump(numOutChans / 2) )
        },{
            VLayout( *nsServer.outMeter.outLevelMeters )
        });

        view = NS_ContainerView()
        //.maxHeight_(
        //    NS_Style('viewMargins')[1] + // top margin
        //    20 + 2 + 20 +                // label + divider + button
        //    (numOutChans * (20 + 2)) +   // NS_LevelMeter height
        //    NS_Style('viewMargins')[3]   // bottom margin
        //)
        .layout_(
            VLayout(
                StaticText()
                .string_("outputs")
                .align_(\center)
                .stringColor_( NS_Style('textDark') ),
                NS_HDivider(),
                NS_Button([
                    ["startMeter", NS_Style('textLight'), NS_Style('bGroundDark')],
                    ["stopMeter", NS_Style('bGroundLight'), NS_Style('textDark')]
                ])
                .addLeftClickAction({ |b|
                    if(b.value == 1,{
                        nsServer.outMeter.startMetering;
                    },{
                        nsServer.outMeter.stopMetering
                    })
                }),
                meterStack
            ).spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'));
        )
    }
}
