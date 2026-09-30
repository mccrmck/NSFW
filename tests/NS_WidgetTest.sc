NS_WidgetTest : UnitTest {
    var report;

    setUp {
        report = NS_Test.report;
    }

    test_newInstance {
        var w = NS_Widget();

        this.assert(w.mouseActionDict.notNil, "creates mouseActionDict", report);
        this.assertException({ w.drawWidget }, SubclassResponsibilityError, "method for subclasses", report);
    }

    test_mouseActions {
        var f = { 1 + 1 };
        var g = { 1 + 2 };
        var h = { 1 + 3 };
        var i = { 1 + 4 };
        var j = { 1 + 5 };
        var w = NS_Widget();

        w.addLeftClickAction(f);
        w.addLeftClickAction(g, \alt);
        w.addLeftClickAction(h, \cmd);
        w.addLeftClickAction(i, \ctrl);
        w.addLeftClickAction(j, \shift);
        this.assert(w.mouseActionDict['none']['leftClick'] == f, "leftClickAction is added", report);
        this.assert(w.mouseActionDict['alt']['leftClick'] == g, "leftClickAction + altMod is added", report);
        this.assert(w.mouseActionDict['cmd']['leftClick'] == h, "leftClickAction + cmdMod is added", report);
        this.assert(w.mouseActionDict['ctrl']['leftClick'] == i, "leftClickAction + ctrlMod is added", report);
        this.assert(w.mouseActionDict['shift']['leftClick'] == j, "leftClickAction + shiftMod is added", report);
    }
}
