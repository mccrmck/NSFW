OSC_ModuleFragment {
    var <horizontal, <widgetArray;

    *new { |horizontal = true, widgetArray|
        ^super.newCopyArgs(horizontal, widgetArray.asArray).init;
    }

    init {}

    oscString { |label|

        ^"{
            \"version\": \"1.26.2\",
            \"createdWith\": \"Open Stage Control\",
            \"type\": \"fragment\",
            \"content\": %
        }".format( OSC_Panel(nil, nil, horizontal, widgetArray).oscStringLabel(label) )
    }

    write { |fileName|
        var name = PathName(OSC_Fragment.filenameSymbol.asString).pathOnly +/+ "ModuleFragments/%.json".format(fileName);
        var file = File(name,"w");
        file.write( this.oscString(fileName) );
        file.close;
    }
}

OSC_PanelID {
    classvar <id=0;
    *initClass { id = 0; }
    *next { ^id = id + 1; }
}

OSC_Fragment {
    var <width, <height;
    var <id, <responder;

    *new { |width, height|
        ^super.newCopyArgs(width, height).init
    }

    init {
        id = "fragment_" ++ OSC_PanelID.next;
    }

    oscString {

        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        responder = "/%osc".format(id);

        ^"{
            \"type\": \"fragment\",
            \"top\": 18,
            \"left\": 41,
            \"lock\": false,
            \"id\": \"%\",
            \"visible\": true,
            \"interaction\": true,
            \"comments\": \"\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"css\": \"\",
            \"file\": \"OSC{%}\",
            \"fallback\": \"\",
            \"props\": {},
            \"address\": \"auto\",
            \"variables\": \"@{parent.variables}\"
        }".format(id, w, h, e, responder)
    }

    loadFragment { |fileName|
        var path = PathName(OSC_Fragment.filenameSymbol.asString).pathOnly +/+ "ModuleFragments/%.json".format(fileName);
        OpenStageControl.netAddr.sendMsg(responder,path)
    }
    
}

OSC_Panel {
    var <width, <height, <horizontal, <widgetArray;
    var <id;

    *new { |width, height, horizontal = true, widgetArray|
        ^super.newCopyArgs(width, height, horizontal, widgetArray.asArray).init
    }

    init {
        id = "panel_" ++ OSC_PanelID.next;
    }

    oscString {
        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        var orientation = switch(horizontal,
            true,        { "horizontal" },
            \horizontal, { "horizontal" },
            \hori,       { "horizontal" },
            \h,          { "horizontal" },
            false,       { "vertical" },
            \vertical,   { "vertical" },
            \vert,       { "vertical" },
            \v,          { "vertical" },
            { "horizontal value is not valid".error }
        );
        var widgets = widgetArray.collect({ |widget|
            widget.oscString;
        });
        widgets = "%".ccatList("%"!(widgets.size-1)).format(*widgets);

        ^"{
            \"type\": \"panel\",
            \"top\": 0,
            \"left\": 0,
            \"lock\": false,
            \"id\": \"%\",
            \"visible\": true,
            \"interaction\": true,
            \"comments\": \"\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"colorText\": \"auto\",
            \"colorWidget\": \"auto\",
            \"colorStroke\": \"auto\",
            \"colorFill\": \"auto\",
            \"alphaStroke\": \"auto\",
            \"alphaFillOff\": \"auto\",
            \"alphaFillOn\": \"auto\",
            \"lineWidth\": \"auto\",
            \"borderRadius\": \"auto\",
            \"padding\": 0,
            \"html\": \"\",
            \"css\": \"\",
            \"colorBg\": \"auto\",
            \"layout\": \"%\",
            \"justify\": \"start\",
            \"gridTemplate\": \"\",
            \"contain\": true,
            \"scroll\": true,
            \"innerPadding\": false,
            \"tabsPosition\": \"top\",
            \"variables\": \"@{parent.variables}\",
            \"traversing\": false,
            \"value\": \"\",
            \"default\": \"\",
            \"linkId\": \"\",
            \"address\": \"auto\",
            \"preArgs\": \"\",
            \"typeTags\": \"\",
            \"decimals\": 2,
            \"target\": \"\",
            \"ignoreDefaults\": false,
            \"bypass\": false,
            \"onCreate\": \"\",
            \"onValue\": \"\",
            \"widgets\": [%],
            \"tabs\": []
        }".format(id, w, h, e, orientation, widgets)
    }

    oscStringLabel { |label|
        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        var orientation = switch(horizontal,
            true,        { "horizontal" },
            \horizontal, { "horizontal" },
            \hori,       { "horizontal" },
            \h,          { "horizontal" },
            false,       { "vertical" },
            \vertical,   { "vertical" },
            \vert,       { "vertical" },
            \v,          { "vertical" },
            { "horizontal value is not valid".error }
        );
        var widgets = widgetArray.collect({ |widget|
            widget.oscString;
        });
        widgets = "%".ccatList("%"!(widgets.size-1)).format(*widgets);

        ^"{
            \"type\": \"panel\",
            \"top\": 0,
            \"left\": 0,
            \"lock\": false,
            \"id\": \"%\",
            \"visible\": true,
            \"interaction\": true,
            \"comments\": \"\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"colorText\": \"auto\",
            \"colorWidget\": \"auto\",
            \"colorStroke\": \"auto\",
            \"colorFill\": \"auto\",
            \"alphaStroke\": \"auto\",
            \"alphaFillOff\": \"auto\",
            \"alphaFillOn\": \"auto\",
            \"lineWidth\": \"auto\",
            \"borderRadius\": \"auto\",
            \"padding\": 0,
            \"html\": \"%\",
            \"css\": \".html {\\n position: absolute;\\n top: 50\\%;\\n left: 0;\\n right: 0;\\n text-align: center;\\n z-index: -2;\\n opacity:0.75;\\n font-size:20rem;\\n}\",
            \"colorBg\": \"auto\",
            \"layout\": \"%\",
            \"justify\": \"start\",
            \"gridTemplate\": \"\",
            \"contain\": true,
            \"scroll\": true,
            \"innerPadding\": false,
            \"tabsPosition\": \"top\",
            \"variables\": \"@{parent.variables}\",
            \"traversing\": false,
            \"value\": \"\",
            \"default\": \"\",
            \"linkId\": \"\",
            \"address\": \"auto\",
            \"preArgs\": \"\",
            \"typeTags\": \"\",
            \"decimals\": 2,
            \"target\": \"\",
            \"ignoreDefaults\": false,
            \"bypass\": false,
            \"onCreate\": \"\",
            \"onValue\": \"\",
            \"widgets\": [%],
            \"tabs\": []
        }".format(id, w, h, e, label, orientation, widgets)
    }
}
