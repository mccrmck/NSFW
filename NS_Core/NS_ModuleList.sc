NS_ModuleList {
    classvar <view;

    *new {
        ^super.new.init
    }

    init {
        var path = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "NS_Modules/";
        var folderNames = PathName(path).folders.collect({ |entry| entry.folderName });
        var moduleArrays = folderNames.collect({ |folder|
            PathName(path +/+ folder).entries.collect({ |entry| 
                if(entry.isFile,{ entry.fileNameWithoutExtension.split($_)[1] })
            })
        });
        var stack = StackLayout(
            *moduleArrays.collect({ |modArray|
                ListView()
                .items_(modArray)
                .background_(Color.clear)
                .stringColor_(Color.white)
                .beginDragAction_({ |v| modArray[v.value] })
            })
        );

        view = View().layout_(
            HLayout(
                ListView()
                .items_(folderNames)
                .background_(Color.clear)
                .stringColor_(Color.white)
                .action_({ |v| stack.index_(v.value) })
                .valueAction_( folderNames.collect(_.asSymbol).indexOf('main') ),
                stack
            )
        );

        view.layout.spacing_(0).margins_(0);
    }

    asView { ^view }
}
