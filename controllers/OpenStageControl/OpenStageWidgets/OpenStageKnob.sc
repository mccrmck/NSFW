OpenStageKnob : OpenStageWidget {
    var snap;

    *new { ^super.new.init }

    init {
        id = "knob_" ++ OpenStageID.next('fader');
        snap = "vertical";
        expand = true;
        width = "auto";
        height = "auto";
    }

    snap { snap = "snap" }

    oscString {
        ^"{
            \"type\": \"knob\",
            \"id\": \"%\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"design\": \"default\",
            \"pips\": false,
            \"dashed\": false,
            \"angle\": 320,
            \"mode\": \"%\",
            \"onTouch\": \"var val\\nif(event.type == 'start'){\\n  val = 1\\n} else if(event.type == 'stop'){\\n  val = 0\\n}\\nsend('/touch_%',val)\"
        }".format(id, width, height, expand, snap, id)
    }
}
