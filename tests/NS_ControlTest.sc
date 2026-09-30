NS_ControlStringTest : UnitTest {
    var report;

    setUp {
        report = NS_Test.report;
    }

    test_newInstance {
        var c = NS_ControlString("testString", "default");

        this.assert(c.label == "testString", "init Label", report);
        this.assert(c.spec == nil, "init spec", report);
        this.assert(c.value == "default", "init value", report);
        this.assert(c.actionDict == IdentityDictionary(), "init actionDict", report);
        this.assert(c.actionDict.size == 0, "actionDict has size of 0", report);
    }

    test_value {
        var c = NS_ControlString("testString", "default");

        this.assert(c.value == "default", "value is set to initVal", report);

        c.value_("newVal");
        this.assert(c.value == "newVal", "value sets new value", report);

        c.normValue_("normVal");
        this.assert(c.value == "normVal", "normValue sets new value", report);

        c.resetValue;
        this.assert(c.value == "default", "reset returns value to initalization value", report);
    }

    test_saveLoad {
        var c = NS_ControlString("testString", "default");

        var save = c.save;
        this.assert(save == "default", "save returns value", report);
        
        c.value_("unique");
        c.load(save);
        this.assert(c.value == "default", "load updates value", report);
    }
}

NS_ControlIntTest : UnitTest {
    var report, floatError;

    setUp {
        report = NS_Test.report;
        floatError = NS_Test.epsilon;
    }

    test_newInstance {
        var c = NS_ControlInt("testInt", 0, 3, 1);

        this.assert(c.label == "testInt", "init label", report);
        this.assert(c.spec == ControlSpec(0, 3, \lin, 1), "init spec", report);
        this.assert(c.value == 1, "init value", report);
        this.assert(c.value.isInteger, "value is integer", report);
        this.assert(c.mapped == 'unmapped', "default state is unmapped", report);
        this.assert(c.actionDict == IdentityDictionary(), "init actionDict", report);
        this.assert(c.actionDict.size == 0, "actionDict has size of 0", report);
        this.assert(c.responderDict == IdentityDictionary(), "init responderDict", report);
        this.assert(c.responderDict.size == 0, "responderDict has size of 0", report);
    }

    test_value {
        var c = NS_ControlInt("testInt", 0, 3, 1);

        this.assert(c.value == 1, "value is set to initVal", report);

        c.value_(3);
        this.assert(c.value == 3, "value sets new value",  report);
        this.assert(c.normValue == 1, "normValue scales to 0-1", report);

        c.normValue_(0.5);
        this.assert(c.value == 2, "normValue rounds new value", report);
        // this seems like surprising behaviour, but we want our value to snap/round
        this.assertFloatEquals(c.normValue, 2/3, "normValue rounds new value", floatError, report);

        c.resetValue;
        this.assert(c.value == 1, "reset returns value to initalization value", report);
    }

    test_spec {
        var norm;
        var c = NS_ControlInt("testInt", 0, 3, 1);

        norm = c.normValue;
        c.spec_(0, 4);
        this.assert(c.value == 1, "scales/rounds value to new spec", report);
        c.spec_(0, 6);
        this.assert(c.value == 2, "scales/rounds value to new spec", report);
        this.assertFloatEquals(c.normValue, norm, "normValue stays the same across compatible specs", floatError, report);
        c.value_(9);
        this.assert(c.value == 6, "spec constrains at boundaries", report);
    }

    test_actionDict {
        var n = 0;
        var c = NS_ControlInt("testInt", 0, 3, 1);
        var f = { |c| n = c.value };

        c.addAction(\testAction, f, true);
        this.assert(c.actionDict.size == 1, "addAction adds to actionDict", report);
        this.assert(c.actionDict['testAction'].notNil, "addAction adds correct key", report);
        this.assert(c.actionDict['testAction'] == f, "addAction adds correct function", report);
        this.assert(n == 1, "addAction executes function", report);

        c.value_(2);
        this.assert(n == 2, "value executes function", report);
        c.value_(0, \testAction);
        this.assert(n == 2, "excludeKeys prevent function execution", report);

        c.addAction(\anotherTestAction, { |c| n = n * 2 }, false);
        this.assert(n == 2, "addAction without update", report);

        c.removeAction(\anotherTestAction);
        this.assert(c.actionDict.size == 1, "removeAction removes from actionDict", report);
    }

    test_responderDict {
        var c = NS_ControlInt("test", 0, 3, 1);
        var o = OSCFunc({ |msg| 1 + 1 });

        c.addResponder(\testResponder, o);
        this.assert(c.responderDict.size == 1, "addResponder adds to responderDict", report);
        this.assert(c.responderDict['testResponder'].notNil, "addResponder adds correct key", report);
        this.assert(c.responderDict['testResponder'] == o, "addResponder adds correct object", report);

        c.removeResponder(\testResponder);
        this.assert(c.actionDict.size == 0, "removeResponder removes from responderDict", report);
    }

    test_free {
        var c = NS_ControlInt("test", 0, 3, 2);

        c.addAction(\action, { |c| c.value = c.value * 2 }, false);
        c.addResponder(\responder, OSCFunc({ |m| m.postln },'/address'));
        c.free;
        this.assert(c.actionDict.size == 0, "free empties actionDict", report);
        this.assert(c.responderDict.size == 0, "free empties responderDict", report);
    }

    //test_saveLoad {
    //    var c = NS_ControlInt("test", 0, 3, 2);
    //    var path = '/address';
    //    var addr = NetAddr("localhost", 8080);
    //    var save;
    //
    //    c.addResponder(\responder, OSCFunc({ |m| m.postln }, path, addr));
    //    save = c.save;
    //    this.assert(save == [c.value, [path, addr]], "save returns value and responderDict info", report);
    //}
}

NS_ControlFloatTest : UnitTest {
    var report, floatError;

    setUp {
        report = NS_Test.report;
        floatError = NS_Test.epsilon;
    }

    test_newInstance {
        var c = NS_ControlFloat("testFloat", \unipolar, 0.5);

        this.assert(c.label == "testFloat", "init label", report);
        this.assert(c.spec == ControlSpec(0, 1, \lin), "init spec", report);
        this.assert(c.value == 0.5, "init value", report);
        this.assert(c.mapped == 'unmapped', "default state is unmapped", report);
        this.assert(c.actionDict == IdentityDictionary(), "init actionDict", report);
        this.assert(c.actionDict.size == 0, "actionDict has size of 0", report);
        this.assert(c.responderDict == IdentityDictionary(), "init responderDict", report);
        this.assert(c.responderDict.size == 0, "responderDict has size of 0", report);
    }

    test_value {
        var c = NS_ControlFloat("testFloat", ControlSpec(1, 100, \exp), 2);

        this.assert(c.value == 2, "value is set to initVal", report);

        c.value_(10);
        this.assertFloatEquals(c.value, 10, "value sets new value", floatError, report);
        this.assertFloatEquals(c.normValue, 0.5, "normValue scales to 0-1", floatError, report);

        c.normValue_(0.25);
        this.assertFloatEquals(c.value, 3.1622776601684, "normValue sets new value", floatError, report);
        this.assertFloatEquals(c.normValue, 0.25, "normValue sets new normValue", floatError, report);

        c.resetValue;
        this.assert(c.value == 2, "reset returns value to initalization value", report);
    }

    test_spec {
        var norm;
        var c = NS_ControlFloat("testFloat", \db, -12);

        this.assert(c.spec ==  ControlSpec(-inf, 0.0, \db, 0.0, -inf, "dB"), "convert symbol to spec", report);
        norm = c.normValue;

        c.spec_(ControlSpec(-1, 1, \lin));
        this.assert(c.spec == ControlSpec(-1, 1, \lin), "assign new spec", report);
        this.assertFloatEquals(c.value, 0.0023744672545445, "scale value to new spec", floatError, report);
        this.assertFloatEquals(c.normValue, norm, "normValue is unchanged", floatError, report);

        c.value_(4);
        this.assert(c.value == 1, "spec constrains at boundaries", report);
    }

    test_actionDict {
        var n = 0;
        var c = NS_ControlFloat("testFloat", \freq, 440);
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
        var c = NS_ControlFloat("test", \db, -12);
        var o = OSCFunc({ |msg| 1 + 1 });

        c.addResponder(\testResponder, o);
        this.assert(c.responderDict.size == 1, "addResponder adds to responderDict", report);
        this.assert(c.responderDict['testResponder'].notNil, "addResponder adds correct key", report);
        this.assert(c.responderDict['testResponder'] == o, "addResponder adds correct object", report);

        c.removeResponder(\testResponder);
        this.assert(c.actionDict.size == 0, "removeResponder removes from responderDict", report);
    }

    test_free {
        var c = NS_ControlFloat("test", \freq, 440);

        c.addAction(\action, { |c| c.value = c.value * 2 }, false);
        c.addResponder(\responder, OSCFunc({ |m| m.postln },'/address'));
        c.free;
        this.assert(c.actionDict.size == 0, "free empties actionDict", report);
        this.assert(c.responderDict.size == 0, "free empties responderDict", report);
    }
}
