NS_Transceiver {
  classvar listenFunc;
 
  *new {
    ^super.new.init
  }

  init {

  }

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
      //[msg, time, replyAddr, recvPort].postln;

      if(msg[0] != '/status.reply', {
        var msgString = msg.asString;

        case
        { msgString.contains("button") or:
        ( msgString.contains("touch") )} { incomingType = 'button' }
        { msgString.contains("fader")  } { incomingType = 'fader' }
        { msgString.contains("multi")  } { incomingType = 'multiFader' }
        { msgString.contains("switch") or:
        ( msgString.contains("radio"))} { incomingType = 'switch' }
        { msgString.contains("xy")     } { incomingType = 'xy' };

        if(incomingType == type,{

          if( incomingType == 'button' or: (incomingType == 'switch'),{
            this.assignOSCControllerDiscreet(module, index, msg[0], replyAddr);
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

  *listenforMIDI { |bool| }

  *assignOSCControllerContinuous { |module, index, path, netAddr|

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

    // how do I get the QTGui fucntion to .sendMsg to the controllers?
  }

  *assignOSCControllerDiscreet { |module, index, path, netAddr|

    module.oscFuncs[index] = OSCFunc({ |msg|
      var val = msg[1];

      { module.controls[index].valueAction_(val) }.defer

    }, path, netAddr );

  }

  *clearAssignedController { |module, index|
    module.oscFuncs[index].free;
    module.oscFuncs[index] = nil
  }


  *setController { |module, controlIndex|
    // oscFunc = module.oscFuncs[index];
    // var netAddr = oscFunc.srcID;
    // var path = oscFunc.path;
    // var val = 

   //netAddr.sendMsg(path,val)

  }

}



// OSC
// thisProcess.addOSCRecvFunc(func)

// MIDI 
// MIDIIn.addFuncTo(\noteOn,{|src, chan, num, val|"MIDI Message Received:\n\ttype: %\n\tsrc: %\n\tchan: %\n\tnum: %\n\tval: %\n\n".postf(type, src, chan, num, val))

