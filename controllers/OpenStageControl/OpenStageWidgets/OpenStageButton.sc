OpenStageButton : OpenStageWidget {
    var <mode, <width, <height, <label;
    var <id;

    *new { |mode('toggle'), width, height, label|
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

        // these fields are merged with default values
        // remember last entry in .json can't end with a comma...
        ^"{
            \"type\": \"button\",
            \"id\": \"%\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"borderRadius\": \"%\",
            \"label\": %,
            \"mode\": \"%\"
        }".format(id, w, h, e, bRadius, l, m) // bRadius inherited from superclass
    }
}

OpenStageSwitch : OpenStageWidget {
    var  <numPads, <columns, <mode, <width, <height;
    var <id;

    *new { |numPads(3), columns(1), mode('slide'), width, height|
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
        // these lines let me get pads w/o labels
        var labels = numPads.collect({ "\"\"" });  
        var values = (0..(numPads-1));

        // these fields are merged with default values
        // remember last entry in .json can't end with a comma...
        ^"{
            \"type\": \"switch\",
            \"id\": \"%\",
            \"comments\": \"\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"borderRadius\": \"%\",
            \"layout\": \"grid\",
            \"gridTemplate\": \"%\",
            \"values\": {
                \"labels\": %,
                \"values\": %
            },
            \"value\": 0,
            \"mode\": \"%\"
        }".format(
            // bRadius inherited from superclass
            id, w, h, e, bRadius, columns.asInteger, labels, values , m
        ) 
    }
}
