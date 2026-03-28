+ Layout {
    nsMarginsSpacing { |...keys|
        var margins, spacing;

        keys.do { |key, index|

            switch(index)
            { 0 } {
                if(key.isNumber) 
                { margins = key; spacing = key } 
                {
                    margins = NS_Style((key ++ "Margins").asSymbol);
                    spacing = NS_Style((key ++ "Spacing").asSymbol);
                }
            }
            { 1 } {
                if(key.isNumber)
                { spacing = key } 
                { spacing = NS_Style((key ++ "Spacing").asSymbol) }
            }
            { "% does not accept more than 2 arguments".format(thisMethod).warn };
        };

        this.margins_(margins).spacing_(spacing)
    }
}

