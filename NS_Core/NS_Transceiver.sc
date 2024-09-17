NS_Transceiver {
    classvar listenFunc;

    *listenForControllers { |module, ctrlIndex, type|
        this.listenForOSC(true, module, ctrlIndex, type);
        this.listenforMIDI(true);
    }

    *stopListenForControllers {
        this.listenForOSC(false);
        this.listenforMIDI(false);
    }

    *listenForOSC { |bool, module, index, type = 'fader'|
        thisProcess.removeOSCRecvFunc(listenFunc);

        listenFunc = { |msg, time, replyAddr, recvPort|
            var incomingType;

            if(msg[0] != '/status.reply' and: { msg[0] != '/inSynth'}, {
                var msgString = msg.asString;

                case
                { msgString.contains("button") or:
                ( msgString.contains("touch") )} { incomingType = 'button' }
                { msgString.contains("knob")   } { incomingType = 'knob' }
                { msgString.contains("fader")  } { incomingType = 'fader' }
                { msgString.contains("multi")  } { incomingType = 'multiFader' }
                { msgString.contains("switch") or:
                ( msgString.contains("radio") )} { incomingType = 'switch' }
                { msgString.contains("xy")     } { incomingType = 'xy' };

                if(incomingType == type,{

                    if( incomingType == 'button' or: (incomingType == 'switch'),{
                        this.assignOSCControllerDiscrete(module, index, msg[0], replyAddr);
                    },{
                        this.assignOSCControllerContinuous(module, index, msg[0], replyAddr);
                    });

                    this.stopListenForControllers
                },{
                    "wrong control type?".error
                });

            });
        };

        if(bool,{
            thisProcess.addOSCRecvFunc(listenFunc)
        })
    }

    *assignOSCControllerContinuous { |module, index, path, netAddr|
        module.controlTypes[index] = 'OSCcontinuous';

        module.oscFuncs[index] = OSCFunc({ |msg|
            var val = msg[1..];
            var spec, specs;

            if( val.size == 1,{
                spec = module.controls[index].spec;
                val = spec.map( *val )
            },{
                specs = module.controls[index].specs;
                val = val.collect({ |v, i| specs[i].map( v ) });
            });

            { module.controls[index].valueAction_(val) }.defer

        }, path, netAddr );
    }

    *assignOSCControllerDiscrete { |module, index, path, netAddr|

        module.controlTypes[index] = 'OSCdiscrete';

        module.oscFuncs[index] = OSCFunc({ |msg|
            var val = msg[1];

            { module.controls[index].valueAction_(val) }.defer

        }, path, netAddr );
    }

    *listenforMIDI { |bool, module, index, type| }

    *assignMIDIControllerContinuous {}
    *assignMIDIControllerDiscrete {

        MIDIFunc({})
    }

    *clearAssignedController { |module, index|
        this.stopListenForControllers;
        module.controlTypes[index] = nil;
        module.oscFuncs[index].free;
        module.oscFuncs[index] = nil
    }

    *setController/*FromQTGui*/ { |module, controlIndex|
        // oscFunc = module.oscFuncs[index];
        // var netAddr = oscFunc.srcID;
        // var path = oscFunc.path;
        // var val = 

        //netAddr.sendMsg(path,val)

    }
}

// MIDI 
// MIDIIn.addFuncTo(\noteOn,{|src, chan, num, val|"MIDI Message Received:\n\ttype: %\n\tsrc: %\n\tchan: %\n\tnum: %\n\tval: %\n\n".postf(type, src, chan, num, val))
