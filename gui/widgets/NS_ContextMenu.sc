NS_ContextMenu : NS_Widget {

    *new { |parent, bounds, layout|
        ^super.new.drawWidget(parent, bounds, layout)
    }

    drawWidget { |parent, bounds, layout|
        var aBounds = parent.absoluteBounds;
        var screenHeight = Window.availableBounds.height;
        var position = Rect(aBounds.left, screenHeight - aBounds.top, 0, 0) + bounds;

        view = Window(bounds: position, resizable: false, border: false)
        .background_( NS_Style('transparent') );

        layout = layout !? layout ?? HLayout();

        view.layout_(
            HLayout(
                UserView()
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var r = NS_Style('radius');

                    Pen.fillColor_( NS_Style('darklight') );
                    Pen.addRoundedRect(Rect(0, 0, w, h), r, r);
                    Pen.fill;
                })
                .layout_(layout)
            ).nsMarginsSpacing(0)
        );

        view.endFrontAction_({ view.close });
        view.front;
    }
}
