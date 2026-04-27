NS_Transceiver {
    classvar <excludePaths;
    classvar <continuousQueue, <discreteQueue;
    classvar oscListenFunc, midiListenFunc;
    classvar isListening = false;


    classvar controlQueue, oscListenFuncNew;

    *initClass {
        excludePaths = [
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

        continuousQueue = LinkedList();
        discreteQueue   = LinkedList();

        //oscListenFuncNew = { |msg, time, replyAddr, recvPort|
        //    var nsControl = controlQueue.first;
        //
        //    nsControl !? {
        //        var path = msg[0];
        //        var pathOk = excludePaths.collect { |str| 
        //            path.asString.contains(str)
        //        }.reduce('or').not;
        //
        //        if(pathOk) {
        //            var disWidget = ["button", "touch", "switch"]
        //            .collect { |str| path.asString.contains(str) }.reduce('or');
        //
        //            var args = nsControl.class.switch(
        //                NS_ControlInt,   { [path, replyAddr] },
        //                NS_ControlFloat, { [path, replyAddr] },
        //            );
        //
        //            controlQueue.popFirst.assignOSCcontroller(*args)
        //        }
        //    } ?? { this.listenForControllers(false) }
        //};

        oscListenFunc = { |msg, time, replyAddr, recvPort|
            var conQueue = continuousQueue.size > 0;
            var disQueue = discreteQueue.size > 0;

            // can I remove some of the nested functions here?
            if(conQueue or: disQueue) {
                var path = msg[0];
                var pathOk = excludePaths.collect { |str| 
                    path.asString.contains(str)
                }.reduce('or').not;

                if(pathOk) {
                    var nsControl;
                    var disWidget = ["button", "touch", "switch"]
                    .collect { |str| path.asString.contains(str) }.reduce('or');

                    if(disWidget) 
                    { nsControl = discreteQueue.popFirst } 
                    { nsControl = continuousQueue.popFirst };

                    nsControl.assignOSCcontroller(path, replyAddr)
                }
            } {
                this.listenForControllers(false) 
            }
        };


        midiListenFunc = ( // src/uid, chan, num, val

            control: { |src, chan, num, val|

                ['control', src, chan, num, val].postln
                //if(continuousQueue.size > 0) {
                //    var nsControl = continuousQueue.popFirst;
                //    nsControl.assignMIDIcontroller(*args)
                //} {
                //    this.listenForControllers(false) 
                //}


            },
            noteOn: { |src, chan, num, val|
                ['noteOn', src, chan, num, val].postln
            },
            noteOff: { |src, chan, num, val|
                ['noteOff', src, chan, num, val].postln
            },
            program: { |...args|
                var nsControl = discreteQueue.popFirst;

                ['program'].postln;
                nsControl !? // if nsControl.notNil...
                { nsControl.assignMIDIcontroller(*args) } ?? // ...map it
                {
                    if(continuousQueue.size == 0) { 
                        this.listenForControllers(false) 
                    }
                }

            }
        )
    }

    // can the controlType be used instead of the 'type' argument?
    *addToQueue { |nsControl, type|
        if(type == 'discrete') 
        { discreteQueue.add(nsControl) }
        { continuousQueue.add(nsControl) }
    }

    *removeFromQueue { |nsControl, type|
        if(type == 'discrete') 
        { discreteQueue.remove(nsControl) }
        { continuousQueue.remove(nsControl) };

        if(discreteQueue.size == 0 and: discreteQueue.size == 0) { 
            this.listenForControllers(false)
        }
    }

    *listenForControllers { |bool|
        this.listenForOSC(bool);    
        this.listenForMIDI(bool);
        isListening = bool;
    }

    /*==== OSC ====*/

    *listenForOSC { |bool|
        if(bool) 
        { if(isListening.not) { thisProcess.addOSCRecvFunc(oscListenFunc) } }
        { thisProcess.removeOSCRecvFunc(oscListenFunc) }
    }

    /*==== MIDI ====*/

    /* add 14-bit MIDI if/when you have a capable controller */

    *listenForMIDI { |bool|
        var msgTypes = midiListenFunc.keys;

        if(bool) {
            if(isListening.not) { 
                msgTypes.do { |t| MIDIIn.addFuncTo(t, midiListenFunc[t]) }
            }
        } {
            msgTypes.do { |t| MIDIIn.removeFuncFrom(t, midiListenFunc[t]) }
        }
    }
}
