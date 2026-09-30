OpenStagePanel : OpenStageWidget {
    // needs getters (at least for tabs) for OpenStageControl.refresh
    var <widgets, <tabs, columns, tabPos, color, label;

    *new { ^super.new.init }

    init { 
        id = "panel_" ++ OpenStageID.next('panel');
        widgets = []; 
        tabs = [];
        columns = 1;
        expand = true;
        width = "auto";
        height = "auto";
        tabPos = "hidden";
        color = "auto";
        label = "";
    }

    widgetArray_ { |widgetArray|
        if(tabs.size > 0) { "widget cannot host both tabs and widgets".error };
        widgets = widgetArray
    }

    tabArray_ { |tabArray|
        if(widgets.size > 0) { "widget cannot host both widgets and tabs".error };
        tabs = tabArray
    }

    columns_ { |cols| columns = cols }

    tabPos_ { |key|
        var validKeys = ["top", "left", "right", "bottom", "hidden"];
        if(validKeys.reduce('++').contains(key.asString)) {
            tabPos = key.asString
        } {
            "tabPos key not valid".error
        }
    }

    randCol { color = "rgba(%,%,%,1)".format(*{ 256.rand } ! 3) }

    label_ { |inString| label = inString.asString }

    oscString {
        var widgetString = OpenStageControl.prCollectOSCStrings(widgets);
        var tabString = OpenStageControl.prCollectOSCStrings(tabs);
        var layout = columns.switch(
            1,            { "vertical" },
            widgets.size, { "horizontal" },
            tabs.size,    { "horizontal" },
            { "grid" }
        );

        ^"{
            \"type\": \"panel\",
            \"id\": \"%\",
            \"width\": \"%\",
            \"height\": \"%\",
            \"expand\": %,
            \"colorWidget\": \"%\",
            \"html\": \"%\",
            \"layout\": \"%\",
            \"lineWidth\": 0,
            \"padding\": 1,
            \"gridTemplate\": \"%\",
            \"tabsPosition\": \"%\",
            \"widgets\": [%],
            \"tabs\": [%]
        }".format(
            id, width, height, expand, color, label, 
            layout, columns, tabPos, widgetString, tabString
        )
    }
}
