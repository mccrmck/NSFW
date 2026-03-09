NSFW {
    classvar instance;
    classvar win;
    classvar serverList, serverStackArray, serverStack;
    classvar serverListView, <serverStackView;
    classvar <servers;

    classvar hubStack;

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

    *cleanup {
        Window.closeAll;
        NS_Controller.cleanupAll;
        thisProcess.recompile
    }
}
