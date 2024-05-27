NS_ChannelStrip {
  var <group, <outBus;
  var <stripBus;
  var <stripGroup, <inGroup, <inSendGroup, slots, <slotGroups, <faderGroup;
  var <inModule, <slotModules, <fader;
  var <inSynth;

  *initClass {
    StartUp.add{
      SynthDef(\ns_stripFader,{
        var sig = In.ar(\inBus.kr, 2);
        var mute = 1 - \mute.kr(0); 
        sig = ReplaceBadValues.ar(sig);
        sig = sig * mute * \amp.kr(0);
        sig = sig * Env.asr().ar(2,\gate.kr(1));

        Out.ar(\outBus.kr, sig)
      }).add;

      SynthDef(\ns_stripIn,{
        var sig = In.ar(\inBus.kr,2);
        ReplaceOut.ar(\outBus.kr,sig);
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

    fader = Synth(\ns_stripFader,[\inBus, stripBus,\outBus,outBus],faderGroup)
  }

  addInSynth {
    inSynth = Synth(\ns_stripIn,[\inBus,stripBus,\outBus,stripBus],inGroup)
  }

  removeInSynth { inSynth.free }

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
