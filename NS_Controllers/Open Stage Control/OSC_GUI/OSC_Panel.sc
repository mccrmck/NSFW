OSC_Panel {
    var <widgetArray, <tabArray, <columns, <width, <height, <randCol;
    var <id;

    *new { |widgetArray, tabArray, columns = 1, width, height, randCol = false|
        ^super.newCopyArgs(widgetArray.asArray, tabArray.asArray, columns, width, height, randCol).init
    }

    init {
        id = "panel_" ++ OSC_PanelID.next;
    }

    oscString { |label|
        var e = if( width.isNil && (height.isNil),{ true },{ false });
        var w = width ? "auto";
        var h = height ? "auto";
        var color = if(randCol,{"rgba(%,%,%,1)".format(256.rand,256.rand,256.rand)},{ "auto" });
        var widgets = widgetArray.collect( _.oscString );
        var tabs = tabArray.collect( _.oscString );
        var layout = case
        { columns == 1 }{ layout = "vertical" }
        { columns == widgetArray.size }{ layout = "horizontal" }
        { columns == tabArray.size }{ layout = "horizontal" }
        { layout = "grid" };

        if( widgets.size > 0 and: (tabs.size  > 0),{
            "cannot add both widgets and tabs to the same panel".error
        });
        widgets = "%".ccatList("%"!(widgets.size-1)).format(*widgets);
        tabs    = "%".ccatList("%"!(tabs.size-1)).format(*tabs);
        label   = if(label.isNil,{ "" },{ label.asString });

        ^"{
            \"type\": \"panel\",
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
            \"colorWidget\": \"%\",
            \"colorStroke\": \"auto\",
            \"colorFill\": \"auto\",
            \"alphaStroke\": \"auto\",
            \"alphaFillOff\": \"auto\",
            \"alphaFillOn\": \"auto\",
            \"lineWidth\": \"auto\",
            \"borderRadius\": \"auto\",
            \"padding\": 0,
            \"html\": \"%\",
            \"css\": \"> inner > .navigation {\\n display:none;\\n}\\n\\n .html {\\n position: absolute;\\n top: 50\\%;\\n left: 0;\\n right: 0;\\n text-align: center;\\n z-index: -2;\\n opacity:0.75;\\n font-size:20rem;\\n}\",
            \"colorBg\": \"auto\",
            \"layout\": \"%\",
            \"justify\": \"start\",
            \"gridTemplate\": \"%\",
            \"contain\": true,
            \"scroll\": true,
            \"innerPadding\": false,
            \"tabsPosition\": \"top\",
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
            \"widgets\": [%],
            \"tabs\": [%]
        }".format(id, w, h, e, color, label, layout, columns, widgets, tabs)
    }
}
