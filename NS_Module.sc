NS_Module {
  classvar synthFunc, controls;
  var group, <synths, ctlBus;

  *new {
    ^super.new.init
  }

  init {}

  makeCtlBus {}

  // most synths will process mono input, some will process stereo, no?
  // can the mixer know what is needed an then send the right amount of channels to the right place?
}

