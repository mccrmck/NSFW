OSC_XYID {
    classvar <id=0;
    *initClass { id = 0; }
    *next { ^id = id + 1; }
}

OSC_XY {
    var <width, <height, <snap;
    var <id;

    *new { |width, height, snap = false|
        ^super.newCopyArgs(width, height, snap).init
    }

    init {
        id = "xy_" ++ OSC_XYID.next;
    }

    oscString {
        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
       
        ^"{
            \"type\": \"xy\",
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
            \"pointSize\": 20,
            \"ephemeral\": false,
            \"pips\": false,
            \"snap\": %,
            \"spring\": false,
            \"rangeX\": {
              \"min\": 0,
              \"max\": 1
            },
            \"rangeY\": {
              \"min\": 0,
              \"max\": 1
            },
            \"logScaleX\": false,
            \"logScaleY\": false,
            \"axisLock\": \"\",
            \"doubleTap\": false,
            \"sensitivity\": 1,
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
        }".format(id, w, h, e, snap, id)
    }
}
