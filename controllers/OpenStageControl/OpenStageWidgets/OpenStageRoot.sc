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
                \"layout\": \"grid\",
                \"justify\": \"start\",
                \"gridTemplate\": \"%\",
                \"tabsPosition\": \"hidden\",
                \"onCreate\": \"send('/nsfwGuiLoaded')\",
                \"widgets\": [%],
                \"tabs\": [%]
            }
        }".format(columns, widgets, tabs)
    }

    // this was in the above .json, but I think it's superfluous
    //\"css\": \".html {\\n position: absolute;\\n top: 50\\%;\\n left: 0;\\n right: 0;\\n text-align: center;\\n z-index: -2;\\n opacity:0.75;\\n font-size:20rem;\\n}\",

    write { |path|
        var file = File(path, "w");
        file.write(this.oscString);
        file.close;
    }
}
