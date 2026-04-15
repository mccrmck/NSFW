NS_Window : SCViewHolder {
    var containerView;
    var draggable = false;
    var menuX, menuY;

    *new { |title(""), bounds|
        ^super.new.init(title, bounds)
    }

    init { |winTitle, winBounds|
        var buttSize = 16;

        var menuBar = UserView()
        .fixedHeight_(buttSize + (NS_Style('viewMargins')[1] * 2)) 
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;

            Pen.fillColor_( NS_Style('darklight') );
            Pen.addRoundedRect(Rect(0, 0, w, h), r, r);
            Pen.fill;
        })
        .mouseDownAction_({ |v, x, y|
            menuX = x; menuY = y;
            draggable = true
        })
        .mouseUpAction_({ draggable = false })
        .mouseMoveAction_({ |v, x, y, mod|
            var newX = x - menuX;
            var newY = y - menuY;

            var bounds  = view.bounds;
            var newLeft = bounds.left + newX;
            var newTop  = bounds.top - newY;

            if(draggable) {
                view.bounds_(Rect(newLeft, newTop, bounds.width, bounds.height))
            }
        })
        .layout_(
            HLayout(
                // close window
                NS_Button([
                    [NS_Style('clear'), NS_Style('textDark'), NS_Style('red')]
                ])
                .fixedSize_(buttSize)
                .addLeftClickAction({ view.close }),
                // some other functions?
                NS_Button([
                    ["", NS_Style('textDark'), NS_Style('orange')]
                ])
                .fixedSize_(buttSize)
                .addLeftClickAction({  }),
                // expand/contract window?
                NS_Button([
                    ["", NS_Style('textDark'), NS_Style('yellow')]
                ])
                .fixedSize_(buttSize)
                .addLeftClickAction({  }),
                nil,
                if(winTitle.size > 0) 
                { 
                    StaticText().string_(winTitle)
                    .stringColor_(NS_Style('highlight')) 
                }
                { nil },
                nil

            ).nsMarginsSpacing('view')
        );

        containerView = UserView();

        view = Window(bounds: winBounds, border: false )
        .background_( NS_Style('transparent') );
        view.layout_(
            VLayout(
                UserView()
                .layout_(
                    VLayout(menuBar, containerView).nsMarginsSpacing('view', 'inner')
                    //.spacing_(0).margins_([4,2])
                )
                .drawFunc_({ |v|
                    var w = v.bounds.width;
                    var h = v.bounds.height;
                    var r = NS_Style('radius');
                    var b = NS_Style('border');

                    Pen.fillColor_( NS_Style('highlight') );
                    Pen.strokeColor_(NS_Style('bGroundDark'));
                    Pen.width_(b);
                    Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
                    Pen.fillStroke;
                })
            ).nsMarginsSpacing(0) // ensures resize triggers are at corners of UserView
        )
    }

    // methods for getting/setting the layout:

    layout_ { |newLayout|
        containerView.layout_(newLayout);
        newLayout.nsMarginsSpacing('inner')
    }
}
