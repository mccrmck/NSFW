NS_ContainerView : NS_Widget {

    *new {
        ^super.new.init
    }

    init {
        view = UserView().drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var rect = Rect(0,0,w,h);
            var rad = NS_Style.radius;

            Pen.fillColor_( NS_Style.highlight );
            Pen.addRoundedRect(rect, rad, rad);
            Pen.fill;
        })
    }
}
