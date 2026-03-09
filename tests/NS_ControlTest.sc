NS_ControlTest : UnitTest {

    test_newInstance {
        var c = NS_Control("test", \unipolar, 0.5);

        this.assert(c.label == "test", "init label");
        this.assert(c.spec == ControlSpec(0, 1, \lin), "init spec");
        this.assert(c.value == 0.5, "init value");
        this.assert(c.mapped == 'unmapped', "default state is unmapped");
        this.assert(c.actionDict == IdentityDictionary(), "init actionDict");
        this.assert(c.actionDict.size == 0, "actionDict has size of 0");
        this.assert(c.responderDict == IdentityDictionary(), "init responderDict");
        this.assert(c.responderDict.size == 0, "responderDict has size of 0");
    }

    test_value {
        var c = NS_Control("test", ControlSpec(1, 100, \exp), 2);

        this.assert(c.value == 2, "value is set to initVal");

        c.value_(10);
        this.assertFloatEquals(c.value, 10, "value sets new value", NS_Test.epsilon);
        this.assertFloatEquals(c.normValue, 0.5, "normValue scales to 0-1", NS_Test.epsilon);

        c.normValue_(0.25);
        this.assertFloatEquals(c.value, 3.1622776601684, "normValue sets new value", NS_Test.epsilon);
        this.assertFloatEquals(c.normValue, 0.25, "normValue sets new value", NS_Test.epsilon);

        c.resetValue;
        this.assert(c.value == 2, "reset returns value to initalization value");
    }

    test_spec {
        var norm;
        var c = NS_Control("test", \db, -12);

        this.assert(c.spec ==  ControlSpec(-inf, 0.0, \db, 0.0, -inf, "dB"), "convert symbol to spec");
        norm = c.normValue;

        c.spec_(ControlSpec(-1, 1, \lin));
        this.assert(c.spec == ControlSpec(-1, 1, \lin), "assign new spec");
        this.assertFloatEquals(c.value, 0.0023744672545445, "scale value to new spec", NS_Test.epsilon);
        this.assertFloatEquals(c.normValue, norm, "normValue is unchanged", NS_Test.epsilon);

        c.value_(4);
        this.assert(c.value == 1, "spec constrains at boundaries");
    }

    test_stringSpec {
        var c = NS_Control("test", \string, "default");

        this.assert(c.spec.isNil, "string spec is nil");
        this.assert(c.value == "default", "init string value");

        c.value_("newString");
        this.assert(c.value == "newString", "value changes string");
        this.assert(c.normValue == "newString", "normValue returns correct string");

        c.normValue_("yetAnotherString");
        this.assert(c.value == "yetAnotherString", "normValue changes string");
        this.assert(c.normValue == "yetAnotherString", "normValue returns correct");

        // test spec, must create some Error/Exception Classes...
    }

    test_actionDict {
        var n = 0;
        var c = NS_Control("test", \freq, 440);
        var f = { |c| n = c.value };

        c.addAction(\testAction, f, true);
        this.assert(c.actionDict.size == 1, "addAction adds to actionDict");
        this.assert(c.actionDict['testAction'].notNil, "addAction adds correct key");
        this.assert(c.actionDict['testAction'] == f, "addAction adds correct function");
        this.assert(n == 440, "addAction executes function");

        c.value_(220);
        this.assert(n == 220, "value executes function");
        c.value_(880, \testAction);
        this.assert(n == 220, "excludeKeys prevent function execution");

        c.addAction(\anotherTestAction, { |c| n = n * 2 }, false);
        this.assert(n == 220, "addAction without update");

        c.removeAction(\anotherTestAction);
        this.assert(c.actionDict.size == 1, "removeAction removes from actionDict");
    }

    test_responderDict {
        var c = NS_Control("test", \freq, 440);
        var o = OSCFunc({ |msg| 1 + 1 });

        c.addResponder(\testResponder, o);
        this.assert(c.responderDict.size == 1, "addResponder adds to responderDict");
        this.assert(c.responderDict['testResponder'].notNil, "addResponder adds correct key");
        this.assert(c.responderDict['testResponder'] == o, "addResponder adds correct object");

        c.removeResponder(\testResponder);
        this.assert(c.actionDict.size == 0, "removeResponder removes from responderDict");
    }

    test_free {
        var c = NS_Control("test", \freq, 440);

        c.addAction(\action, { |c| c.value = c.value * 2 }, false);
        c.free;
        this.assert(c.actionDict.isNil, "free removes actionDict");
        this.assert(c.responderDict.isNil, "free removes responderDict");
    }
}
