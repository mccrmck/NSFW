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

    *new {
        ^super.new.init
    }

    init {
        ^UserView()
        .fixedHeight_(2)
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
