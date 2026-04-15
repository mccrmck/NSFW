NS_ControlModule {
    var <controlDict;
    var <loaded = false;

    *new { ^super.new.init }

    init { controlDict = NS_ControlDict() }

    free { controlDict.do(_.free) }

    //save { 
    //    var saveArray = List.newClear(0);
    //    var ctrlVals  = controlDict.collect({ |c| c.value }); // .collect turns List into Array
    //    var responders = controlDict.collect({ |c|          // this is wack
    //        var func = c.responderDict['controller'];
    //        func !? { [func.path, func.srcID] }
    //    });
    //
    //    saveArray.add(ctrlVals);   // loadArray[0]
    //    saveArray.add(responders); // loadArray[1]
    //    this.saveExtra(saveArray); // loadArray[2]
    //
    //    ^saveArray
    //}

    saveExtra { |saveArray| }
    //
    //load { |loadArray, cond, action|
    //    loaded = false;
    //
    //    // oscFuncs
    //    loadArray[1].do({ |pathAddr, index|
    //        pathAddr !? {
    //            var path = pathAddr[0];
    //            var addr = pathAddr[1];
    //            var ctrl = controlDict[index];
    //
    //            ctrl.mapped = 'mapped';
    //
    //            if(ctrl.spec.step == 1,{
    //                NS_Transceiver.assignOSCControllerDiscrete(ctrl, path, addr)  
    //            },{       
    //                NS_Transceiver.assignOSCControllerContinuous(ctrl, path, addr)
    //            });
    //            // cond.wait somewhere?
    //        }
    //    });
    //
    //    // controlDict
    //    loadArray[0].do({ |ctrlVal, index|
    //        ctrlVal !? {
    //            controlDict[index].value_(ctrlVal);
    //        }
    //    });
    //
    //    // anything extra
    //    this.loadExtra(loadArray[2], cond, { loaded = true; cond.signalOne });
    //
    //    cond.wait { loaded };
    //    action.value
    //}

    loadExtra { |loadArray, cond, action|
        // this needs to be in every overloaded .loadExtra
        action.value
    }
}
