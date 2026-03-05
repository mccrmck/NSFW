NS_Window : NS_Widget {

    *new { |bounds|
        ^super.new.init(bounds)
    }

    init { |bounds|
        var inset = NS_Style('inset') * 2;
        var r = NS_Style('radius');

        var menuBar = View()
        .background_( NS_Style('darklight') )
        .maxHeight_(30)
        .layout_(
            HLayout(
                // close window
                NS_Button([
                    [NS_Style('clear'), NS_Style('textDark'), NS_Style('red')]
                ])
                .fixedSize_(20)
                .addLeftClickAction({ view.close }),
                // some otehr function?
                NS_Button([
                    ["?", NS_Style('textDark'), NS_Style('orange')]
                ])
                .fixedSize_(20)
                .addLeftClickAction({  }),
                // expand/contract window?
                NS_Button([
                    ["⇳", NS_Style('textDark'), NS_Style('yellow')]
                ])
                .fixedSize_(20)
                .addLeftClickAction({  }),

                nil,
            )
            .spacing_( NS_Style('viewSpacing') )
            .margins_( NS_Style('viewMargins') ),
        );

        view = Window(bounds: bounds, border: false )
        .background_( NS_Style('transparent') ).front;
        view.layout_(
            VLayout(
                UserView()
                .layout_(
                    VLayout(
                        menuBar,
                        nil
                        //NS_Text(),
                        //NS_Button(),
                        //NS_Text(),
                        //Slider2D().thumbSize_(12),
                        //NS_Button(),

                    )
                    .spacing_( NS_Style('viewSpacing') )
                    .margins_( NS_Style('viewMargins') )
                )
                .drawFunc_({ |v|
                    var rect = v.bounds.insetBy(inset/2);
                    var w = rect.bounds.width;
                    var h = rect.bounds.height;

                    Pen.fillColor_( NS_Style('highlight') );
                    Pen.strokeColor_(NS_Style('bGroundDark'));
                    Pen.width_( inset );
                    Pen.addRoundedRect(rect, r, r);
                    Pen.fillStroke;
                })
            ).spacing_(0).margins_(0)
        )
    }

    // methods for getting/setting the layout:

    layout  {}
    layout_ {}
}
