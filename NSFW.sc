NSFW {
    classvar instance;
    classvar win;
    classvar serverList, serverStackArray, serverStack;
    classvar serverListView, <serverStackView;
    classvar <servers;

    *initClass {
        servers = Dictionary()
    }

    *new { |nsOptions|
        ^super.new.init(nsOptions)
    }

    init { |options|
        var serverName = ("nsfw_" ++ servers.size).asSymbol;
        this.bootServer(serverName, options)
    }

    bootServer { |serverName, serverOptions|
        var cond = CondVar();

        fork {
            var nsServer = NS_Server(serverName, serverOptions, { cond.signalOne });

            servers.put(serverName, nsServer);

            cond.wait { nsServer.server.serverRunning };
            { NS_ServerWindow(nsServer).win.visible_(true) }.defer;
        }
    }

    *bootFromSavedFile {}

    *cleanUp {
        Window.closeAll;
        NS_Controller.cleanUpAll;
        thisProcess.recompile
    }
}
