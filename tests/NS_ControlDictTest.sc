NS_ControlDictTest : UnitTest {

    test_newInstance {
        var c = NS_ControlDict();
        var d = NS_Control("test", \unipolar);
        var e = NS_ControlDict(d);

        this.assert(c.controls == IdentityDictionary(), "init controls");
        this.assert(c.controls.size == 0, "init with size of arguments passed");

        this.assert(e.controls.size == 1, "init controls with one entry");
        this.assert(e.controls['test'].notNil, "init with correct key");
        this.assert(e.controls['test'] == d, "init with correct object");
    }

    test_add {
        var c = NS_Control("testOne", \unipolar);
        var d = NS_Control("testTwo", \bipolar);
        var e = NS_ControlDict();

        this.assert(e.controls.size == 0, "init with empty dictionary");

        e.addAll(c, d); // uses .add internally, not sure if I need an extra test for that
        this.assert(e.controls.size == 2, "add two controls at once");
        this.assert(e.controls['testOne'].notNil, "key added as symbol");
        this.assert(e.controls['testOne'] == c, "correct object added");

    }
}
