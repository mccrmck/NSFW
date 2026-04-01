NS_ControlModule {
    var <>controls;
    var <loaded = false;

    *new { ^super.new.init }

    init { controls = NS_ControlDict() }

    free { controls.do(_.free) }

    save { 
        var saveArray = List.newClear(0);
        var ctrlVals  = controls.collect({ |c| c.value }); // .collect turns List into Array
        var responders = controls.collect({ |c|          // this is wack
            var func = c.responderDict['controller'];
            func !? { [func.path, func.srcID] }
        });

        saveArray.add(ctrlVals);   // loadArray[0]
        saveArray.add(responders); // loadArray[1]
        this.saveExtra(saveArray); // loadArray[2]

        ^saveArray
    }

    saveExtra { |saveArray| }

    load { |loadArray, cond, action|
        loaded = false;

        // oscFuncs
        loadArray[1].do({ |pathAddr, index|
            pathAddr !? {
                var path = pathAddr[0];
                var addr = pathAddr[1];
                var ctrl = controls[index];

                ctrl.mapped = 'mapped';

                if(ctrl.spec.step == 1,{
                    NS_Transceiver.assignOSCControllerDiscrete(ctrl, path, addr)  
                },{       
                    NS_Transceiver.assignOSCControllerContinuous(ctrl, path, addr)
                });
                // cond.wait somewhere?
            }
        });

        // controls
        loadArray[0].do({ |ctrlVal, index|
            ctrlVal !? {
                controls[index].value_(ctrlVal);
            }
        });

        // anything extra
        this.loadExtra(loadArray[2], cond, { loaded = true; cond.signalOne });

        cond.wait { loaded };
        action.value
    }

    loadExtra { |loadArray, cond, action|
        // this needs to be in every overloaded .loadExtra
        action.value
    }
}
