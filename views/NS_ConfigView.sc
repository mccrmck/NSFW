NS_ConfigView : SCViewHolder {

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var savePath = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "saved/servers/";

       var saveButton = NS_Button([
            ["save", NS_Style('textLight'), NS_Style('bGroundDark')]
        ])
        .addLeftClickAction({
            Dialog.savePanel(
                { |path| 
                    nsServer.save.writeArchive(path);
                    "% saved to: %".format(nsServer.name, path).postln;
                }, 
                nil,
                savePath
            )
        });

       var loadButton = NS_Button([
            ["load", NS_Style('textLight'), NS_Style('bGroundDark')]
        ])
        .addLeftClickAction({
            Dialog.openPanel(
                { |path| nsServer.load(Object.readArchive(path)) }, 
                nil, false, savePath
            )
        });

        var configButton = NS_Button(["config"]);

        var controllerButton = NS_Button(["controllers"]);

        view = NS_ContainerView().layout_(
            VLayout(
                saveButton, 
                loadButton, 
                configButton,
                controllerButton
            ).nsMarginsSpacing('view')
        )
    }
}
