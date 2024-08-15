NS_Out {

  *new { |sig, numChans = 2, bus, mix, thru|
    ^super.new.init(sig, numChans, bus, mix, thru)
  }

  init { |sig, numChans, bus, mix, thru|
      var out  = XFade2.ar(In.ar(bus,numChans), sig,(mix * thru).linlin(0,1,-1,1).lag(0.01));
      ^ReplaceOut.ar( bus, out )
  }
}
