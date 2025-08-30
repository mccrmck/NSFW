OpenStageKnob {
    var <snap, <width, <height;
    var <id;

    *new { |snap = false, width, height|
        ^super.newCopyArgs(snap, width, height).init
    }

    init {
        id = "knob_" ++ OpenStageFaderID.next;
    }

    oscString {
        var e = if(width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        var s = if(snap,{ "snap" },{ "vertical" });
       
        ^"{
            \"type\": \"knob\",
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
            \"design\": \"default\",
            \"colorKnob\": \"auto\",
            \"pips\": false,
            \"dashed\": false,
            \"angle\": 360,
            \"mode\": \"%\",
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
        }".format(id, w, h, e, s, id)
    }
}
