OpenStageXY : OpenStageWidget {
    var snap;

    *new { ^super.new.init }

    init {
        id = "xy_" ++ OpenStageXYID.next;
        snap = true;
        expand = true;
        width = "auto";
        height = "auto"
    }
    
    oscString {
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
        }".format(id, width, height, expand, bRadius, bRadius, bRadius, snap, id)
    }
}
