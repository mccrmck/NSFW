NS_ChannelStripView : SCViewHolder {

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controlDict;

        var header = NS_Header(strip.stripId)
        .beginDragAction_({ strip.stripId });

        var ampFader = NS_ControlFader(controls['amp'], 0.1);

        var showButton = NS_Button.show.fixedSize_(20)
        .addLeftClickAction({ strip.toggleAllVisible });

        var muteButton = NS_ControlButton.mute(controls['mute']).fixedSize_(20);

        var slotViews = strip.slots.size.collect({ |slotIndex| 
            NS_ModuleSlotView(strip, slotIndex)
        });

        var nsServer = NSFW.servers[strip.stripGroup.server.name];

        var sendToggles = nsServer.outStrips.collect { |outStrip|
            controls[(outStrip.stripId ++ "Toggle").asSymbol]
        };

        var sendKnobs =  nsServer.outStrips.collect { |outStrip|
            controls[(outStrip.stripId ++ "Knob").asSymbol]
        };

        //var receives = nsServer.inStrips.collect { |inStrip|
        //    controls[inStrip.stripId.asSymbol]
        //};

        //var receives = 4.collect({ |i| 
        //
        //    NS_ControlSink(controls[("inBus" ++ i).asSymbol])
        //    .addLeftClickAction({})
        //    .addRightClickAction({ |cSink, view, x, y|
        //        var receiveAmp = NS_ControlFader(controls[("amp" ++ i).asSymbol], 0.01, 'vert');
        //        var muteButton = NS_ControlButton(controls[("mute" ++ i).asSymbol], [
        //            [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
        //            [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
        //        ]).maxHeight_(20);
        //
        //        var sinkWidth = view.absoluteBounds.width;
        //
        //        NS_ContextMenu(
        //            view,
        //            Rect(0, -90, sinkWidth, 120),
        //            VLayout(receiveAmp, muteButton).nsMarginsSpacing(0)
        //        )
        //    })
        //});

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style('radius');
            var b = NS_Style('border');

            var fill = if(strip.paused)
            { NS_Style('transparent') }
            { NS_Style('highlight') };

            Pen.fillColor_(fill);
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.fillStroke;
        })
        .layout_(
            VLayout(
                header.maxHeight_(30),
                //HLayout( *receives ).nsMarginsSpacing('inner'),
                NS_ReceiveView(*receives),
                NS_HDivider(),
                VLayout( *slotViews ).nsMarginsSpacing('inner'),
                HLayout(ampFader, showButton, muteButton).nsMarginsSpacing('inner'),
                NS_HDivider(),
                NS_SendView(*sends)
            ).nsMarginsSpacing('view')
        )
    }
}

NS_ChannelStripOutView : SCViewHolder { 

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controlDict;

        var ampFader = NS_ControlFader(controls['amp'], 0.1);

        var showButton = NS_Button.show.fixedSize_(20)
        .addLeftClickAction({ strip.toggleAllVisible });

        var muteButton = NS_ControlButton.mute(controls['mute']).fixedSize_(20);

        var slotViews = strip.slots.size.collect({ |slotIndex| 
            NS_ModuleSlotView(strip, slotIndex)
        });

        var sends = controls.reject{ |val, key| 
            (key == 'amp') || (key == 'mute') || (key.asString.contains("module"))
        }.collectAs({ |ctrl|
            NS_ControlButton(ctrl, [
                [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
            ]).font_( Font(*NS_Style('smallFont')) )
        }, Array).clump(4);

        view = View().layout_(
            VLayout(
                NS_Header(strip.stripId),
                VLayout( *slotViews ).nsMarginsSpacing('inner'),
                HLayout(ampFader, showButton, muteButton).nsMarginsSpacing('inner'),
                GridLayout.rows( *sends ).nsMarginsSpacing('inner')
            ).nsMarginsSpacing('inner')
        )
    }
}

NS_ChannelStripInView : SCViewHolder {

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controlDict;

        var ampFader = NS_ControlFader(controls['amp'], 0.1);

        var showButton = NS_Button.show.fixedSize_(20)
        .addLeftClickAction({ strip.toggleAllVisible });

        var muteButton = NS_ControlButton.mute(controls['mute']).fixedSize_(20);

        var slotViews = strip.slots.size.collect({ |slotIndex|
            NS_ModuleSlotView(strip, slotIndex)
        });

        var nsServer = NSFW.servers[strip.stripGroup.server.name];

        var sendToggles = NS_Server.numOutStrips.collect { |stripNum|
            controls["O:%Toggle".format(stripNum).asSymbol]
        };

        var sendKnobs = NS_Server.numOutStrips.collect { |stripNum|
            controls["O:%Knob".format(stripNum).asSymbol]
        };

        var stripToggles = NS_Server.numPages.collect { |pageNum|
            NS_Server.numStrips.collect { |stripNum|
                controls["%:%Toggle".format(pageNum, stripNum).asSymbol]
            }
        }.flat;

        var stripKnobs = NS_Server.numPages.collect { |pageNum|
            NS_Server.numStrips.collect { |stripNum|
                controls["%:%Knob".format(pageNum, stripNum).asSymbol]
            }
        }.flat;

        var inBus = NS_ControlText(controls[(strip.stripId ++ "_inBus").asSymbol])
        .maxHeight_(30);

        view = UserView()
        .layout_(
            VLayout(
                inBus,
                VLayout( *slotViews ).nsMarginsSpacing('inner'),
                HLayout(ampFader, showButton, muteButton).nsMarginsSpacing('inner'),
                NS_HDivider(),
                NS_SendView(*sends)
            ).nsMarginsSpacing('inner')
        )
    }
}
