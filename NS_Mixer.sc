NS_Mixer {

  var mixerGroup, inGroup, insertGroup, preGroup, postGroup;

  *new { |server|
    ^super.new.init(server)
  }

  init { |server|
    mixerGroup = Group(server);
    inGroup = Group.head(mixerGroup);

  }


  addInSynth {}
  

}


NS_MixerStrip {
   
  *new {
    ^super.new.init
  }

  init {

  }

  addInsert {}
  addPreSend {}
  addPostSend {}
}
