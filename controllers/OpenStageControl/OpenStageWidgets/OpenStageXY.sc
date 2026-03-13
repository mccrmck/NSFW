OpenStageXY : OpenStageWidget {
    var <snap, <width, <height;
    var <id;

    *new { |snap = true, width, height|
        ^super.newCopyArgs(snap, width, height).init
    }

    init {
        id = "xy_" ++ OpenStageXYID.next;
    }

    oscString {
        var e = if(width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";

        // these fields are merged with default values
        // remember last entry in .json can't end with a comma...
        ^"{
            \"type\": \"xy\",
            \"id\": \"%\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"borderRadius\": \"%\",
            \"padding\": %,
            \"pointSize\": %,
            \"pips\": false,
            \"snap\": %,
            \"onTouch\": \"var val\\nif(event.type == 'start'){\\n  val = 1\\n} else if(event.type == 'stop'){\\n  val = 0\\n}\\nsend('/touch_%',val)\"
        }".format(id, w, h, e, bRadius, bRadius, bRadius, snap, id)
    }
}
