NS_ChannelStripMatrixView : NS_Widget {

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;
        var slotViews = strip.slots.size.collect({ |slotIndex| 
            NS_ModuleSlotView(strip, slotIndex)
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
                UserView()
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
                }),
                HLayout( 
                    *4.collect({ |i| 
                        // this mess == (vol, mute) + slots + sends + index
                        NS_ControlSink(controls[2 + slotViews.size + 4 + i])
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

                            win.layout_(
                                HLayout(
                                    NS_ContainerView().layout_(
                                        VLayout(
                                            // this mess == (vol, mute) + slots + sends + inputSinks + index
                                            NS_ControlFader(controls[2 + slotViews.size + 4 + 4 + i], 0.01, 'vert'),
                                            NS_Button([
                                                [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                                                [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                                            ]).maxHeight_(20),
                                            NS_Button([
                                                [NS_Style('clear'), NS_Style('textLight'), NS_Style('bGroundDark')]
                                            ])
                                            .maxHeight_(20)
                                            .addLeftClickAction({ win.close })
                                        ).spacing_(0).margins_(0)
                                    )
                                ).spacing_(0).margins_(0)
                            );

                            win.front;
                        })
                    })
                ),
                VLayout( *slotViews ),
                HLayout(
                    NS_ControlFader(controls[0], 0.1),
                    NS_Button([
                        [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
                    ])
                    .fixedSize_(20)
                    .addLeftClickAction({ strip.toggleAllVisible }),
                    NS_ControlButton(controls[1], [
                        [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                        [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                    ]).fixedSize_(20),
                ).spacing_(0).margins_(0),
                HLayout(
                    *4.collect({ |i|
                        var ctrl = controls[2 + strip.slots.size + i];

                        NS_ControlButton(ctrl, [
                            [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                            [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
                        ]).font_(Font(*NS_Style('smallFont'))).maxWidth_(30)
                    })
                )
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
        var slotViews = strip.slots.size.collect({ |slotIndex| 
            NS_ModuleSlotView(strip, slotIndex)
        });

        view = View().layout_(
            VLayout(
                UserView()
                .minHeight_(strip.stripId.bounds(Font(*NS_Style('defaultFont'))).height + 2)
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var rect = Rect(0, 0, w, h);

                    Pen.stringCenteredIn(
                        strip.stripId,
                        rect,
                        Font(*NS_Style('defaultFont')),
                        NS_Style('textLight')
                    )
                }),
                VLayout( *slotViews ),
                HLayout( 
                    NS_ControlFader(controls[0], 0.1),
                    NS_Button([
                        [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
                    ])
                    .fixedSize_(20)
                    .addLeftClickAction({ strip.toggleAllVisible }),
                    NS_ControlButton(controls[1], [
                        [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                        [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                    ]).fixedSize_(20),
                ).spacing_(0).margins_(0),
                GridLayout.rows(
                    *controls[(2 + slotViews.size)..].collect({ |ctrl|
                        NS_ControlButton(ctrl, [
                            [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                            [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
                        ]).font_( Font(*NS_Style('smallFont')) )
                    }).clump(4);
                )
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
        var nsServer = NSFW.servers[strip.stripGroup.server.name];
        var controls = strip.controls;
        var slotViews = strip.slots.size.collect({ |slotIndex|
            NS_ModuleSlotView(strip, slotIndex)
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
                NS_ControlText(controls.last)
                .maxHeight_(30),
                VLayout( *slotViews ),
                HLayout( 
                    NS_ControlFader(controls[0], 0.1),
                    NS_Button([
                        [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
                    ])
                    .fixedSize_(20)
                    .addLeftClickAction({ strip.toggleAllVisible }),
                    NS_ControlButton(controls[1], [
                        [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                        [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                    ]).fixedSize_(20)
                ).spacing_(0).margins_(0),
                HLayout(
                    *4.collect({ |i|
                        var ctrl = controls[2 + slotViews.size + i];

                        NS_ControlButton(ctrl, [
                            [ctrl.label, NS_Style('textDark'), NS_Style('highlight')],
                            [ctrl.label, NS_Style('textLight'), NS_Style('bGroundDark')]
                        ]).font_( Font(*NS_Style('smallFont')) )
                    })
                )
            )
        );

        view.layout.spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'))
    }

    refresh {
        view.refresh;
        // what else goes here? after loading, for example
    }
}
