NS_ReceiveView : NS_Widget {

    *new { |...nsControls|
        ^super.new.drawWidget(nsControls)
    }

    drawWidget { |controls|

        view = UserView()
        .layout_(
            HLayout(
                *controls.collect { |c| 
                    NS_RoutingSlot(c) 
                    .addRightClickAction({
                        this.mouseActionDict['none']['rightClick'].value
                    })
                }
            ).nsMarginsSpacing('inner')
        )
        .mouseDownAction_({ |...args| this.onMouseDown(*args) });
        
        this.addDoubleClickAction({ mouseActionDict['none']['leftClick'].value });
        this.addLeftClickAction({ /* disable left click */});
        this.addRightClickAction({ "recv".postln });
    }
}

NS_SendView : NS_Widget {

    *new { |...nsControls|
        ^super.new.drawWidget(nsControls)
    }

    drawWidget { |controls|

        view = UserView()
        .layout_(
            HLayout(
                *controls.collect { |c| 
                    NS_RoutingSlot(c)
                    .addRightClickAction({
                        this.mouseActionDict['none']['rightClick'].value
                    })
                }
            ).nsMarginsSpacing('inner')
        )
        .mouseDownAction_({ |...args| this.onMouseDown(*args) });

        this.addDoubleClickAction({ mouseActionDict['none']['leftClick'].value });
        this.addLeftClickAction({ /* disable left click */});
        this.addRightClickAction({ "send".postln });
    }

}

// make Routing Node with properites/states:
// send/receive(can receive?)

NS_RoutingSlot : NS_ControlWidget {

    *new { |nsControl|
        ^super.new.drawWidget(nsControl)
    }

    drawWidget { |control|
        var scale = 1;

        view = UserView()
        .maxSize_(20)
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
            var val = (control.value + 1).asInteger.wrap(0, 1);
            control.value_(val);
            scale = 0.93;
        });
        this.addDoubleClickAction({ mouseActionDict['none']['leftClick'].value });
        this.addLeftClickAction({ /* ignore autoAssign shortcut */ }, 'shift');
        
        // this key must be different for the stripView and the ContextMenu
        control.addAction(\qtGui,{ { view.refresh }.defer })
    }
}
