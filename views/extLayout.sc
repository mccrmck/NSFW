+ Layout {

    nsMarginsSpacing { |key|

        if(key.isNumber) {
            this.spacing_(key).margins_(key)
        } {
            var spacingKey = (key ++ "Spacing").asSymbol;
            var marginsKey = (key ++ "Margins").asSymbol;
            this.spacing_(NS_Style(spacingKey)).margins_(NS_Style(marginsKey))
        }
    }
}

