NS_Module {
    var <>modGroup;
    var <>synths, <>controls, <inBus;
    var paused = false;
    var <>win;

  *new { |group|
    ^super.newCopyArgs(group).init
  }

  
  // most synths will process mono input, some will process stereo, no?
  // can the mixer know what is needed an then send the right amount of channels to the right place?


  pause {
      synths.do({ |synth| synth.set(\pauseGate, 0) });
      modGroup.run(false);
      paused = true;
  }

  unpause {
      synths.do({ |synth| synth.set(\pauseGate, 1); synth.run(true);});
      modGroup.run(true);
      paused = false;
  }

}
