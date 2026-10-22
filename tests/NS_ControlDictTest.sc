NS_ControlDictTest : UnitTest {
    var report;

    setUp {
        report = NS_Test.report;
    }

    test_newInstance {
        var c = NS_ControlDict();
        var d = NS_ControlFloat('test', \unipolar);
        var e = NS_ControlDict(d);

        this.assert(c.controls == Event(), "init controls", report);
        this.assert(c.controls.size == 0, "init with size of arguments passed", report);

        this.assert(e.controls.size == 1, "init controls with one entry", report);
        this.assert(e.controls['test'].notNil, "init with correct key", report);
        this.assert(e.controls['test'] == d, "init with correct object", report);
    }

    test_add {
        var c = NS_ControlFloat('testOne', \unipolar);
        var d = NS_ControlString('testTwo', "great");
        var e = NS_ControlDict();

        this.assert(e.controls.size == 0, "init with empty dictionary", report);

        e.addAll(c, d); // uses .add internally, not sure if I need an extra test for that
        this.assert(e.controls.size == 2, "add two controls at once", report);
        this.assert(e.controls['testOne'].notNil, "key added as symbol", report);
        this.assert(e.controls['testOne'] == c, "correct object added", report);
    }

    test_saveLoad {
        var saved;
        var n = NetAddr("0.0.0.0", 6000);
        var c = NS_ControlString('string', "persnickety");
        var d = NS_ControlInt('int', 0, 2, 0);
        var e = NS_ControlFloat('float', \unipolar, 0.0);
        var f = NS_ControlDict(c, d, e);

        c.value_("smoochie");
        d.value_(2);
        e.value_(0.5).prAddResponder(\oscController, OSCFunc({}, '/test', n));

        saved = f.save;

        this.assert(
            saved == (
                'int': [2, []], 
                'string': "smoochie", 
                'float': [0.5, [['OSC', '/test', n.ip, n.port]]]
            ),
            "save collects values and responder info for all controls",
            report
        );

        f.clear;

        this.assert(c.value == "persnickety");
        this.assert(d.value == 0);
        this.assert(e.value == 0.0);

        f.load(saved);

        this.assert(c.value == "smoochie", "controlDict loads saved string value", report);
        this.assert(d.value == 2, "controlDict loads saved int value", report);
        this.assert(e.value == 0.5, "controlDict loads saved float value", report);
        this.assert(e.responderDict['oscController'].notNil, "controlDict loads saved responder", report);
    }
}
