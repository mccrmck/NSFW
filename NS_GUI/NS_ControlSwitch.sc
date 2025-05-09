NS_ControlSwitch : NS_ControlWidget {

    *new { |ns_control, labelArray, numColumns = 1|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control, labelArray, numColumns.max(1))
    }

    init { |control, labels, columns|
        var inset = NS_Style.inset;
        var font = Font(*NS_Style.defaultFont);
        var labelRows = labels.clump(columns.asInteger);
        var buttons;

        view = UserView()
        .drawFunc_({ |v|
            var string;
            var value = control.value;
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2 / labelRows.size;

            var borderFill = if(isHighlighted,{ 
                [NS_Style.assigned, NS_Style.assigned]
            },{
                [NS_Style.bGroundDark, NS_Style.transparent]
            });

            buttons = labelRows.collect({ |row, rowIndex|
                var width  = w / row.size;
                var height = h / labelRows.size;

                row.collect({ |label, columnIndex|
                    var left = columnIndex * width;
                    var top  = height * rowIndex;

                    Rect(inset + left, inset + top, width, height)
                });
                
            }).flat;

            Pen.width_(inset);

            Pen.strokeColor_(borderFill[0]); 
            Pen.fillColor_(borderFill[1]);
            Pen.addRoundedRect(Rect(inset / 2, inset / 2, w + inset, h + inset), r, r);
            Pen.fillStroke;

            buttons.do({ |rect, index|
                var stringCol, fillCol;
                if(value == index,{
                    stringCol = NS_Style.textDark;
                    fillCol   = NS_Style.bGroundLight;
                },{
                    stringCol = NS_Style.textLight;
                    fillCol   = NS_Style.bGroundDark;
                });
                Pen.strokeColor_(NS_Style.bGroundDark);
                Pen.fillColor_(fillCol);
                Pen.addRoundedRect(rect, r, r);
                Pen.fillStroke;
                Pen.stringCenteredIn(
                    labels[index].asString, rect, font, stringCol
                );
                Pen.stroke
            });
        })
        .mouseDownAction_({ |v, x, y, modifiers, buttonNumber, clickCount|

            if(buttonNumber == 0,{
                if(clickCount == 1,{

                    buttons.do({ |rect, index|
                       if(rect.containsPoint(x@y),{ 
                           control.value_(index)
                       })
                    })
                    
                },{
                    this.toggleAutoAssign(control, 'discrete')
                });
            },{
                this.openControlMenu(control, 'discrete')
            });

            view.refresh;
        });

        control.addAction(\qtGui,{ |c| { view.refresh }.defer });
    }
}
