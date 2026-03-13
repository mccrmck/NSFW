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

        // these fields are merged with default values
        // remember last entry in .json can't end with a comma...
        ^"{
            \"type\": \"knob\",
            \"id\": \"%\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"html\": \"\",
            \"css\": \"\",
            \"design\": \"default\",
            \"colorKnob\": \"auto\",
            \"pips\": false,
            \"dashed\": false,
            \"angle\": 320,
            \"mode\": \"%\",
            \"onTouch\": \"var val\\nif(event.type == 'start'){\\n  val = 1\\n} else if(event.type == 'stop'){\\n  val = 0\\n}\\nsend('/touch_%',val)\"
        }".format(id, w, h, e, s, id)
    }
}
