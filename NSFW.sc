NSFW {
  classvar <servers, <controllers;

  *initClass {
    servers = Dictionary();
  }
  

  *new { |controllers, numServers = 1|
    ^super.new.init(controllers.asArray, numServers)
  }

  init { |controllersArray, numServers|

    numServers.do({ |index|
      var name = ("nsfw_" ++ index).asSymbol;
      servers.put(name, NS_Server(name))
    });
  

    controllers = controllersArray;

  }
  
  
}
