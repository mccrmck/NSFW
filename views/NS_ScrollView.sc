/* 
* it *should* be possible to get the scrollHandle to float above the view
* using StackLayout and .mouseOverAction to control which widgets respond to mouse
* inputs. Unfortunately, the top-level view needs to have .acceptsMouseOver set to true...
* I have no idea how this would work for context menus, for example...
* If I eventually move everything to NS_Windows, this might be possible/easier to maintain
*/

NS_ScrollView : SCViewHolder {
    var <pos = 0;

    var inset;
    var scrollView, frameView, scrollHandle;
    var <isScrolling = false;

    // this is just for vertical scrolling right now, must pass a flag
    // for horizontal scrolling, and then mess around with innards
    // maybe the *new method checks the flags and then bifurcates, pretty much
    // all the drawing logic is different, might be cleaner than case statements

    *new { |viewHeight = 300, scrollViewHeight = 500|
        ^super.new.init(viewHeight, scrollViewHeight)
    }

    init { |containerHeight, innerHeight|
        inset = NS_Style('inset');

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
            var rect = v.bounds.insetBy(inset);
            var w = rect.width;
            var h = rect.height;
            var r = NS_Style('radius');

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.width_(inset);
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.stroke;
        })
        .layout_( 
            HLayout(
                frameView.layout_(
                    VLayout( scrollView )
                    .margins_(NS_Style('viewMargins'))
                    .spacing_(NS_Style('viewSpacing'))
                ),
                scrollHandle
            ).margins_(NS_Style('viewMargins')).spacing_(NS_Style('viewSpacing'))
        );
    }

    layout_ { |...views|
        var innerViews = views.collect({ |v| 
            v.mouseWheelAction_({ |...args|
                this.prScrollAction(*args)
            })
        });

        scrollView.layout_( 
            VLayout( *innerViews )
            .margins_(NS_Style('viewMargins'))
            .spacing_(NS_Style('viewSpacing'))
        )
    }

    prScrollAction { |v, x, y, mod, xDelta, yDelta|
        pos = (pos - (yDelta / scrollView.bounds.height)).clip(0,1);
        this.moveView;
    }

    moveView {
        var val = pos.linlin(0,1,0,(scrollView.bounds.height - frameView.bounds.height).neg);
        scrollView.moveTo(scrollView.bounds.left, val);
        scrollHandle.pos_(pos)
    }
}

NS_ScrollHandle : SCViewHolder {
    var <pos = 0;
    var <>action;
    var draggable = false;

    *new {
        ^super.new.init()
    }

    init {
        var inset = NS_Style('inset');

        view = UserView()
        .minWidth_(15)
        .maxWidth_(21)
        .background_(NS_Style('transparent'))
        .drawFunc_({ |v|
            var rect = v.bounds.insetBy(inset);
            var w = rect.bounds.width;
            var h = rect.bounds.height;
            var r = w.min(h) / 2;

            // draw scroll lane
            Pen.fillColor_( NS_Style('highlight') );
            Pen.addRoundedRect(Rect(inset, inset, w, h), r, r);
            Pen.fill;

            // draw scroll handle
            Pen.addOval(Rect(inset, inset + (pos * (h - w)), w, w));
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
