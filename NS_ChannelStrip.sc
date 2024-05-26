NS_ChannelStrip {
  var <server, <outBus;
  var <stripBus;
  var <stripGroup, <inGroup, <slots, <faderGroup;
  var <fader;

  *initClass {
    StartUp.add{
      SynthDef(\ns_stripFader,{
        var sig = In.ar(\inBus.kr, 2);
        var mute = 1 - \mute.kr(0); 
        sig = ReplaceBadValues.ar(sig);
        sig = sig * mute * \amp.kr(0);
        sig = sig * Env.asr().ar(2,\gate.kr(1));

        Out.ar(\outBus.kr, sig)
      }).add
    }
  }

  *new { |server, outBus, numSlots = 5| 
    ^super.newCopyArgs(server, outBus).init(numSlots)
  }

  init { |numSlots|

    server     = server ? Server.default;
    stripBus   = Bus.audio(server,2);

    stripGroup = Group(server,\addToTail);
    inGroup    = Group(stripGroup,\addToTail);
    slots      = numSlots.collect({ |i| Group(stripGroup,\addToTail) });
    faderGroup = Group(stripGroup,\addToTail);

    fader = Synth(\ns_stripFader,[\inBus, stripBus,\outBus,outBus],faderGroup)
  }

  addInputModule { |inBus|
    NS_Input(inGroup,stripBus,inBus)
  }

  removeInputModule {
    
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

}
