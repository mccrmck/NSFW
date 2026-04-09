NS_ControlSwitch : NS_ControlWidget {

    *new { |ns_control, labelArray, numColumns = 1|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.drawWidget(ns_control, labelArray, numColumns.max(1))
    }

    drawWidget { |control, labels, columns|
        var labelRows = labels.clump(columns.asInteger);
        var buttons;

        view = UserView()
        .minHeight_(20)
        .minWidth_(40)
        .drawFunc_({ |v|
            var string;
            var value = control.value;
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');

            var bCol = case
            { control.mapped == 'listening' }{ NS_Style('listening') }
            { control.mapped == 'mapped'    }{ NS_Style('assigned')  }
            { NS_Style('bGroundDark') };

            Pen.addRoundedRect(Rect(0, 0, w, h), r + (b / 2), r + (b / 2));
            Pen.clip;

            buttons = labelRows.collect({ |row, rowIndex|
                var width  = w / row.size;
                var height = h / labelRows.size;

                row.collect({ |label, columnIndex|
                    var left = columnIndex * width;
                    var top  = height * rowIndex;

                    Rect(left, top, width, height)
                });

            }).flat;

            buttons.do({ |rect, index|
                var stringCol, fillCol;
                if(value == index)
                {
                    stringCol = NS_Style('textDark');
                    fillCol   = NS_Style('bGroundLight');
                }
                {
                    stringCol = NS_Style('textLight');
                    fillCol   = NS_Style('bGroundDark');
                };
                Pen.strokeColor_(NS_Style('bGroundDark'));
                Pen.fillColor_(fillCol);
                Pen.fillRect(rect);
                Pen.stringCenteredIn(
                    labels[index].asString, 
                    rect, 
                    Font(*NS_Style('defaultFont')),
                    stringCol
                );
                Pen.stroke
            });

            Pen.strokeColor_(bCol); 
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.stroke;
        })
        .beginDragAction_({ control })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseMoveAction_({ |v, x, y, modifiers|
            buttons.do({ |rect, index|
                if(rect.containsPoint(x@y) and: { control.value != index },{
                    control.value_(index)
                })
            })
        });

        this.addLeftClickAction({ |switch, v, x, y|
            buttons.do({ |rect, index|
                if(rect.containsPoint(x@y),{ control.value_(index) })
            })
        });
        this.addDoubleClickAction({ |...args| 
            mouseActionDict['none']['leftClick'].value(*args)
        });
        this.addLeftClickAction({ this.toggleAutoAssign(control) }, 'shift');
        this.addRightClickAction({ this.openControlMenu(control) });
        this.addLeftClickAction({ view.beginDrag }, 'cmd');

        control.addAction("qtSwitch" ++ this.hash, { |c| { view.refresh }.defer });
        view.onClose_({ control.removeAction("qtSwitch" ++ this.hash) })
    }
}
