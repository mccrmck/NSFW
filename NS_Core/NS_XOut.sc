NS_XOut {

  *new { |bus, sig, mix, thru|
    ^super.new.init(bus, sig, mix, thru)
  }

  init { |bus, sig, mix, thru|
    ^ReplaceOut.ar( bus, XFade2.ar(In.ar(bus,2), sig,(mix * thru).linlin(0,1,-1,1)) )
  }
}
