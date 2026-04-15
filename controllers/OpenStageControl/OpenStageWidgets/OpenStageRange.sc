OpenStageRange : OpenStageWidget {
    var <snap, <horizontal, <width, <height;
    var <id;

    *new { |snap(true), horizontal(true), width, height|
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

        // these fields are merged with default values
        // remember last entry in .json can't end with a comma...
        // seems like range doesn't respond to borderRadius, maybe file a bug report?
        ^"{
            \"type\": \"range\",
            \"id\": \"%\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"borderRadius\": \"%\",
            \"design\": \"compact\",
            \"horizontal\": %,
            \"snap\": %,
            \"onTouch\": \"var val\\nif(event.type == 'start'){\\n  val = 1\\n} else if(event.type == 'stop'){\\n  val = 0\\n}\\nsend('/touch_%',val)\"
        }".format(id, w, h, e, bRadius, orientation, snap, id)
    }
}
