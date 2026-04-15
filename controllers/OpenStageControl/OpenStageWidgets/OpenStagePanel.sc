OpenStagePanel : OpenStageWidget {
    var <widgetArray, <tabArray, <columns, <width, <height, <randCol;
    var <id;

    *new { |widgetArray, tabArray, columns(1), width, height, randCol(false)|
        ^super.newCopyArgs(widgetArray.asArray, tabArray.asArray, columns, width, height, randCol).init
    }

    init {
        id = "panel_" ++ OpenStagePanelID.next;
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

        // these fields are merged with default values
        // remember last entry in .json can't end with a comma...
        // this widget doesn't inherit bRadius because I think it looks silly...
        ^"{
            \"type\": \"panel\",
            \"id\": \"%\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"colorWidget\": \"%\",
            \"html\": \"%\",
            \"css\": \".html {\\n position: absolute;\\n top: 50\\%;\\n left: 0;\\n right: 0;\\n text-align: center;\\n z-index: -2;\\n opacity:0.75;\\n font-size:20rem;\\n}\",
            \"layout\": \"%\",
            \"lineWidth\": 0,
            \"padding\": 1,
            \"gridTemplate\": \"%\",
            \"tabsPosition\": \"hidden\",
            \"widgets\": [%],
            \"tabs\": [%]
        }".format(id, w, h, e, color, label, layout, columns, widgets, tabs)
    }
}
