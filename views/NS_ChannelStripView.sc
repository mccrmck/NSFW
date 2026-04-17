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

        var outSendToggles = NS_Server.numOutStrips.collect { |stripNum|
            controls["O:%Toggle".format(stripNum).asSymbol]
        };

        var outSendKnobs = NS_Server.numOutStrips.collect { |stripNum|
            controls["O:%Knob".format(stripNum).asSymbol]
        };

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
                //NS_ReceiveView(*receives),
                //NS_HDivider(),
                VLayout( *slotViews ).nsMarginsSpacing('inner'),
                HLayout(ampFader, showButton, muteButton).nsMarginsSpacing('inner'),
                NS_HDivider(),
                NS_SendView(outSendToggles, outSendKnobs)
                .addRightClickAction({ "send".postln })
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

        var outSendToggles = NS_Server.numOutStrips.collect { |stripNum|
            controls["O:%Toggle".format(stripNum).asSymbol]
        };

        var outSendKnobs = NS_Server.numOutStrips.collect { |stripNum|
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
                NS_SendView(outSendToggles, outSendKnobs)
                .addRightClickAction({ |sndView, view, x, y|
                    NS_ContextMenu(
                        view, 
                        Rect(150, -120, 135, 195), 
                        VLayout(
                            NS_RoutingView(
                                stripToggles, stripKnobs, 
                                outSendToggles, outSendKnobs
                            )
                        )
                    )
                })
            ).nsMarginsSpacing('inner')
        )
    }
}
