NS_ServerOutMeterView : SCViewHolder {

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var numOutChans = nsServer.options.outChannels;

        var meterStack = if(numOutChans > 16,{
            GridLayout.columns( 
                *nsServer.outMeter.outLevelMeters.clump(numOutChans / 2)
            ).nsMarginsSpacing('inner')
        },{
            VLayout( *nsServer.outMeter.outLevelMeters ).nsMarginsSpacing('inner')
        });

        view = //NS_ContainerView()
        UserView()
        .layout_(
            VLayout(
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
            ).nsMarginsSpacing('inner')
        )
    }
}
