NS_Window : SCViewHolder {
    var containerView;
    var draggable = false;
    var menuX, menuY;

    *new { |title = "", bounds|
        ^super.new.init(title, bounds)
    }

    init { |winTitle, winBounds|
        var inset = NS_Style('inset');
        var r = NS_Style('radius');
        var buttSize = 20;

        var menuBar = UserView()
        .fixedHeight_(buttSize + inset + inset)
        .mouseDownAction_({ |v, x, y|
            menuX = x; menuY = y;
            draggable = true
        })
        .mouseUpAction_({ draggable = false })
        .mouseMoveAction_({ |v, x, y, mod|
            var newX = x - menuX;
            var newY = y - menuY;

            var bounds = view.bounds;
            var newLeft = bounds.left + newX;
            var newTop = bounds.top - newY;

            if(draggable) {
                view.bounds_(Rect(newLeft, newTop, bounds.width, bounds.height))
            }
        })
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var rad = w.min(h) / 2;

            Pen.fillColor_( NS_Style('darklight') );
            Pen.addRoundedRect(Rect(inset, inset, w, h), rad, rad);
            Pen.fill;
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

            )
            .spacing_(NS_Style('viewSpacing'))
            .margins_(inset)
        );

        containerView = UserView();

        view = Window(bounds: winBounds, border: false )
        .background_( NS_Style('transparent') );
        view.layout_(
            VLayout(
                UserView()
                .layout_(
                    VLayout(
                        menuBar,
                        containerView
                    )
                    .spacing_(0)
                    .margins_([4,2] + inset)
                )
                .drawFunc_({ |v|
                    var rect = v.bounds.insetBy(inset);
                    var w = rect.width;
                    var h = rect.height;

                    Pen.fillColor_( NS_Style('highlight') );
                    Pen.strokeColor_(NS_Style('bGroundDark'));
                    Pen.width_( inset );
                    Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
                    Pen.fillStroke;
                })
            )
            // this ensures the resize triggers are at the corners of the UserView
            .spacing_(0).margins_(0) 
        )
    }

    // methods for getting/setting the layout:

    layout_ { |newLayout|
        containerView.layout_(newLayout);
        newLayout.spacing_(0).margins_(0);
    }
}
