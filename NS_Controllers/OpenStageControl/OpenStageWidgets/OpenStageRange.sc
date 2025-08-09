OpenStageRange {
    var <snap, <horizontal, <width, <height;
    var <id;

    *new { |snap = true, horizontal = true, width, height|
        ^super.newCopyArgs(snap, horizontal, width, height).init
    }

    init {
        id = "range_" ++ OpenStageFaderID.next;
    }

    oscString {
        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        var orientation = switch(horizontal,
            true,        { true },
            \horizontal, { true },
            \hori,       { true },
            \h,          { true },
            false,       { false },
            \vertical,   { false },
            \vert,       { false },
            \v,          { false },
            { "horizontal value is not valid".error }
        );

        ^"{
            \"type\": \"range\",
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
            \"design\": \"compact\",
            \"knobSize\": \"auto\",
            \"colorKnob\": \"auto\",
            \"horizontal\": %,
            \"pips\": false,
            \"dashed\": false,
            \"gradient\": [],
            \"snap\": %,
            \"spring\": false,
            \"doubleTap\": false,
            \"range\": {
                \"min\": 0,
                \"max\": 1
            },
            \"logScale\": false,
            \"sensitivity\": 1,
            \"steps\": \"\",
            \"origin\": \"auto\",
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
            \"onTouch\": \"var val\\nif(event.type == 'start'){\\n  val = 1\\n} else if(event.type == 'stop'){\\n  val = 0\\n}\\nsend('/touch_%',val)\"
        }".format(id, w, h, e, orientation, snap, id)
    }
}
