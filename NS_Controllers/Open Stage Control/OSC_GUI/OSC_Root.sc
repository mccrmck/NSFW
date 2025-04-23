OSC_Root {
    var  <widgetArray, <tabArray, <columns;

    *new { | widgetArray, tabArray, columns|
        ^super.newCopyArgs(widgetArray.asArray, tabArray.asArray, columns).init
    }

    init {}

    oscString {
        var widgets = widgetArray.collect( _.oscString );
        var tabs    = tabArray.collect( _.oscString );
        widgets     = "%".ccatList("%"!(widgets.size-1)).format(*widgets);
        tabs        = "%".ccatList("%"!(tabs.size-1)).format(*tabs);

        ^"{
            \"createdWith\": \"Open Stage Control\",
            \"version\": \"1.29.0\",
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
                \"css\": \".html {\\n position: absolute;\\n top: 50\\%;\\n left: 0;\\n right: 0;\\n text-align: center;\\n z-index: -2;\\n opacity:0.75;\\n font-size:20rem;\\n}\",
                \"colorBg\": \"rgba(0,0,0,1)\",
                \"layout\": \"grid\",
                \"justify\": \"start\",
                \"gridTemplate\": \"%\",
                \"contain\": true,
                \"scroll\": true,
                \"innerPadding\": true,
                \"tabsPosition\": \"hidden\",
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
                \"onValue\": \"\",
                \"onTouch\": \"\",
                \"widgets\": [%],
                \"tabs\": [%]
            }
        }".format(columns, widgets, tabs)
    }

    write { |path|
        var file = File(path, "w");
        file.write( this.oscString );
        file.close;
    }
}
