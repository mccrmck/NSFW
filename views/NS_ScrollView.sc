/* 
* it *should* be possible to get the scrollHandle to float above the view
* using StackLayout and .mouseOverAction to control which widgets respond to mouse
* inputs. Unfortunately, the top-level view needs to have .acceptsMouseOver set to true...
* I have no idea how this would work for context menus, for example...
* If I eventually move everything to NS_Windows, this might be possible/easier to maintain
*/

NS_ScrollView : SCViewHolder {
    var <pos = 0;

    var scrollView, frameView, scrollHandle;
    var <isScrolling = false;

    // this is just for vertical scrolling right now, must pass a flag
    // for horizontal scrolling, and then mess around with innards
    // maybe the *new method checks the flags and then bifurcates, pretty much
    // all the drawing logic is different, might be cleaner than case statements

    *new { |viewHeight(300), scrollViewHeight(500)|
        ^super.new.init(viewHeight, scrollViewHeight)
    }

    init { |containerHeight, innerHeight|

        scrollView = View()
        .background_(NS_Style('transparent'))
        .minHeight_(innerHeight)
        .mouseWheelAction_({ |...args|
            this.prScrollAction(*args)
        });

        frameView = View().mouseWheelAction_({ |...args|
            this.prScrollAction(*args)
        });

        scrollHandle = NS_ScrollHandle()
        .action_({ |y| 
            pos = y; 
            this.moveView
        });

        view = UserView()
        .maxHeight_(containerHeight)
        .background_(NS_Style('transparent'))
        .onResize_({ |v|
            // this is a bit jumpy, maybe set a threshold for updating?
            //var val = (1 - scroll.value).linlin(0,1,0,-600);
            //inner.moveTo(inner.bounds.left, val)
        })
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style('radius');
            var b = NS_Style('border');

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.stroke;
        })
        .layout_( 
            HLayout(
                frameView.layout_(
                    VLayout( scrollView ).nsMarginsSpacing('view')
                ),
                scrollHandle
            ).nsMarginsSpacing('view')
        );
    }

    layout_ { |...views|
        var innerViews = views.collect({ |v| 
            v.mouseWheelAction_({ |...args|
                this.prScrollAction(*args)
            })
        });

        scrollView.layout_( 
            VLayout( *innerViews ).nsMarginsSpacing('view')
        )
    }

    prScrollAction { |v, x, y, mod, xDelta, yDelta|
        pos = (pos - (yDelta / scrollView.bounds.height)).clip(0,1);
        this.moveView;
    }

    moveView {
        var val = pos.linlin(
            0, 1, 0, frameView.bounds.height - scrollView.bounds.height 
        );
        scrollView.moveTo(scrollView.bounds.left, val);
        scrollHandle.pos_(pos)
    }
}

NS_ScrollHandle : SCViewHolder {
    var <pos = 0;
    var <>action;
    var draggable = false;

    *new { ^super.new.init }

    init {

        view = UserView()
        .minWidth_(15)
        .maxWidth_(21)
        .background_(NS_Style('transparent'))
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w.min(h) / 2;
            var b = NS_Style('border');

            // draw scroll lane
            Pen.fillColor_( NS_Style('highlight') );
            Pen.addRoundedRect(Rect(0, 0, w, h), r, r);
            Pen.fill;

            // draw scroll handle
            Pen.addOval(Rect(0, pos * (h - w), w, w).insetBy(b / 2));
            Pen.fill
        })
        .mouseDownAction_({ |v, x, y, mod, buttNum, count|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w / 2;

            draggable = true;

            y = y.linlin(r, h - r, 0, h);
            action.value(y / h)
        })
        .mouseUpAction_({ draggable = false })
        .mouseMoveAction_({ |v, x, y, mod|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = w / 2;

            if(draggable) {
                y = y.linlin(r, h - r, 0, h);
                action.value(y / h)
            }
        });
    }

    pos_ { |inPos|
        pos = inPos.clip(0, 1);
        view.refresh
    }
}
