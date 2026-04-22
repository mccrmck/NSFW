/*
* a conveniece class for running all NS_Tests
* based on the `AtkTests` class found in the atk-sc3 library
*/

NS_Test {
    classvar <>report = false;
    classvar <epsilon = 1e-8; // float error tolerance

    *run { |verbose(false)|
        report = verbose;

        [
            NS_ControlDictTest,
            NS_ControlModuleTest,
            NS_ControlStringTest,
            NS_ControlIntTest,
            NS_ControlFloatTest,
            NS_ControlWidgetTest,
            NS_WidgetTest,
        ].do(_.run)
    }
}
