NS_ReceiveView : NS_Widget {

    *new { |...nsControls|
        ^super.new.drawWidget(nsControls)
    }

    drawWidget { |controls|

        view = UserView()
        .layout_(
            HLayout(
                *controls.collect { |c| 
                    NS_RoutingToggle(c) 
                    .addRightClickAction({
                        this.mouseActionDict['none']['rightClick'].value
                    })
                }
            ).nsMarginsSpacing('inner')
        )
        .mouseDownAction_({ |...args| this.onMouseDown(*args) });

        this.addDoubleClickAction({ mouseActionDict['none']['leftClick'].value });
        this.addLeftClickAction({ /* disable left click */ });
        this.addRightClickAction({ "recv".postln });
    }
}

NS_SendView : NS_Widget {

    *new { |toggleControls, knobControls|
        ^super.new.drawWidget(toggleControls.asArray, knobControls.asArray)
    }

    drawWidget { |toggles, knobs|

        if(toggles.size != knobs.size) 
        { "toggles and knobs must be the same size".throw };

        view = UserView()
        .layout_(
            HLayout(
                *toggles.size.collect { |i| 
                    NS_RoutingSlot(toggles[i], knobs[i])
                    .addRightClickAction({ |slot, v, x, y|
                        this.mouseActionDict['none']['rightClick'].value(slot, view)
                    })
                }
            ).nsMarginsSpacing('inner')
        )
        .mouseDownAction_({ |...args| this.onMouseDown(*args) });

        this.addDoubleClickAction({ mouseActionDict['none']['leftClick'].value });
        this.addLeftClickAction({ /* disable left click */ });
    }
}


NS_RoutingView : SCViewHolder {

    *new { |stripToggles, stripKnobs, outStripToggles, outStripKnobs|
        ^super.new.init(stripToggles, stripKnobs, outStripToggles, outStripKnobs)
    }

    init { |toggles, knobs, outToggles, outKnobs|

        if(toggles.size != knobs.size) 
        { "must be an equal number of toggle and knob controls".throw };

        if(outToggles.size != outKnobs.size) 
        { "must be an equal number of outToggle and outKnob controls".throw };

        view = UserView()
        .layout_(
            VLayout(
                NS_Header("sends").maxHeight_(20),
                GridLayout.rows(
                    *toggles.size.collect { |i|
                        NS_RoutingSlot(toggles[i], knobs[i])
                    }.clump(4)
                ).nsMarginsSpacing('inner'),
                NS_HDivider(),
                HLayout( 
                    *outToggles.size.collect { |i|
                        NS_RoutingSlot(outToggles[i], outKnobs[i])
                    } 
                ).nsMarginsSpacing('view'),
            ).nsMarginsSpacing('inner')
        )
    }
}

// needs a more descriptive name!
NS_RoutingSlot : NS_Widget {
    var draggable = false;

    *new { |nsControlToggle, nsControlKnob|
        ^super.new.drawWidget(nsControlToggle, nsControlKnob)
    }

    drawWidget { |controlToggle, controlKnob|
        var scale = 1;

        view = UserView()
        .minSize_(21)
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var d = w.min(h);
            var r = d / 2;
            var b = NS_Style('border');
            var inset = 0.6;

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(b);

            Pen.scale(scale, scale);
            Pen.translate((1-scale) * w / 2, (1-scale) * h / 2);

            if(controlToggle.value == 1) {
                // draw visual indicator
                Pen.fillColor_(NS_Style('blue'));
                Pen.addRoundedRect(
                    Rect(
                        (w / 2) - (r * inset), 
                        (h / 2) - (r * inset), 
                        d * inset,
                        d * inset
                    ).insetBy(b / 2), r, r
                );
                Pen.fillStroke;

                // draw integrated knob for gain control
                Pen.fillColor_(NS_Style('highlight'));
                Pen.addAnnularWedge(
                    Rect(0, 0, w, h).insetBy(b).center,
                    r * inset,
                    r - (b / 2),
                    pi / 2,
                    controlKnob.normValue.sqrt * 2pi
                );
                Pen.fill;

                // draw 3dB pips
                //36.do { |i|
                //    var spec = \db.asSpec;
                //    var dB = spec.unmap(i * 3.neg).sqrt;
                //
                //    Pen.addAnnularWedge(
                //        Rect(0, 0, w, h).insetBy(b).center,
                //        dB.linexp(0, 1, r, r * inset * 1.25),
                //        r - (b / 2),
                //        (dB * 2pi) + (pi / 2),
                //        1/360,
                //    );
                //    Pen.stroke;
                //};
            };

            // draw button perimeter
            Pen.fillColor_(NS_Style('transparent'));
            Pen.addRoundedRect(
                Rect((w / 2) - r, (h / 2) - r, d, d).insetBy(b / 2), r, r
            );
            Pen.fillStroke;
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseUpAction_({ scale = 1; draggable = false; view.refresh })
        .mouseMoveAction_({ |v, x, y, mod|
            if(draggable) {

                // instead of making this jump to the value of y,
                // increment on the current value with some small interval
                controlKnob.normValue_( 1 - (y / v.bounds.height).clip(0, 1) )
            }
        });

        this.addLeftClickAction({
            var val = (controlToggle.value + 1).wrap(0, 1);
            controlToggle.value_(val);
            scale = 0.93;
        });
        this.addLeftClickAction({ 
            if(controlToggle.value == 1) { draggable = true }
        }, 'alt');
        this.addDoubleClickAction({ mouseActionDict['none']['leftClick'].value });

        this.addLeftClickAction({ /* ignore autoAssign shortcut */ }, 'shift');

        controlToggle.addAction("qtToggle" ++ this.hash, { { view.refresh }.defer });
        controlKnob.addAction("qtKnob" ++ this.hash, { { view.refresh }.defer });
        view.onClose_({
            controlToggle.removeAction("qtToggle" ++ this.hash);
            controlKnob.removeAction("qtKnob" ++ this.hash)
        })
    }
}

NS_RoutingToggle : NS_Widget {

    *new { |nsControl|
        ^super.new.drawWidget(nsControl)
    }

    drawWidget { |control|
        var scale = 1;

        view = UserView()
        .maxSize_(36)
        .minSize_(8)
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var d = w.min(h);
            var r = d / 2;
            var b = NS_Style('border');

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(b);

            Pen.scale(scale, scale);
            Pen.translate((1-scale) * w / 2, (1-scale) * h / 2);

            (control.value + 1).do { |i|
                var inset = [1, 0.6].at(i);
                var col = [NS_Style('transparent'), NS_Style('blue')].at(i);

                Pen.fillColor_(col);

                Pen.addRoundedRect(
                    Rect(
                        (w / 2) - (r * inset), 
                        (h / 2) - (r * inset), 
                        d * inset,
                        d * inset
                    ).insetBy(b / 2), r, r
                );
                Pen.fillStroke
            }
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseUpAction_({ scale = 1; view.refresh });

        this.addLeftClickAction({
            var val = (control.value + 1).wrap(0, 1);
            control.value_(val);
            scale = 0.93;
        });
        this.addDoubleClickAction({ mouseActionDict['none']['leftClick'].value });
        this.addLeftClickAction({ /* ignore autoAssign shortcut */ }, 'shift');

        control.addAction("qtRecvToggle" ++ this.hash,{ { view.refresh }.defer });
        view.onClose_({ control.removeAction("qtRecvToggle" ++ this.hash) })
    }
}

