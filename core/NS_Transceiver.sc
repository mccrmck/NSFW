NS_Transceiver {
    classvar <excludePaths;
    classvar <continuousQueue, <discreteQueue;
    classvar oscListenFunc, midiListenFunc;
    classvar isListening = false;

    *initClass {
        // This could also be:
        // if(NSFW.servers.collectAs({ |srv| srv.id }, Array).contains(replyAddr.port).not) 
        // { /*do a thing*/  }
        // when I send messages from sclang to the sclang port, I get infinite loops, 
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
            "root", // disable o-s-c tabs
            "tr",
            "yawnalysis"
        ];

        continuousQueue = LinkedList();
        discreteQueue   = LinkedList();

        oscListenFunc = { |msg, time, replyAddr, recvPort|
            var path = msg[0];
            // consider excluding all messages from NetAddr.localAddr, see above
            var pathOk = excludePaths.collect { |str| 
                path.asString.contains(str)
            }.reduce('or').not;

            if(pathOk) {
                var conQueue = continuousQueue.size > 0;
                var disQueue = discreteQueue.size > 0;
                var disWidget = ["button", "panel", "switch", "touch"]
                .collect { |str| path.asString.contains(str) }.reduce('or');

                if(disWidget) {
                    if(disQueue) 
                    { discreteQueue.popFirst.assignOSCcontroller(path, replyAddr) }
                } {
                    if(conQueue) 
                    { continuousQueue.popFirst.assignOSCcontroller(path, replyAddr) }
                };
            };

            this.checkEmptyQueues
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
                { this.checkEmptyQueues }
            }
        )
    }

    *checkEmptyQueues {
        if(discreteQueue.size == 0 and: continuousQueue.size == 0) { 
            this.listenForControllers(false)
        }
    }

    *addToDiscreteQueue { |nsControl| discreteQueue.add(nsControl) }

    *removeFromDiscreteQueue { |nsControl| 
        discreteQueue.remove(nsControl);
        this.checkEmptyQueues
    }

    *addToContinuousQueue { |nsControl| continuousQueue.add(nsControl) }

    *removeFromContinuousQueue { |nsControl| 
        continuousQueue.remove(nsControl);
        this.checkEmptyQueues
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
