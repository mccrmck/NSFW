/*
* NS_Widget adds functions for creating custom click actions
* functions can be called on left, right, and double click,
* each of which can be combined with a single key modifier
*/

NS_Widget : SCViewHolder {
    var <mouseActionDict;

    *new { ^super.new.init }
 
    init {
        mouseActionDict = ()
    }

    drawWidget { this.subclassResponsibility(thisMethod) }

    prAddClickAction { |key, func, mod|
        var modifier = mod ?? 'none';
        var modDict = mouseActionDict.atFail(modifier.asSymbol,{
            mouseActionDict.put(modifier.asSymbol, ())
        });
        mouseActionDict[modifier.asSymbol].put(key, func)
    }

    addLeftClickAction { |func, mod|
        this.prAddClickAction('leftClick', func, mod)
    }

    addRightClickAction { |func, mod|
        this.prAddClickAction('rightClick', func, mod)
    }

    addDoubleClickAction { |func, mod|
        this.prAddClickAction('doubleClick', func, mod)
    }

    onMouseDown { |v, x, y, modifiers, buttonNumber, clickCount|
        var click = if(clickCount == 1,{
            ['leftClick', 'rightClick'].at(buttonNumber)
        },{
            ['doubleClick', 'doubleRightClick'].at(buttonNumber)
        });
        var alt   = modifiers.isAlt;   // boolean
        var cmd   = modifiers.isCmd;   // boolean 
        var ctrl  = modifiers.isCtrl;  // boolean
        var shift = modifiers.isShift; // boolean
        var modArray = [alt, cmd, ctrl, shift].asInteger;

        var mod   = if(modArray.sum > 1, {
            ^"multiple modifiers not supported yet".warn;
        },{
           var index = modArray.indexOf(1) ?? 4;
           ['alt', 'cmd', 'ctrl', 'shift', 'none'].at(index)
        });

        // consider adding `classvar verbose` to toggle the warnings
        var func  = mouseActionDict.atFail(mod, { 
            ^"mouse action: %-% not assigned".format(mod, click).warn 
        });
        func      = func.atFail(click, { 
            ^"mouse action: %-% not assigned".format(mod, click).warn 
        });
        func.value(this, v, x, y);
        v.refresh
    }
}

/*
* NS_ControlWidget exposes functions for auto-mapping NS_Controls to hardware
* and software controllers
*
* this will be expanded upon...
*/

NS_ControlWidget : NS_Widget {
    var isHighlighted = false;

    toggleAutoAssign { |nsControl|
        if(nsControl.mapped == 'unmapped') 
        { nsControl.mapped = 'listening'; nsControl.enableAutoAssign } 
        { nsControl.mapped = 'unmapped';  nsControl.disableAutoAssign };

        this.refresh;
    }

    openControlMenu { |nsControl|
        Menu(
            MenuAction("autoAssign",{ 
                this.toggleAutoAssign(nsControl)
            }).checked_(nsControl.mapped != 'unmapped'),
            Menu(
                MenuAction("OSC"),
                MenuAction("MIDI"),
            ).title_("manual Assign")
        ).front
    }
}
