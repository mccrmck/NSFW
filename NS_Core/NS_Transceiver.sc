NS_Transceiver {
    classvar <continuousQueue, <discreteQueue;
    classvar listenFunc, isListening = false;

    *initClass {
        continuousQueue = List.newClear(0);
        discreteQueue   = List.newClear(0);

        listenFunc = { |msg, time, replyAddr, recvPort|
            var path = msg[0];

            if(path != '/status.reply' and: { path != '/inSynth'}, { // add s.meter, s.plotTree, etc.
                var msgString = msg.asString;
                var discreteBools = ["button", "touch", "switch", "radio"].collect({ |string| msgString.contains(string) });

                if( discreteBools.asInteger.sum == 0,{
                    this.assignOSCController('continuous', path, replyAddr)
                },{
                    this.assignOSCController('discrete', path, replyAddr)
                })
            });
        }
    }

    *addToQueue { |module, ctrlIndex, type|
        var ctrlEvent = ( mod: module, index: ctrlIndex );

        if( "button, switch".contains( type.asString ),{
            discreteQueue.add( ctrlEvent ) 
        },{
            continuousQueue.add( ctrlEvent ) 
        })
    }

    *clearAssignedController { |module, index|
        module.controls[index].removeAction(\controller);
        //module.controlTypes[index] = nil;
        module.oscFuncs[index].free;
        module.oscFuncs[index] = nil
    }

    *clearQueues { [continuousQueue, discreteQueue].do({ |q| this.clearQueue(q) }) }

    *clearQueue { |queue|
        queue.do({ |ctrlEvent|
            var module = ctrlEvent['mod'];
            var index = ctrlEvent['index'];

            { module.assignButtons[index].value_(0) }.defer
        });
        queue.clear
    }

    *listenForControllers { |bool|
        this.listenForOSC(bool);
        //this.listenforMIDI(bool);
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

    *assignOSCController { |whichQueue, path, netAddr|
        var queue = switch(whichQueue,
            'discrete', discreteQueue,
            'continuous', continuousQueue
        );
       // var typeKey = "OSC%".format(whichQueue).asSymbol;    // is this still necessary??

        if(queue.size > 0,{

            var ctrlEvent = queue.removeAt(0);
            var module = ctrlEvent['mod'];
            var index = ctrlEvent['index'];

            module.controls[index].addAction(\controller,{ |c| netAddr.sendMsg(path, c.normValue) });
        //    module.controlTypes[index] = typeKey;    // is this still necessary?
            module.oscFuncs[index] = OSCFunc({ |msg|

                module.controls[index].normValue_( msg[1], \controller);

            }, path, netAddr );
        },{
            this.listenForControllers(false);
        })

        //  this.listenForControllers(false);
        // this.clearQueue( queue )
    }
}

// MIDI 
// MIDIIn.addFuncTo(\noteOn,{|src, chan, num, val|"MIDI Message Received:\n\ttype: %\n\tsrc: %\n\tchan: %\n\tnum: %\n\tval: %\n\n".postf(type, src, chan, num, val))
