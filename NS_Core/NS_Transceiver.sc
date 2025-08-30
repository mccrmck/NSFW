NS_Transceiver {
    classvar <continuousQueue, <discreteQueue;
    classvar listenFunc, isListening = false;
    classvar <excludePaths;

    *initClass {
        continuousQueue = List.newClear(0);
        discreteQueue   = List.newClear(0);

        excludePaths    = [
            "status.reply", 
            //"inSynth",
            "InLevels",
            "OutLevels",
            "peakRMS",
            "n_end",
            "tr",
            "n_go",
            "yawnalysis"
        ];

        listenFunc = { |msg, time, replyAddr, recvPort|
            var path = msg[0];
            var pathCheck = excludePaths.collect({ |str| 
                path.asString.contains(str)
            });

            if(pathCheck.asInteger.sum == 0, {
                var conQueue = continuousQueue.size > 0;
                var disQueue = discreteQueue.size > 0;
                if( conQueue or: disQueue,{
                    var discreteBools = ["button", "touch", "switch"]
                    .collect({ |string| msg.asString.contains(string) });

                    if(discreteBools.asInteger.sum == 0 and: conQueue,{
                        var nsControl = continuousQueue.removeAt(0);
                        nsControl.mapped = 'mapped';
                        this.assignOSCControllerContinuous(nsControl, path, replyAddr);
                    });

                    if(discreteBools.asInteger.sum > 0 and: disQueue,{
                        var nsControl = discreteQueue.removeAt(0);
                        nsControl.mapped = 'mapped';
                        this.assignOSCControllerDiscrete(nsControl, path, replyAddr);
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
    }

    *clearQueues { 
        [continuousQueue, discreteQueue].do({ |q| this.clearQueue(q) })
    }

    *clearQueue { |queue|
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

    *assignOSCControllerContinuous { |nsControl, path, netAddr|
        nsControl.addAction(\controller,{ |c| 
            netAddr.sendMsg(path, c.normValue)
        });

        nsControl.addResponder(\controller,
            OSCFunc({ |msg|
                nsControl.normValue_(msg[1], \controller);
            }, path, netAddr)
        );
    }

    *assignOSCControllerDiscrete { |nsControl, path, netAddr|
        nsControl.addAction(\controller,{ |c|
            netAddr.sendMsg(path, c.value)
        });

        nsControl.addResponder(\controller,
            OSCFunc({ |msg|
                nsControl.value_(msg[1], \controller);
            }, path, netAddr)
        )
    }
}

// MIDI 
// MIDIIn.addFuncTo(\noteOn,{ |src, chan, num, val|
//     "MIDI Message Received:\n\ttype: %\n\tsrc: %\n\tchan: %\n\tnum: %\n\tval: %\n\n".postf(type, src, chan, num, val) 
// })
