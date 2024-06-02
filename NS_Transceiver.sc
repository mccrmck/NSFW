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

        // add touch messages here!

        case
        { msgString.contains("button") or:
        ( msgString.contains("touch") )} { incomingType = 'button' }
        { msgString.contains("fader")  } { incomingType = 'fader' }
        { msgString.contains("multi")  } { incomingType = 'multiFader' }
        { msgString.contains("switch") } { incomingType = 'switch' }
        { msgString.contains("xy")     } { incomingType = 'xy' };

        if(incomingType == type,{
          this.assignOSCController(module, index, msg[0], replyAddr);
          this.stopListenForControllers
        },{
          "wrong control type?".error
        });

      });
    };

    if(bool,{
      thisProcess.addOSCRecvFunc(listenFunc)
    },{
      thisProcess.removeOSCRecvFunc(listenFunc)
    })
  }

  *listenforMIDI { |bool| }

  *assignOSCController { |module, index, path, netAddr|

    module.oscFuncs[index] = OSCFunc({ |msg|
      var val = msg[1..];
      var spec, specX, specY;
      case
      { val.size == 1 }{
        spec = module.controls[index].spec;
        val  = spec.map(val[0]);
      }
      { val.size == 2 }{
        specX  = module.controls[index].specX;
        specY  = module.controls[index].specY;
        val[0] = specX.map(val[0]);
        val[1] = specY.map(val[1]);
      }
      { val.size > 2  }{};
      
      { module.controls[index].value_(val) }.defer


    }, path, netAddr );

    // how do I get the QTGui fucntion to .sendMsg to the controllers?
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

