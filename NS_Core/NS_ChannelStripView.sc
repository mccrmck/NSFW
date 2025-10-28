NS_ChannelStripMatrixView : NS_Widget {

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

        var ampFader = NS_ControlFader(controls[0], 0.1);

        var showButton = NS_Button([
            [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
        ]).fixedSize_(20).addLeftClickAction({ strip.toggleAllVisible });

        var muteButton = NS_ControlButton(controls[1], [
            [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
            [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
        ]).fixedSize_(20);

        var slotViews = strip.slots.size.collect({ |slotIndex| 
            NS_ModuleSlotView(strip, slotIndex)
        });

        var receives = 4.collect({ |i| 
            // this mess == (vol, mute) + slots + index
            NS_ControlSink(controls[2 + slotViews.size + i])
            .addLeftClickAction({})
            .addRightClickAction({ |cSink, view, x, y|
                var aBounds = view.absoluteBounds;
                var screenHeight = Window.availableBounds.height;
                var win = Window(
                    bounds:Rect(aBounds.left, screenHeight - aBounds.top - 80, 40, 120), 
                    resizable: false,
                    border: false
                )
                .background_(NS_Style('transparent'));

                // close window when returning to other window
                win.view.endFrontAction_({ win.close }); 

                win.layout_(
                    HLayout(
                        NS_ContainerView().layout_(
                            VLayout(
                                // this mess == (vol, mute) + slots + sinks + index
                                NS_ControlFader(controls[2 + slotViews.size + 4 + i], 0.01, 'vert'),
                                NS_Button([
                                    [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                                    [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                                ]).maxHeight_(20),
                                NS_Button([
                                    [NS_Style('clear'), NS_Style('textLight'), NS_Style('bGroundDark')]
                                ]).maxHeight_(20).addLeftClickAction({ win.close })
                            ).spacing_(0).margins_(0)
                        )
                    ).spacing_(0).margins_(0)
                );

                win.front;
            })
        });

        var sends = 4.collect({ |i|
            // this mess == (vol, mute) + slots + sinks + sinkAmps + index
            var ctrl = controls[2 + slotViews.size + 4 + 4 + i];

            NS_ControlButton(ctrl, [
                [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
            ]).font_(Font(*NS_Style('smallFont'))).maxWidth_(30)
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

NS_ChannelStripOutView : NS_Widget { 

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

        var ampFader = NS_ControlFader(controls[0], 0.1);

        var showButton = NS_Button([
            [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
        ]).fixedSize_(20).addLeftClickAction({ strip.toggleAllVisible });
        
        var muteButton = NS_ControlButton(controls[1], [
            [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
            [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
        ]).fixedSize_(20);

        var slotViews = strip.slots.size.collect({ |slotIndex| 
            NS_ModuleSlotView(strip, slotIndex)
        });

        var sends = controls[(2 + slotViews.size)..].collect({ |ctrl|
            NS_ControlButton(ctrl, [
                [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
            ]).font_( Font(*NS_Style('smallFont')) )
        }).clump(4);

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


NS_ChannelStripInView : NS_Widget {

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;

        var ampFader = NS_ControlFader(controls[0], 0.1);

        var showButton = NS_Button([
            [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
        ]).fixedSize_(20).addLeftClickAction({ strip.toggleAllVisible });

        var muteButton = NS_ControlButton(controls[1], [
            [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
            [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
        ]).fixedSize_(20);

        var slotViews = strip.slots.size.collect({ |slotIndex|
            NS_ModuleSlotView(strip, slotIndex)
        });

        var inBus = NS_ControlText(controls[2 + slotViews.size]).maxHeight_(30);

        var sends = 4.collect({ |i|
            var ctrl = controls[2 + slotViews.size + 1 + i];

            NS_ControlButton(ctrl, [
                [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
            ]).font_( Font(*NS_Style('smallFont')) )
        });

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
