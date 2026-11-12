NS_TransceiverTest : UnitTest {
    var report;

    setUp {
        report = NS_Test.report
    }

    test_queueAddRemove {
        var c = NS_ControlInt(\int, 0, 1, 0);
        var d = NS_ControlFloat(\float, \freq, 220);
        
        NS_Transceiver.addToDiscreteQueue(c);
        this.assert(NS_Transceiver.discreteQueue.size == 1, "add to discreteQueue", report);
        NS_Transceiver.removeFromDiscreteQueue(c);
        this.assert(NS_Transceiver.discreteQueue.size == 0, "remove from discreteQueue", report);

        NS_Transceiver.addToContinuousQueue(d);
        this.assert(NS_Transceiver.continuousQueue.size == 1, "add to continuousQueue", report);
        NS_Transceiver.removeFromContinuousQueue(d);
        this.assert(NS_Transceiver.continuousQueue.size == 0, "remove from continuousQueue", report);
    }

    test_controlIntMap {
        var cond = CondVar();
        var c = NS_ControlInt(\int, 0, 3, 0);
        var addr = NetAddr.localAddr;
        var path = '/touch_69';
        var otherValue = 0;
        var oscFunc = OSCFunc({ |msg| otherValue = msg[1] }, path, addr);

        c.addAction(\signal, { cond.signalOne });

        fork {
            c.toggleAutoAssign;
            this.assert(c.mapped == 'listening', "toggleAutoAssign enables listening", report);
            this.assert(NS_Transceiver.discreteQueue.size == 1, "Int added to discreteQueue", report);

            addr.sendMsg(path, 0);
            cond.waitFor(1, { c.mapped == 'mapped' });

            this.assert(c.mapped == 'mapped', "incoming OSC messages updates mapped status", report);
            this.assert(c.actionDict['oscController'].notNil, "assignOSCcontroller adds action", report);
            this.assert(c.responderDict['oscController'].notNil, "assignOSCcontroller adds responder", report);

            NetAddr.localAddr.sendMsg(path, 2);
            this.assert(c.value == 2, "assignOSCcontroller maps controlInt to osc address", report);
            c.value_(1);
            this.assert(otherValue == 1, "assignOSCcontroller has two-way communication", report);
            oscFunc.free
        };
    }

    test_controlFloatMap {
        var cond = CondVar();
        var c = NS_ControlFloat(\float, \unipolar, 0.75);
        var addr = NetAddr.localAddr;
        var path = '/fader_420';
        var otherValue = 0;
        var oscFunc = OSCFunc({ |msg| otherValue = msg[1] }, path, addr);

        c.addAction(\signal, { cond.signalOne });

        fork {
            c.toggleAutoAssign;
            this.assert(c.mapped == 'listening', "toggleAutoAssign enables listening", report);
            this.assert(NS_Transceiver.continuousQueue.size == 1, "Int added to continuousQueue", report);

            addr.sendMsg(path, 0);
            cond.waitFor(1, { c.mapped == 'mapped' });

            this.assert(c.mapped == 'mapped', "incoming OSC messages updates mapped status", report);
            this.assert(c.actionDict['oscController'].notNil, "assignOSCcontroller adds action", report);
            this.assert(c.responderDict['oscController'].notNil, "assignOSCcontroller adds responder", report);

            NetAddr.localAddr.sendMsg(path, 0.5);
            this.assert(c.value == 0.5, "assignOSCcontroller maps controlFloat to osc address", report);
            c.value_(0.25);
            this.assert(otherValue == 0.25, "assignOSCcontroller has two-way communication", report);
            oscFunc.free
        };
    }
}
