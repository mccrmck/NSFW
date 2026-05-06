OpenStageRange : OpenStageWidget {
    var snap, horizontal;

    *new { ^super.new.init }

    init {
        id = "range_" ++ OpenStageFaderID.next;
        snap = false;
        horizontal = true;
        expand = true;
        width = "auto";
        height = "auto"
    }

    snap { snap = true }
    
    vertical { horizontal = false }

    oscString {
        // possible bug: seems like range doesn't respond to borderRadius
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
        }".format(id, width, height, expand, bRadius, horizontal, snap, id)
    }
}
