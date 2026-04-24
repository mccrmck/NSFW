NS_ControlRange : NS_Widget {
    var <>round;
    var lKnob, rKnob;
    var xPos;
    var drag;

    *new { |nsControlLeft, nsControlRight, round(0.01), orientation('horz')|
        orientation = orientation.switch(
            \horz,       { true },
            \horizontal, { true },
            \vert,       { false },
            \vertical,   { false },
            orientation
        );

        ^super.new.round_(round).drawWidget(nsControlLeft, nsControlRight, orientation)
    }

    drawWidget { |controlLeft, controlRight, orientation|

        if(orientation) 
        { this.drawHorizontalWidget(controlLeft, controlRight) } 
        { this.drawVerticalWidget(controlLeft, controlRight) }
    }

    drawHorizontalWidget { |controlLeft, controlRight|

        view = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
            var string;
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = h / 2;
            var normLeft = controlLeft.normValue * (w - h);
            var normRight = controlRight.normValue * (w - h);
            var b = NS_Style('border');
            var leftMapped = controlLeft.mapped;
            var rightMapped = controlRight.mapped;

            var bCol = case
            { leftMapped == 'listening' and: rightMapped == 'listening' } 
            { NS_Style('listening') }
            { leftMapped == 'mapped' and: rightMapped == 'mapped' }
            { NS_Style('assigned') }
            { NS_Style('bGroundDark') };

            var bColLeft = case
            { leftMapped == 'listening' }{ NS_Style('listening') }
            { leftMapped == 'mapped'    }{ NS_Style('assigned')  }
            { NS_Style('bGroundDark') };
            
            var bColRight = case
            { rightMapped == 'listening' }{ NS_Style('listening') }
            { rightMapped == 'mapped'    }{ NS_Style('assigned')  }
            { NS_Style('bGroundDark') };

            var stringL = controlLeft.label ++ 
            ": " ++ controlLeft.value.round(round).asString;
            var stringR = controlRight.label ++ 
            ": " ++ controlRight.value.round(round).asString;

            lKnob = Rect(normLeft, 0, h, h).insetBy(b / 2);
            rKnob = Rect(normRight, 0, h, h).insetBy(b / 2);

            Pen.fillColor_(NS_Style('highlight'));
            Pen.addRoundedRect(
                Rect(normLeft, 0, (normRight + h) - normLeft, h).insetBy(b), r, r
            );
            Pen.fill;
            
            Pen.width_(b / 2);
            Pen.strokeColor_(bColLeft);
            Pen.addOval(lKnob);
            Pen.fillStroke;

            Pen.strokeColor_(bColRight);
            Pen.addOval(rKnob);
            Pen.fillStroke;

            Pen.strokeColor_(bCol);
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.stroke;

            // add strings for controlValues
            [stringL, stringR].do { |str, i|
                var left = [0, w * 2 / 3].at(i);

                Pen.stringCenteredIn(
                    str,
                    Rect(left, 0, w / 3, h),
                    Font(*NS_Style('defaultFont')),
                    NS_Style('textLight')
                )
            };

            Pen.stroke;
        })
        .mouseDownAction_({ |...args| this.onMouseDown(*args) })
        .mouseMoveAction_({ |v, x, y, modifiers|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = v.bounds.height / 2;
            var xVal = x.linlin(r, w - r, 0, w) / w;

            drag.switch(
                'left',  {
                    xVal = xVal.clip(0, (rKnob.left - h)/ (w - h));
                    controlLeft.normValue_(xVal)
                },
                'right', { 
                    xVal = xVal.clip(lKnob.right / (w - h), 1);
                    controlRight.normValue_(xVal) 
                },
                'both',  {
                    var delta = xVal - xPos;
                    var leftNorm = controlLeft.normValue;
                    var rightNorm = controlRight.normValue;
                    var diff = rightNorm - leftNorm;
                    var val = leftNorm + delta;
                    controlLeft.normValue_(val);
                    controlRight.normValue_(val + diff);
                    xPos = xVal
                }
            )
        });

        this.addLeftClickAction({ |rng, v, x, y|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = h / 2;
            var lNorm = controlLeft.normValue;
            var rNorm = controlRight.normValue;
            var xVal = x.linlin(r, w - r, 0, w) / w;
            var ltLeft = xVal < lNorm;
            var gtRight = xVal > rNorm;

            xPos = xVal;
            case
            { lKnob.containsPoint(x@y) or: ltLeft } { 
                drag = 'left'; controlLeft.normValue_(xVal) 
            }
            { rKnob.containsPoint(x@y) or: gtRight }
            { drag = 'right'; controlRight.normValue_(xVal) }
            { drag = 'both' }
        });
        this.addDoubleClickAction({ |...args| 
            mouseActionDict['none']['leftClick'].value(*args)
        });
        this.addLeftClickAction({ |rng, v, x, y|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var lNorm = controlLeft.normValue;
            var rNorm = controlRight.normValue;

            case
            { lKnob.containsPoint(x@y) } { controlLeft.toggleAutoAssign }
            { rKnob.containsPoint(x@y) } { controlRight.toggleAutoAssign }
            { controlLeft.toggleAutoAssign; controlRight.toggleAutoAssign }

        }, 'shift');
        //this.addRightClickAction({ control.openControlMenu });

        controlLeft.addAction("qtRange" ++ this.hash, { { view.refresh }.defer  });
        controlRight.addAction("qtRange" ++ this.hash, { { view.refresh }.defer  });
        view.onClose_({ 
            controlLeft.removeAction("qtRange" ++ this.hash);
            controlRight.removeAction("qtRange" ++ this.hash);
        })
    }

    //var val = if(orientation.not) { 1 - (y / v.bounds.height).clip(0, 1) };
    drawVerticalWidget {

        "vertical range not implemented yet".warn;
    }
}
