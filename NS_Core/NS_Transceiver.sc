NS_Transceiver {
    classvar <continuousQueue, <discreteQueue;
    classvar listenFunc, isListening = false;

    *initClass {
        continuousQueue = List.newClear(0);
        discreteQueue   = List.newClear(0);

        listenFunc = { |msg, time, replyAddr, recvPort|
            var path = msg[0];
            var pathCheck = [
                "status.reply", 
                "inSynth",
                "InLevels",
                "OutLevels",
                "n_end"
            ].collect({ |str| path.asString.contains(str) });

            if(pathCheck.asInteger.sum == 0, {
                if(continuousQueue.size > 0 or: (discreteQueue.size > 0),{
                    var msgString = msg.asString;
                    var discreteBools = ["button", "touch", "switch"]
                    .collect({ |string| msgString.contains(string) });

                    if(discreteBools.asInteger.sum == 0,{
                        this.assignOSCControllerContinuous(path, replyAddr)
                    },{
                        this.assignOSCControllerDiscrete(path, replyAddr)
                    })
                },{
                    this.listenForControllers(false)
                })
            });
        }
    }

    *addToQueue { |nsControl, type|
        if(type == 'discrete',{
            discreteQueue.add( nsControl ) 
        },{
            continuousQueue.add( nsControl ) 
        })
    }

    *clearAssignedController { |nsControl|
        nsControl.removeAction(\controller);
        nsControl.removeResponder(\controller);
        // module.oscFuncs[index].free;
       // module.oscFuncs[index] = nil
    }

    *clearQueues { 
        [continuousQueue, discreteQueue].do({ |q| this.clearQueue(q) })
    }

    *clearQueue { |queue|
        //  queue.do({ |ctrlEvent|
        //      var module = ctrlEvent['mod'];
        //      var index  = ctrlEvent['index'];

        //      { module.assignButtons[index].value_(0) }.defer
        //  });
        queue.clear
    }

    *listenForControllers { |bool|
        this.listenForOSC(bool);
        // this.listenforMIDI(bool);
    }

    *listenForOSC { |bool|
        if(bool,{
            if(isListening.not,{ thisProcess.addOSCRecvFunc(listenFunc) });
            isListening = true;
        },{
            thisProcess.removeOSCRecvFunc(listenFunc);
            isListening = false;
        })
    }

    *assignOSCControllerContinuous { |path, netAddr|
        if(continuousQueue.size > 0,{
            var nsControl = continuousQueue.removeAt(0);

            nsControl.addAction(\controller,{ |c| 
                netAddr.sendMsg(path, c.normValue)
            });

            nsControl.addResponder(\controller,
                OSCFunc({ |msg|
                    nsControl.normValue_(msg[1], \controller);
                }, path, netAddr)
            );
        })
    }

    *assignOSCControllerDiscrete { |path, netAddr|
        if(discreteQueue.size > 0,{
            var nsControl = discreteQueue.removeAt(0);

            nsControl.addAction(\controller,{ |c|
                netAddr.sendMsg(path, c.value)
            });

            nsControl.addResponder(\controller,
                OSCFunc({ |msg|
                    nsControl.value_(msg[1], \controller);
                }, path, netAddr)
            )
        })
    }
}

// MIDI 
// MIDIIn.addFuncTo(\noteOn,{ |src, chan, num, val|
//     "MIDI Message Received:\n\ttype: %\n\tsrc: %\n\tchan: %\n\tnum: %\n\tval: %\n\n".postf(type, src, chan, num, val) 
// })
