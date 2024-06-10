NS_OpenStageControl {
  classvar ip, port;

  *new { |ip = "localhost", port = 8080|
    ^super.new.init(ip, port)
  }

  init { |ipIn, portIn|
    var unixString = "open /Applications/open-stage-control.app --args " ++
    "--send %:% ".format(ipIn, NetAddr.localAddr.port ) ++
    "--load '%'".format( "NSFW.json".resolveRelative );

    ip = ipIn;
    port = portIn;

    unixString.unixCmd;
  }

  updateStrip { |stripIndex|
    NetAddr(ip, port).sendMsg("/strip%".format(stripIndex))
  }
}

