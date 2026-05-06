OpenStageRoot {
    var  <widgetArray, <tabArray, <columns;

    *new { |widgetArray, tabArray, columns|
        ^super.newCopyArgs(widgetArray.asArray, tabArray.asArray, columns).init
    }

    init {}

    oscString {
        var widgets = widgetArray.collect(_.oscString);
        var tabs    = tabArray.collect(_.oscString);
        widgets     = "%".ccatList("%" ! (widgets.size - 1)).format(*widgets);
        tabs        = "%".ccatList("%" ! (tabs.size - 1)).format(*tabs);

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
                \"css\": \".navigation { background: transparent; border: none; }\\n .tablink { background-color: transparent; border-radius: 100vw; border: 1px solid #615c47; font-size: 0; }\\n .tablink.on { background-color: #615c4750; } \", 
                \"layout\": \"grid\",
                \"justify\": \"start\",
                \"gridTemplate\": \"%\",
                \"tabsPosition\": \"top\",
                \"bypass\": true,
                \"onCreate\": \"send('/nsfwGuiLoaded')\",
                \"widgets\": [%],
                \"tabs\": [%]
            }
        }".format(columns, widgets, tabs)
    }

    write { |path|
        var file = File(path, "w");
        file.write(this.oscString);
        file.close;
    }
}
