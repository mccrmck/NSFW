NS_ChannelStripView : SCViewHolder {

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }
    
    init { |strip|
        var controls = strip.controls;

        var header = UserView()
        .minHeight_("0:0".bounds.height)
        .beginDragAction_({ strip.stripId })
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;

            Pen.stringCenteredIn(
                strip.stripId,
                Rect(0, 0, w, h),
                Font(*NS_Style('defaultFont')),
                NS_Style('textLight')
            )
        });

        var ampFader = NS_ControlFader(controls['amp'], 0.1);

        var showButton = NS_Button([
            [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
        ]).fixedSize_(20).addLeftClickAction({ strip.toggleAllVisible });

        var muteButton = NS_ControlButton(controls['mute'], [
            [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
            [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
        ]).fixedSize_(20);

        var slotViews = strip.slots.size.collect({ |slotIndex| 
            NS_ModuleSlotView(strip, slotIndex)
        });

        var nsServer = NSFW.servers[strip.stripGroup.server.name];

        var sends = nsServer.outMixer.collect({ |outStrip, i|
            var ctrl = controls[outStrip.stripId.asSymbol];

            NS_ControlButton(ctrl, [
                [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
            ]).font_(Font(*NS_Style('smallFont')))
        });

        var receives = 4.collect({ |i| 

            NS_ControlSink(controls[("inBus" ++ i).asSymbol])
            .addLeftClickAction({})
            .addRightClickAction({ |cSink, view, x, y|
                var receiveAmp = NS_ControlFader(controls[("amp" ++ i).asSymbol], 0.01, 'vert');
                var muteButton = NS_ControlButton(controls[("mute" ++ i).asSymbol], [
                    [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                    [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                ]) .maxHeight_(20);

                var sinkWidth = view.absoluteBounds.width;
                
                NS_ContextMenu(
                    view,
                    Rect(0, -90, sinkWidth, 120),
                    VLayout(receiveAmp, muteButton).spacing_(0).margins_(0)
                )
            })
        });

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style('radius');
            var fill = if(strip.paused,{ 
                NS_Style('transparent')
            },{
                NS_Style('highlight')
            });

            Pen.fillColor_(fill);
            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(2);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(1), r, r);
            Pen.fillStroke;
        })
        .layout_(
            VLayout(
                header,
                HLayout( *receives ),
                VLayout( *slotViews ),
                HLayout(ampFader, showButton, muteButton).spacing_(0).margins_(0),
                HLayout( *sends )
            )
        );

        view.layout.spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'))
    }

    refresh {
        view.refresh;
        // what else goes here? after loading, for example
    }
}

NS_ChannelStripOutView : SCViewHolder { 

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;

        var header = UserView()
        .minHeight_("o:0".bounds.height)
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;

            Pen.stringCenteredIn(
                strip.stripId,
                Rect(0, 0, w, h),
                Font(*NS_Style('defaultFont')),
                NS_Style('textLight')
            )
        });

        var ampFader = NS_ControlFader(controls['amp'], 0.1);

        var showButton = NS_Button([
            [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
        ]).fixedSize_(20).addLeftClickAction({ strip.toggleAllVisible });

        var muteButton = NS_ControlButton(controls['mute'], [
            [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
            [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
        ]).fixedSize_(20);

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
                header,
                VLayout( *slotViews ),
                HLayout(ampFader, showButton, muteButton).spacing_(0).margins_(0),
                GridLayout.rows( *sends )
            )
        );

        view.layout.spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'))
    }

    refresh {
        view.refresh;
        // what else goes here? after loading, for example
    }
}

NS_ChannelStripInView : SCViewHolder {

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;

        var ampFader = NS_ControlFader(controls['amp'], 0.1);

        var showButton = NS_Button([
            [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
        ]).fixedSize_(20).addLeftClickAction({ strip.toggleAllVisible });

        var muteButton = NS_ControlButton(controls['mute'], [
            [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
            [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
        ]).fixedSize_(20);

        var slotViews = strip.slots.size.collect({ |slotIndex|
            NS_ModuleSlotView(strip, slotIndex)
        });

        var nsServer = NSFW.servers[strip.stripGroup.server.name];

        var sends = nsServer.outMixer.collect({ |outStrip, i|
            var ctrl = controls[outStrip.stripId.asSymbol];

            NS_ControlButton(ctrl, [
                [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
            ]).font_( Font(*NS_Style('smallFont')) )
        });

        var inBus = NS_ControlText(controls[(strip.stripId ++ "_inBus").asSymbol])
        .maxHeight_(30);

        view = UserView()
        .maxHeight_(180)
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style('radius');

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(2);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(1), r, r);
            Pen.stroke;
        })
        .layout_(
            VLayout(
                inBus,
                VLayout( *slotViews ),
                HLayout(ampFader, showButton, muteButton).spacing_(0).margins_(0),
                HLayout( *sends )
            )
        );

        view.layout.spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'))
    }

    refresh {
        view.refresh;
        // what else goes here? after loading, for example
    }
}
