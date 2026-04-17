NS_ControlModuleTest : UnitTest {
    var report;

    setUp {
        report = NS_Test.report;
    }

    test_newInstance {
        var c = NS_ControlModule();

        this.assert(c.controlDict.class == NS_ControlDict, "init creats ControlDict", report);
    }

    // can I test saving and loading? Write files and compare?

}
