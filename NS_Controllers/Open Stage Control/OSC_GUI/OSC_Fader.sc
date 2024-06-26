OSC_FaderID {
    classvar <id=0;
    *initClass { id = 0; }
    *next { ^id = id + 1; }
}

OSC_Fader {
    var <width, <height, <horizontal, <snap;
    var <id;

    *new { |width, height, horizontal = false, snap = false|
        ^super.newCopyArgs(width, height, horizontal, snap).init
    }

    init {
        id = "fader_" ++ OSC_FaderID.next;
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
            \"type\": \"fader\",
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


OSC_MultiFader {
    var <width, <height, <horizontal, <snap, <numFaders;
    var <id;

    *new { |width, height, horizontal = false, snap = false, numFaders = 4|
        ^super.newCopyArgs(width, height, horizontal, snap, numFaders).init
    }

    init {
        id = "multiFader_" ++ OSC_FaderID.next;
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
        var onTouchString, onDrawString;

        case
        { horizontal == false && (snap == false) }{
            onTouchString = "\"if (event.type == \\\"start\\\") {\\n locals.x = event.offsetX / width\\n locals.y = 1 - value[parseInt(locals.x * value.length)]\\n} else {\\n locals.x += event.movementX / width\\n locals.y = (1 - value[parseInt(locals.x * value.length)] + event.movementY / height)\\n}\\n\\nvar n = parseInt(locals.x * value.length)\\nn = Math.max(0, Math.min(n, value.length-1))\\n\\nvalue[n] = 1 - locals.y\\n\\nset(\\\"this\\\", value)\\nvar val\\nif(event.type == 'start'){\\nsend('/touch_%',1)\\n} else if(event.type == 'stop'){\\nsend('/touch_%',0)\\n}\"".format(id,id);
            onDrawString = "var sliderWidth = width / value.length - 1\\n for (var i in value){\\n ctx.beginPath()\\n ctx.rect(i * width / value.length, height, sliderWidth, - value[i] * height)\\nctx.fill()\\n}"
        }
        { horizontal == true  && (snap == false) }{
            onTouchString = "\"if (event.type == \\\"start\\\") {\\n locals.y = event.offsetY / height\\n locals.x = value[parseInt(locals.y * value.length)]\\n} else {\\n locals.y += event.movementY / height\\n locals.x = (value[parseInt(locals.y * value.length)] + event.movementX / width)\\n}\\n\\nvar n = parseInt(locals.y * value.length)\\nn = Math.max(0, Math.min(n, value.length-1))\\n\\nvalue[n] = locals.x\\n\\nset(\\\"this\\\", value)\\nvar val\\nif(event.type == 'start'){\\nsend('/touch_%',1)\\n} else if(event.type == 'stop'){\\nsend('/touch_%',0)\\n}\"".format(id,id);
            onDrawString = "var sliderHeight = height / value.length - 1\\n for (var i in value){\\n ctx.beginPath()\\n ctx.rect(0,i * height / value.length, value[i] * width, sliderHeight)\\nctx.fill()\\n}"
        }
        { horizontal == false && (snap == true) }{
            onTouchString = "\"if (event.type == \\\"start\\\") {\\n locals.x = event.offsetX / width\\n locals.y = event.offsetY / height\\n} else {\\n locals.x += event.movementX / width\\n locals.y += event.movementY / height\\n}\\n\\nvar n = parseInt(locals.x * value.length)\\nn = Math.max(0, Math.min(n, value.length-1))\\n\\nvalue[n] = 1 - locals.y\\n\\nset(\\\"this\\\", value)\\nvar val\\nif(event.type == 'start'){\\nsend('/touch_%',1)\\n} else if(event.type == 'stop'){\\nsend('/touch_%',0)\\n}\"".format(id,id);
            onDrawString = "var sliderWidth = width / value.length - 1\\n for (var i in value){\\n ctx.beginPath()\\n ctx.rect(i * width / value.length, height, sliderWidth, - value[i] * height)\\nctx.fill()\\n}"
        }
        { horizontal == true  && (snap == true) }{ 
            onTouchString = "\"if (event.type == \\\"start\\\") {\\n locals.x = event.offsetX / width\\n locals.y = event.offsetY / height\\n} else {\\n locals.x += event.movementX / width\\n locals.y += event.movementY / height\\n}\\n\\nvar n = parseInt(locals.y * value.length)\\nn = Math.max(0, Math.min(n, value.length-1))\\n\\nvalue[n] = locals.x\\n\\nset(\\\"this\\\", value)\\nvar val\\nif(event.type == 'start'){\\nsend('/touch_%',1)\\n} else if(event.type == 'stop'){\\nsend('/touch_%',0)\\n}\"".format(id,id);
            onDrawString = "var sliderHeight = height / value.length - 1\\n for (var i in value){\\n ctx.beginPath()\\n ctx.rect(0,i * height / value.length, value[i] * width, sliderHeight)\\nctx.fill()\\n}"
        };

        ^"{
            \"type\": \"canvas\",
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
            \"valueLength\": %,
            \"autoClear\": true,
            \"continuous\": false,
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
            \"onValue\": \"for (var i in value) {\\n    value[i] = Math.max(0, Math.min(1, value[i]))\\n}\\n\\nset(\\\"this\\\", value, {sync: false, send: false})\",
            \"onTouch\": %,
            \"onDraw\": \"ctx.fillStyle = cssVars.colorFill\\nctx.globalAlpha = cssVars.alphaFillOn\\n\\n%\",
            \"onResize\": \"\"
        }".format(id, w, h, e, numFaders, onTouchString, onDrawString)
    }
}
