NS_ControlModuleTest : UnitTest {

    test_newInstance {
        var c = NS_ControlModule();

        this.assert(c.controls.class == NS_ControlDict, "init creats ControlDict");

    }

}
