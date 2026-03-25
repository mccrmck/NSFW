+ Layout {
    nsMarginsSpacing { |...keys|
        var spacing, margins;

        keys.do { |key, index|

            switch(index)
            { 0 } {
                if(key.isNumber) 
                { spacing = key; margins = key; } 
                {
                    spacing = NS_Style((key ++ "Spacing").asSymbol);
                    margins = NS_Style((key ++ "Margins").asSymbol);
                }
            }
            { 1 } {
                if(key.isNumber)
                { margins = key } 
                { margins = NS_Style((key ++ "Margins").asSymbol) }
            }
            { "% does not accept more than 2 arguments".format(thisMethod).warn };
        };

        this.spacing_(spacing).margins_(margins)
    }
}

