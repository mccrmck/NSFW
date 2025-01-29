NS_Window {
    classvar <radius = 16, <inset = 12;
    classvar minWidth = 120, minHeight = 120;

    var <win, <uView;
    var <background;
    var reSizeBool = false, moveBool = false, cornerMask = #[false, false, false, false];
    var lastMouseX = 0, lastMouseY = 0;

    *new { |bounds|
        ^super.new.init(bounds)
    }

    init { |bounds|

        background = Color.white.alpha_(0.2);
        win = Window(bounds: bounds, border: false ).background_( Color.clear ).front;
        win.view.acceptsMouseOver_(true);
        uView = UserView(win, win.bounds.width@win.bounds.height).resize_(5);
        uView.drawFunc_({
            var h = win.bounds.height;
            var w = win.bounds.width;
            var i = inset/2;
            var points = [
                w@0 + [i.neg,i], // rightTop
                w@h - i,         // rightBot
                0@h + [i,i.neg], // leftBot
                0@0 + i,         // leftTop
            ];
            var lastPoint = points.last;
            uView.bounds = w@h;
            
            Pen.strokeColor = Color.black;
            Pen.fillColor_( background );
            Pen.width_( inset );
            Pen.moveTo( points[points.size - 2] - (0@radius) );

            points.do({ |point,i|
                Pen.arcTo( lastPoint, point, radius );
                lastPoint = point;
            });
            Pen.fillStroke;
        });

        uView.mouseDownAction_({ |view, localX, localY, modifiers, buttonNumber, clickCount|
            var h = win.bounds.height;
            var w = win.bounds.width;

            // not at corners but within border: move
            var left   = localX <= inset;
            var top    = localY <= inset;
            var right  = localX >= (w - inset);
            var bottom = localY >= (h - inset);

            var xBool  = left or: right;
            var yBool  = top or: bottom;

            reSizeBool = xBool and: yBool;   // this does not work for different radius and inset vals
            moveBool   = (xBool or: yBool) xor: reSizeBool;
            cornerMask = [left, top, right, bottom];

            lastMouseX = localX;
            lastMouseY = localY;
        })
        .mouseUpAction_({ |view, localX, localY, modifiers, buttonNumber, clickCount|
            reSizeBool = false; // necessary?
            moveBool = false;  // necessary?

            lastMouseX = localX;
            lastMouseY = localY;
        })
        .mouseMoveAction_({ |view, localX, localY| 
            var deltaX = lastMouseX - localX;
            var deltaY = lastMouseY - localY;

            case
            { moveBool } {
                var left = win.bounds.left - deltaX;
                var top = win.bounds.top + deltaY;

                win.bounds_( Rect(left, top, win.bounds.width, win.bounds.height) );
            }
            { reSizeBool }{
                var whichCorner = cornerMask.asInteger.convertDigits(2);
                var left, top, width, height;

                switch(whichCorner,
                    3, { // right bottom
                        left = win.bounds.left;
                        top = win.bounds.top + deltaY;
                        width = win.bounds.width - deltaX;
                        height = win.bounds.height - deltaY;
                        
                        lastMouseX = localX;
                        lastMouseY = localY
                    },
                    6, { // right top
                        left = win.bounds.left;
                        top = win.bounds.top;
                        width = win.bounds.width - deltaX;
                        height = win.bounds.height + deltaY;

                        lastMouseX = localX
                    },
                    9, { // left bottom
                        left = win.bounds.left - deltaX;
                        top = win.bounds.top + deltaY;
                        width = win.bounds.right - left;
                        height = win.bounds.height - deltaY;

                        lastMouseY = localY
                    },
                    12,{ // left top
                        left = win.bounds.left - deltaX;
                        top = win.bounds.top;
                        width = win.bounds.right - left;
                        height = win.bounds.height + deltaY;
                    }
                );

                if( width <= minWidth  ,{ width = minWidth; left = win.bounds.left});
                if( height <= minHeight,{ height = minHeight; top = win.bounds.top});

                win.bounds_( Rect(left, top, width, height) )
            }

        });
    }

    close { win.close }
    
    view { ^win.view }
    viewBounds { ^win.view.bounds.insetBy(inset/2) }

    bounds { ^win.bounds }
    bounds_ {}

    layout { ^win.layout }
    layout_ { |layout|
        win.layout_( layout );
        win.layout.margins_(inset).spacing_(0);
        win.refresh
    }

    drawFunc_ { |func|
        var oldFunc = uView.drawFunc;

        uView.drawFunc_({
            func.();
            oldFunc.();
        });
        win.refresh;
    }

    background_ { |color|
        background = color;
        win.refresh
    }

    resizeTo { |w,h|
        win.setInnerExtent(w,h);
        win.refresh
    }



}
