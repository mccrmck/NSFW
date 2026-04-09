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
                .drawFunc_({ |view|
                    var mainCol = NS_Style('mainColor');
                    var bgCol = NS_Style('bGroundDark');
                    var v = view.bounds;
                    var w = v.width;
                    var h = v.height;
                    var r = NS_Style('radius');
                    var b = NS_Style('border');

                    Pen.addRoundedRect(Rect(0, 0, w, h), r, r);
                    Pen.clip;

                    Pen.addRect(Rect(v.left, v.top, w / 2, h / 2) );
                    Pen.fillAxialGradient(v.leftTop, v.rightBottom, bgCol, mainCol);
                    Pen.addRect(Rect(v.left, v.top + (h / 2), w / 2, h / 2));
                    Pen.fillAxialGradient(v.leftBottom, v.rightTop, bgCol, mainCol);

                    Pen.addRect(Rect(v.left + (w / 2), v.top, w / 2, h / 2));
                    Pen.fillAxialGradient(v.rightTop, v.leftBottom, bgCol, mainCol);
                    Pen.addRect(Rect(v.left + (w / 2), v.top + (h / 2), w / 2, h / 2));
                    Pen.fillAxialGradient(v.rightBottom, v.leftTop, bgCol, mainCol);
                    
                    Pen.width_(b);
                    Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
                    Pen.stroke;
                })
                .layout_(layout.nsMarginsSpacing(NS_Style('border') / 2))
            ).nsMarginsSpacing('inner')
        );

        view.endFrontAction_({ view.close });
        view.front;
    }
}
