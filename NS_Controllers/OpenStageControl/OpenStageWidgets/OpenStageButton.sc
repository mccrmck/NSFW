OpenStageButton {
    var <mode, <width, <height, <label;
    var <id;

    *new { |mode = 'toggle', width, height, label|
        ^super.newCopyArgs(mode, width, height, label).init
    }

    init {
        id = "button_" ++ OpenStageButtonID.next;
    }

    oscString {
        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        var m = switch(mode,
            'toggle', {"toggle"},
            'tap',    {"tap"},
            'push',   {"push"},
        );
        var l = if(label.isNil,{ "false" },{ "\"%\"".format(label.asString) });

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
            \"label\": %,
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
        }".format(id, w, h, e, l, m)
    }
}

OpenStageSwitch {
    var  <numPads, <columns, <mode, <width, <height;
    var <id;

    *new { |numPads = 3, columns = 1, mode = 'slide', width, height|
        ^super.newCopyArgs(numPads, columns, mode, width, height).init
    }

    init {
        id = "switch_" ++ OpenStageButtonID.next;
    }

    oscString {
        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        var m = switch(mode, 'tap', {"tap"}, 'slide', {"slide"});
        // I think these lines let me get pads w/o labels
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
            \"layout\": \"grid\",
            \"gridTemplate\": \"%\",
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
        }".format(id, w, h, e, columns.asInteger, labels, values , m)
    }
}
