NS_OpenStageControl {
  classvar ip, port;

  *new { |ip = "localhost", port = 8080|
    ^super.new.init(ip, port)
  }

  init { |ip, port|
    var unixString = "open /Applications/open-stage-control.app --args " ++
    "--send %:% ".format(ip, NetAddr.localAddr.port ) ++
    "--load '%'".format( "NSFW.json".resolveRelative );

    unixString.unixCmd;
  }
}

