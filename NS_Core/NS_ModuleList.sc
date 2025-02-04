NS_ModuleList {
    classvar <view, control;

    *new {
        ^super.new.init
    }

    init {
        var gradient = Color.rand;
        var path = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "NS_Modules/";
        var folderNames = PathName(path).folders.collect({ |entry| entry.folderName });
        var moduleArrays = folderNames.collect({ |folder|
            PathName(path +/+ folder).entries.collect({ |entry| 
                if(entry.isFile,{
                    entry.fileNameWithoutExtension.split($_)[1];
                })
            })
        });
        var views = moduleArrays.collect({ |modArray|
            ListView()
            .items_(modArray)
            .background_(Color.clear)
            .stringColor_(Color.white)
            .beginDragAction_({ |view, x, y|
                var index = view.value;
                modArray[index]
            })
        });
        var stack = StackLayout( *views ).index_( folderNames.collect(_.asSymbol).indexOf('main') );

        view = View().layout_(
            HLayout(
                ToolBar(
                    *folderNames.collect({ |name,index|
                        MenuAction(name,{ stack.index_(index) })
                    })
                ).orientation_('vertical'),
                stack
            )
        );

        view.layout.spacing_(0).margins_(0);
    }

    asView { ^view }
}
