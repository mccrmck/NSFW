NSFW {
    classvar <servers;

    *initClass {
        servers = Dictionary()
    }

    *new { |nsOptions|
        ^super.new.init(nsOptions)
    }

    init { |options|
        var serverName = ("nsfw_" ++ servers.size).asSymbol;
        var cond = CondVar();
        this.bootServer(serverName, options, cond)
    }

    bootServer { |serverName, serverOptions, cond|

        fork {
            var nsServer = NS_Server(serverName, serverOptions, { cond.signalOne });

            servers.put(serverName, nsServer);

            cond.wait { nsServer.server.serverRunning };
            { NS_ServerWindow(nsServer) }.defer;
        }
    }

    *bootFromSavedFile {}

    *cleanUp {
        Window.closeAll;
        NS_Controller.cleanUpAll;
        thisProcess.recompile
    }
}
