NS_ChannelStrip {
  var <group, <outBus;
  var <stripBus;
  var <stripGroup, <inGroup, <inSendGroup, slots, <slotGroups, <faderGroup;
  var <inModule, <slotModules, <fader;
  var <inSynth, <sends;

  *initClass {
    StartUp.add{
      SynthDef(\ns_stripFader,{
        var sig = In.ar(\bus.kr, 2);
        var mute = 1 - \mute.kr(0); 
        sig = ReplaceBadValues.ar(sig);
        sig = sig * mute;
        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(0));

        ReplaceOut.ar(\bus.kr, sig)
      }).add;

      SynthDef(\ns_stripIn,{
        var sig = In.ar(\bus.kr,2);
        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
        ReplaceOut.ar(\bus.kr,sig);
      }).add;

      SynthDef(\ns_stripSend,{
        var sig = In.ar(\inBus.kr,2);
        sig = sig * NS_Envs(\gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
        Out.ar(\outBus.kr,sig);
      }).add

    }
  }

  *new { |target, outBus, numSlots = 5| 
    ^super.newCopyArgs(target, outBus).init(numSlots)
  }

  init { |numSlots|

    stripBus   = Bus.audio(group.server,2);

    stripGroup = Group(group,\addToTail);
    inGroup    = Group(stripGroup,\addToTail);
    slots      = Group(stripGroup,\addToTail);
    slotGroups = numSlots.collect({ |i| Group(slots,\addToTail) });
    faderGroup = Group(stripGroup,\addToTail);

    sends = Array.newClear(4);
    fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);

    this.addSendSynth(outBus,0);
  }

  addInSynth {
    inSynth = Synth(\ns_stripIn,[\bus,stripBus],inGroup)
  }

  removeInSynth { inSynth.set(\gate,0) }

  addSendSynth { |outBus, sendIndex|
    sends[sendIndex] = Synth(\ns_stripSend,[\inBus,stripBus,\outBus,outBus],faderGroup,\addToTail)
  }

  addModuleToSlot {}

  removeModuleFromSlot {}

  free {}

  amp  { this.fader.get(\amp,{ |a| a.postln }) }
  amp_ { |amp| this.fader.set(\amp, amp) }

  toggleMute {
    this.fader.get(\mute,{ |muted|
      this.fader.set(\mute,1 - muted)
    })
  }

  outBus_ { |newBus|
    outBus = newBus;
    fader.set(\outBus,newBus)
  }

  pause {


  }

  unpause {

  }

}
