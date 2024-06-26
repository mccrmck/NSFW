OSC_ButtonID {
    classvar <id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1; }
}

OSC_Button {
    var <width, <height, <mode;
    var <id;

    *new { |width, height, mode = 'toggle'|
        ^super.newCopyArgs(width, height, mode).init
    }

    init {
        id = "button_" ++ OSC_ButtonID.next;
    }

    oscString {
        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        var m = switch(mode,
         'toggle', {"toggle"},
         't',      {"toggle"},
         'push',   {"push"},
         'p',      {"push"},
        );
              
        ^"{
            \"type\": \"button\",
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
            \"padding\": \"auto\",
            \"html\": \"\",
            \"css\": \"\",
            \"colorTextOn\": \"auto\",
            \"label\": false,
            \"vertical\": false,
            \"wrap\": false,
            \"on\": 1,
            \"off\": 0,
            \"mode\": \"%\",
            \"doubleTap\": false,
            \"decoupled\": false,
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
            \"onValue\": \"\"
        }".format(id, w, h, e, m)
    }
}

OSC_Switch {
    var <width, <height, <horizontal, <mode, <numPads;
    var <id;

    *new { |width, height, horizontal = true, mode = 'tap', numPads = 4|
        ^super.newCopyArgs(width, height, horizontal, mode, numPads).init
    }

    init {
        id = "switch_" ++ OSC_ButtonID.next;
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
        var m = switch(mode,
         'tap',   {"tap"},
         't',     {"tap"},
         'slide', {"slide"},
         's',     {"slide"},
        );
        var labels = numPads.collect({ "\"\"" });
        var values = (0..(numPads-1));
              
        ^"{
            \"type\": \"switch\",
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
            \"padding\": \"auto\",
            \"html\": \"\",
            \"css\": \"\",
            \"colorTextOn\": \"auto\",
            \"layout\": \"%\",
            \"gridTemplate\": \"\",
            \"wrap\": false,
            \"values\": {
                \"labels\": %,
                \"values\": %
            },
            \"mode\": \"%\",
            \"value\": \"0\",
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
            \"onValue\": \"\"
        }".format(id, w, h, e, orientation, labels, values , m)
    }
}
