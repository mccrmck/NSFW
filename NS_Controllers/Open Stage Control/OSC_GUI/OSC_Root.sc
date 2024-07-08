OSC_Root {
    var <horizontal, <widgetArray;

    *new { | horizontal = true, widgetArray|
        ^super.newCopyArgs(horizontal,widgetArray.asArray).init
    }

    init {}

    oscString {
        var orientation = switch(horizontal,
            true,        { "horizontal" },
            \horizontal, { "horizontal" },
            \hori,       { "horizontal" },
            \h,          { "horizontal" },
            false,       { "vertical" },
            \vertical,   { "vertical" },
            \vert,       { "vertical" },
            \v,          { "vertical" },
            { "horizontal value is not valid".error }
        );
        var widgets = widgetArray.collect({ |widget|
            widget.oscString;
        });
        widgets = "%".ccatList("%"!(widgets.size-1)).format(*widgets);

        ^"{
            \"createdWith\": \"Open Stage Control\",
            \"version\": \"1.26.2\",
            \"type\": \"session\",
            \"content\": {
                \"type\": \"root\",
                \"lock\": false,
                \"id\": \"root\",
                \"visible\": true,
                \"interaction\": true,
                \"comments\": \"\",
                \"width\": \"auto\",
                \"height\": \"auto\",
                \"colorText\": \"auto\",
                \"colorWidget\": \"auto\",
                \"alphaFillOn\": \"auto\",
                \"borderRadius\": \"auto\",
                \"padding\": 2,
                \"html\": \"\",
                \"css\": \"\",
                \"colorBg\": \"rgba(0,0,0,1)\",
                \"layout\": \"%\",
                \"justify\": \"start\",
                \"gridTemplate\": \"\",
                \"contain\": true,
                \"scroll\": true,
                \"innerPadding\": true,
                \"tabsPosition\": \"top\",
                \"hideMenu\": false,
                \"variables\": \"@{parent.variables}\",
                \"traversing\": false,
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
                \"onPreload\": \"\",
                \"widgets\": [%],
                \"tabs\": []
            }
        }".format(orientation, widgets)
    }

    write { |path|
        var file = File(path,"w");
        file.write(this.oscString);
        file.close;
    }
}
