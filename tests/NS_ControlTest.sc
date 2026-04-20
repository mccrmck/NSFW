NS_ControlTest : UnitTest {
    var report, floatError;

    setUp {
        report = NS_Test.report;
        floatError = NS_Test.epsilon;
    }

    test_newInstance {
        var c = NS_Control("test", \unipolar, 0.5);

        this.assert(c.label == "test", "init label", report);
        this.assert(c.spec == ControlSpec(0, 1, \lin), "init spec", report);
        this.assert(c.value == 0.5, "init value", report);
        this.assert(c.mapped == 'unmapped', "default state is unmapped", report);
        this.assert(c.actionDict == IdentityDictionary(), "init actionDict", report);
        this.assert(c.actionDict.size == 0, "actionDict has size of 0", report);
        this.assert(c.responderDict == IdentityDictionary(), "init responderDict", report);
        this.assert(c.responderDict.size == 0, "responderDict has size of 0", report);
    }

    test_value {
        var c = NS_Control("test", ControlSpec(1, 100, \exp), 2);

        this.assert(c.value == 2, "value is set to initVal", report);

        c.value_(10);
        this.assertFloatEquals(c.value, 10, "value sets new value", floatError, report);
        this.assertFloatEquals(c.normValue, 0.5, "normValue scales to 0-1", floatError, report);

        c.normValue_(0.25);
        this.assertFloatEquals(c.value, 3.1622776601684, "normValue sets new value", floatError, report);
        this.assertFloatEquals(c.normValue, 0.25, "normValue sets new value", floatError, report);

        c.resetValue;
        this.assert(c.value == 2, "reset returns value to initalization value", report);
    }

    test_spec {
        var norm;
        var c = NS_Control("test", \db, -12);

        this.assert(c.spec ==  ControlSpec(-inf, 0.0, \db, 0.0, -inf, "dB"), "convert symbol to spec", report);
        norm = c.normValue;

        c.spec_(ControlSpec(-1, 1, \lin));
        this.assert(c.spec == ControlSpec(-1, 1, \lin), "assign new spec", report);
        this.assertFloatEquals(c.value, 0.0023744672545445, "scale value to new spec", floatError, report);
        this.assertFloatEquals(c.normValue, norm, "normValue is unchanged", floatError, report);

        c.value_(4);
        this.assert(c.value == 1, "spec constrains at boundaries", report);
    }

    test_stringSpec {
        var c = NS_Control("test", \string, "default", report);

        this.assert(c.spec.isNil, "string spec is nil", report);
        this.assert(c.value == "default", "init string value", report);

        c.value_("newString");
        this.assert(c.value == "newString", "value changes string", report);
        this.assert(c.normValue == "newString", "normValue returns correct string", report);

        c.normValue_("yetAnotherString");
        this.assert(c.value == "yetAnotherString", "normValue changes string", report);
        this.assert(c.normValue == "yetAnotherString", "normValue returns correct", report);

        // test spec, must create some Error/Exception Classes...
    }

    test_actionDict {
        var n = 0;
        var c = NS_Control("test", \freq, 440);
        var f = { |c| n = c.value };

        c.addAction(\testAction, f, true);
        this.assert(c.actionDict.size == 1, "addAction adds to actionDict", report);
        this.assert(c.actionDict['testAction'].notNil, "addAction adds correct key", report);
        this.assert(c.actionDict['testAction'] == f, "addAction adds correct function", report);
        this.assert(n == 440, "addAction executes function", report);

        c.value_(220);
        this.assert(n == 220, "value executes function", report);
        c.value_(880, \testAction);
        this.assert(n == 220, "excludeKeys prevent function execution", report);

        c.addAction(\anotherTestAction, { |c| n = n * 2 }, false);
        this.assert(n == 220, "addAction without update", report);

        c.removeAction(\anotherTestAction);
        this.assert(c.actionDict.size == 1, "removeAction removes from actionDict", report);
    }

    test_responderDict {
        var c = NS_Control("test", \freq, 440);
        var o = OSCFunc({ |msg| 1 + 1 });

        c.addResponder(\testResponder, o);
        this.assert(c.responderDict.size == 1, "addResponder adds to responderDict", report);
        this.assert(c.responderDict['testResponder'].notNil, "addResponder adds correct key", report);
        this.assert(c.responderDict['testResponder'] == o, "addResponder adds correct object", report);

        c.removeResponder(\testResponder);
        this.assert(c.actionDict.size == 0, "removeResponder removes from responderDict", report);
    }

    test_free {
        var c = NS_Control("test", \freq, 440);

        c.addAction(\action, { |c| c.value = c.value * 2 }, false);
        c.addResponder(\responder, OSCFunc({ |m| m.postln },'localhost'));
        c.free;
        this.assert(c.actionDict.size == 0, "free empties actionDict", report);
        this.assert(c.responderDict.size == 0, "free empties responderDict", report);
    }
}
