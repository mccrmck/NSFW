NS_Test {
    classvar <epsilon = 1e-8; // float error tolerance

    *run {
        [
            NS_ControlDictTest,
            NS_ControlModuleTest,
            NS_ControlTest,
        ].do(_.run)

    }
}
