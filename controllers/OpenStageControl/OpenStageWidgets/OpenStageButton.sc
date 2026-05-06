OpenStageButton : OpenStageWidget {
    var mode, label;

    *new { ^super.new.init }

    init {
        id = "button_" ++ OpenStageButtonID.next;
        mode = "toggle";
        label = "false";
        expand = true;
        width = "auto";
        height = "auto";
    }

    mode_ { |modeString|
        var validKeys = ["push", "tap", "toggle"];
        if(validKeys.reduce('++').contains(modeString.asString)) {
            mode = modeString.asString
        } {
            "mode key not valid".error
        }
    }

    label_ { |inString| label = "\"%\"".format(inString.asString) }

    oscString {
        ^"{
            \"type\": \"button\",
            \"id\": \"%\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"borderRadius\": \"%\",
            \"label\": %,
            \"mode\": \"%\"
        }".format(id, width, height, expand, bRadius, label, mode)
    }
}

OpenStageSwitch : OpenStageWidget {
    var numPads, columns, mode;

    *new { ^super.new.init }

    init {
        id = "switch_" ++ OpenStageButtonID.next;
        numPads  = 3;
        columns = 1;
        mode = "slide";
        expand = true;
        width = "auto";
        height = "auto";
    }

    numPads_ { |n| numPads = n }

    columns_ { |numCols| columns = numCols }

    mode_ { |modeString|
        var validKeys = ["slide", "tap"];
        if(validKeys.reduce('++').contains(modeString.asString)) {
            mode = modeString.asString
        } {
            "mode key not valid".error
        }
    }

    oscString {
        // these lines let me get zero-indexed pads w/o labels
        var labels = numPads.collect { "\"\"" };  
        var values = (0..(numPads-1));

        ^"{
            \"type\": \"switch\",
            \"id\": \"%\",
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
            id, width, height, expand, bRadius,
            columns, labels, values, mode
        ) 
    }
}
