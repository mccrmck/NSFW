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
                })
                .beginDragAction_({ strip.stripId }),
                HLayout( 
                    *4.collect({ |i| 
                        NS_ControlSink(controls[3 + slotViews.size + 4 + i]) // what a mess
                        .addLeftClickAction({})
                        .addRightClickAction({
                            Menu(
                                View()
                                .background_(NS_Style('bGroundDark'))
                                .layout_(
                                    HLayout(
                                        NS_ControlFader(controls[3 + slotViews.size + 4 + 4 + i]) // what a mess)
                                        .fixedWidth_(90)
                                    ).margins_(0).spacing_(0)
                                )
                            ).front
                        })
                    })
                ),
                VLayout( *slotViews ),
                NS_ControlFader(controls[0], 0.1),
                HLayout( 
                    NS_ControlButton(controls[1], [
                        [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
                    ]),
                    NS_ControlButton(controls[2], [
                        [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                        [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                    ]),
                ),
                HLayout(
                    *4.collect({ |i|
                        var ctrl = controls[3 + strip.slots.size + i];

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
                NS_ControlFader(controls[0], 0.1),
                HLayout( 
                    NS_ControlButton(controls[1], [
                        [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
                    ]),
                    NS_ControlButton(controls[2], [
                        [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                        [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                    ]),
                ),
                GridLayout.rows(
                    *controls[(3 + strip.slots.size)..].collect({ |ctrl|
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
                NS_ControlFader(controls[0], 0.1),
                HLayout( 
                    NS_ControlButton(controls[1], [
                        [NS_Style('show'), NS_Style('textDark'), NS_Style('yellow')]
                    ]),
                    NS_ControlButton(controls[2], [
                        [NS_Style('mute'), NS_Style('red'), NS_Style('bGroundDark')],
                        [NS_Style('play'), NS_Style('green'), NS_Style('bGroundDark')]
                    ])
                ),
                HLayout(
                    *4.collect({ |i|
                        var ctrl = controls[3 + strip.slots.size + i];

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
