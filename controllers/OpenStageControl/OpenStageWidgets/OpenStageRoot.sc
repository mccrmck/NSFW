OpenStageRoot {
    var  widgets, tabs, columns;

    *new { ^super.new.init }

    init {
        widgets = [];
        tabs = [];
        columns = 1;
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

    oscString {
        var widgetString = OpenStageControl.prCollectOSCStrings(widgets);
        var tabString = OpenStageControl.prCollectOSCStrings(tabs);

        // these fields are merged with default values
        // remember last entry in .json can't end with a comma...

        ^"{
            \"createdWith\": \"Open Stage Control\",
            \"version\": \"1.30.2\",
            \"type\": \"session\",
            \"content\": {
                \"type\": \"root\",
                \"id\": \"root\",
                \"padding\": 2,
                \"colorBg\": \"#181122\",
                \"colorWidget\": \"#615c47\",
                \"css\": \".navigation { background: transparent; border: none; }\\n.tablink { background-color: transparent; border-radius: 100vw; border: 1px solid #615c47; font-size: 0; }\\n.tablink.on { background-color: #615c4750; } \", 
                \"layout\": \"grid\",
                \"justify\": \"start\",
                \"gridTemplate\": \"%\",
                \"tabsPosition\": \"top\",
                \"bypass\": true,
                \"onCreate\": \"send('/nsfwGuiLoaded')\",
                \"widgets\": [%],
                \"tabs\": [%]
            }
        }".format(columns, widgetString, tabString);
    }

    write { |path|
        var file = File(path, "w");
        file.write(this.oscString);
        file.close;
    }
}
