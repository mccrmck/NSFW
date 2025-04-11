NS_ContainerView : SCViewHolder {

    *new {
        ^super.new.init
    }

    init {
        view = UserView().drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var rad = NS_Style.radius;
            var rect = Rect(0,0,w,h);

            Pen.fillColor_( NS_Style.highlight );
            Pen.addRoundedRect(rect, rad, rad);
            Pen.fill;
        })
    }
}
