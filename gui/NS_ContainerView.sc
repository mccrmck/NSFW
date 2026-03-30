NS_ContainerView {

    *new {
        ^super.new.init
    }

    init {
        ^UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var rect = Rect(0,0,w,h);
            var rad = NS_Style('radius');

            Pen.fillColor_( NS_Style('highlight') );
            Pen.addRoundedRect(rect, rad, rad);
            Pen.fill;
        })
    }
}


NS_HDivider {
    *new { ^NS_Divider().fixedHeight_(2) }
}

NS_VDivider {
    *new { ^NS_Divider().fixedWidth_(2) }
}

NS_Divider {

    *new {
        ^super.new.init
    }

    init {
        ^UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var rect = Rect(0,0,w,h);
            var rad = NS_Style('radius');

            Pen.fillColor_( NS_Style('bGroundDark') );
            Pen.addRoundedRect(rect, rad, rad);
            Pen.fill;
        })
    }
}


NS_Header : SCViewHolder {

    *new { |string|
        ^super.new.init(string.asString)
    }

    init { |inString|

        view = UserView()
        .minHeight_(inString.bounds(Font(*NS_Style('bigFont'))).height)
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;

            Pen.stringCenteredIn(
                inString,
                Rect(0, 0, w, h),
                Font(*NS_Style('bigFont')),
                NS_Style('textDark')
            )
        })
    }
}
