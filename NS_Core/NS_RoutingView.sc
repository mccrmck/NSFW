NS_MatrixRoutingView : NS_Widget {

    *new { |strip|
        ^super.new.init(strip)
    }

    init { |strip|
        var nsServer = NSFW.servers[strip.stripGroup.server.name];
        var thisPage = strip.stripId.split($:)[0].asInteger;
        var thisStrip = strip.stripId.split($:)[1].asInteger;

        var stripButtons = strip.sendCtrls['stripSends'].collect({ |ctrl, ctrlIndex|
            var string = nsServer.strips.flat[ctrlIndex].stripId;
            var stripBool = (ctrlIndex % 4) != thisStrip;
            var pageBool  = ctrlIndex > (thisPage * 4 + thisStrip); 

            if(stripBool and: pageBool,{
                NS_ControlButton(ctrl, [
                    [string, NS_Style.textDark, NS_Style.bGroundLight],
                    [string, NS_Style.textLight, NS_Style.bGroundDark]
                ]).font_(Font(*NS_Style.smallFont)).maxWidth_(30)
            },{
                Button()
                .maxWidth_(30)
                .font_(Font(*NS_Style.smallFont))
                .enabled_(false)
                .states_([
                    [string, NS_Style.textDark, NS_Style.darklight]
                ])
            })
        });

        var outButtons = strip.sendCtrls['outSends'].collect({ |ctrl, ctrlIndex|
            var string = nsServer.outMixer[ctrlIndex].stripId;

            NS_ControlButton(ctrl, [
                [string, NS_Style.textDark, NS_Style.bGroundLight],
                [string, NS_Style.textLight, NS_Style.bGroundDark]
            ]).font_(Font(*NS_Style.smallFont)).maxWidth_(30)

        });

        view = View()
        .background_(NS_Style.bGroundDark)
        .layout_(
            VLayout(
                StaticText()
                .string_("strips")
                .align_(\center)
                .stringColor_(NS_Style.textLight),
                GridLayout.rows( *stripButtons.clump(4) ),
                StaticText()
                .string_("outputs")
                .align_(\center)
                .stringColor_(NS_Style.textLight),
                GridLayout.rows( *outButtons.clump(4) ),
            ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
        )
    }
}

NS_MatrixRoutingOutView : NS_Widget {

    *new { |strip|
        ^super.new.init(strip)
    }

    init { |strip|
        var outButtons = strip.sendCtrls['hardwareSends'].collect({ |ctrl, ctrlIndex|
            NS_ControlButton(ctrl, [
                [ctrl.label, NS_Style.textDark, NS_Style.bGroundLight],
                [ctrl.label, NS_Style.textLight, NS_Style.bGroundDark]
            ]).font_(Font(*NS_Style.smallFont)).maxWidth_(30)
        });

        view = View()
        .background_(NS_Style.bGroundDark)
        .layout_(
            VLayout(
                StaticText()
                .string_("hardware outs")
                .align_(\center)
                .stringColor_(NS_Style.textLight),
                GridLayout.rows( *outButtons.clump(4) )
            ).spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
        )
    }
}
