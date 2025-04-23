NS_ControlSink : NS_ControlWidget {

    *new { |ns_control|
        if(ns_control.isNil,{ "must provide an NS_Control".warn });
        ^super.new.init(ns_control)
    }

    init { |control|

        view = DragBoth()
        .setBoth_(true)
        .align_(\left)
        .stringColor_(NS_Style.textDark)
        .background_(NS_Style.bGroundLight)
        .canReceiveDragHandler_({ View.currentDrag.isString })
        .receiveDragHandler_({
            var val = View.currentDrag;
            val = control.spec !? { control.spec.constrain(val) } ?? { val };

            view.object_(val);
            control.value_(val, \qtGui)
        });

        control.addAction(\qtGui,{|c| { view.object_(c.value ? "") }.defer })
    }
}
