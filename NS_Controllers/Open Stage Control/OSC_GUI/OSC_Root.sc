OSC_Root {
    var <horizontal, <widgetArray, <tabArray;

    *new { | horizontal = true, widgetArray, tabArray|
        ^super.newCopyArgs(horizontal, widgetArray.asArray, tabArray.asArray).init
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
        var tabs = tabArray.collect({ |widget|
            widget.oscString;
        });
        widgets = "%".ccatList("%"!(widgets.size-1)).format(*widgets);
        tabs    = "%".ccatList("%"!(tabs.size-1)).format(*tabs);

        ^"{
            \"createdWith\": \"Open Stage Control\",
            \"version\": \"1.27.0\",
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
                \"css\": \"> inner > .navigation {\\n display:none;\\n}\\n\\n .html {\\n position: absolute;\\n top: 50\\%;\\n left: 0;\\n right: 0;\\n text-align: center;\\n z-index: -2;\\n opacity:0.75;\\n font-size:20rem;\\n}\",
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
                \"bypass\": true,
                \"onCreate\": \"send('/nsfwGuiLoaded')\",
                \"onValue\": \"\",
                \"onPreload\": \"\",
                \"widgets\": [%],
                \"tabs\": [%]
            }
        }".format(orientation, widgets, tabs)
    }

    write { |path|
        var file = File(path,"w");
        file.write(this.oscString);
        file.close;
    }
}
