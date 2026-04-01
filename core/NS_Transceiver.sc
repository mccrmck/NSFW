NS_Transceiver {
    classvar <continuousQueue, <discreteQueue;
    classvar midiListenFunc, oscListenFunc;
    classvar oscLastContinuousPath, oscLastDiscretePath;
    classvar isListening = false;
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
            "b_set",
            "b_setn",
            "b_getn",
            "n_go",
            "n_end",
            "tr",
            "yawnalysis"
        ];

        oscListenFunc = { |msg, time, replyAddr, recvPort|
            var path = msg[0];
            var pathCheck = excludePaths.collect{ |str| 
                path.asString.contains(str)
            };

            // there's too many nested funcions here...
            if(pathCheck.asInteger.sum == 0) {
                var conQueue = continuousQueue.size > 0;
                var disQueue = discreteQueue.size > 0;

                if(conQueue or: disQueue) {
                    var index;
                    var discreteBools = ["button", "touch", "switch"]
                    .collect{ |str| msg.asString.contains(str) }.asInteger.sum;

                    if(discreteBools == 0 and: conQueue) {
                        if(path != oscLastContinuousPath) {
                            var nsControl = continuousQueue.removeAt(0);
                            oscLastContinuousPath = path;
                            this.assignOSCControllerContinuous(nsControl, path, replyAddr);
                        }
                    };

                    if(discreteBools > 0 and: disQueue) {
                        if(path != oscLastDiscretePath) {
                            var nsControl = discreteQueue.removeAt(0);
                            oscLastDiscretePath = path;
                            this.assignOSCControllerDiscrete(nsControl, path, replyAddr);
                        }
                    };
                }
                { this.listenForControllers(false) }
            };
        };

        midiListenFunc = ( // src/uid, chan, num, val
            control: {  |...args|

                if(continuousQueue.size > 0) {
                    var nsControl = continuousQueue.removeAt(0);
                    this.assignMIDIControllerContinuous(nsControl, *args);
                    // this should move elsewhere, no?
                    this.listenForControllers(false)
                }
            },
            noteOn: { |src, chan, num, val|
                ['noteOn', src, chan, num, val].postln
            },
            noteOff: { |src, chan, num, val|
                ['noteOff', src, chan, num, val].postln
            },
            program: { |src, chan, num, val|
                ['program', src, chan, num, val].postln
            },
        )
    }

    *addToQueue { |nsControl, type|
        if(type == 'discrete') 
        { discreteQueue.add( nsControl ) }
        { continuousQueue.add( nsControl ) }
    }

    *clearAssignedController { |nsControl|
        nsControl
        .removeAction(\oscController).removeResponder(\oscController)
        .removeAction(\midiController).removeResponder(\midiController);
    }

    *clearQueues { 
        continuousQueue.do { |nsControl| nsControl.mapped = 'unmapped' };
        discreteQueue.do { |nsControl| nsControl.mapped = 'unmapped' };
        continuousQueue.clear;
        discreteQueue.clear;
    }

    *listenForControllers { |bool|
        this.listenForOSC(bool);    
        this.listenForMIDI(bool);
        isListening = bool;
    }

    /*==== OSC ====*/

    *listenForOSC { |bool|
        if(bool) {
            if(isListening.not) { thisProcess.addOSCRecvFunc(oscListenFunc) };
        }{
            thisProcess.removeOSCRecvFunc(oscListenFunc);
        }
    }

    *assignOSCControllerContinuous { |nsControl, path, netAddr|
        nsControl.mapped = 'mapped';

        nsControl.addResponder(\oscController,
            OSCFunc({ |msg|
                nsControl.normValue_(msg[1], \oscController); // seems to get gummy without this key
            }, path, netAddr)
        );

        nsControl.addAction(\oscController,{ |c| 
            netAddr.sendMsg(path, c.normValue)
        });
    }

    *assignOSCControllerDiscrete { |nsControl, path, netAddr|
        nsControl.mapped = 'mapped';

        nsControl.addResponder(\oscController,
            OSCFunc({ |msg|
                nsControl.value_(msg[1], \oscController); // seems to get gummy without this key
            }, path, netAddr)
        );

        nsControl.addAction(\oscController,{ |c|
            netAddr.sendMsg(path, c.value)
        });
    }

    /*==== MIDI ====*/

    /* refactor for 14-bit MIDI if/when you have a capable controller */

    *listenForMIDI { |bool|
        if(MIDIClient.initialized.not) 
        { MIDIClient.init; MIDIIn.connectAll }
        { MIDIClient.list}; // refreshes list of MIDIEndPoints

        if(bool) {
            if(isListening.not) { 
                [\noteOn, \noteOff, \control, \program].do { |type|
                    MIDIIn.addFuncTo(type, midiListenFunc[type])
                }
            };
        } {
            [\noteOn, \noteOff, \control, \program].do { |type|
                MIDIIn.removeFuncFrom(type, midiListenFunc[type])
            }
        }
    }

    *assignMIDIControllerContinuous { |nsControl, src, chan, num, val|
        nsControl.mapped = 'mapped';

        // check if MIDIController class has two-way communication?
        //
        //nsControl.addAction(\midiController, { |c|
        //    MIDIOut()
        //});

        nsControl.addResponder(\midiController, 
            MIDIFunc.cc({ |val|
                nsControl.normValue_(val / 127)
            }, num, chan, src)
        );
    }

    *assignMIDIControllerDiscrete { |nsControl, src, chan, num, val|

        // consider the case of switches, this will require some math methinks..

    }
}
