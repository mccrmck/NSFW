NS_ControlDictTest : UnitTest {
    var report;

    setUp {
        report = NS_Test.report;
    }

    test_newInstance {
        var c = NS_ControlDict();
        var d = NS_ControlFloat("test", \unipolar);
        var e = NS_ControlDict(d);

        this.assert(c.controls == IdentityDictionary(), "init controls", report);
        this.assert(c.controls.size == 0, "init with size of arguments passed", report);

        this.assert(e.controls.size == 1, "init controls with one entry", report);
        this.assert(e.controls['test'].notNil, "init with correct key", report);
        this.assert(e.controls['test'] == d, "init with correct object", report);
    }

    test_add {
        var c = NS_ControlFloat("testOne", \unipolar);
        var d = NS_ControlString("testTwo", "great");
        var e = NS_ControlDict();

        this.assert(e.controls.size == 0, "init with empty dictionary", report);

        e.addAll(c, d); // uses .add internally, not sure if I need an extra test for that
        this.assert(e.controls.size == 2, "add two controls at once", report);
        this.assert(e.controls['testOne'].notNil, "key added as symbol", report);
        this.assert(e.controls['testOne'] == c, "correct object added", report);
    }
}
