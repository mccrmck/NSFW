NS_ModuleList {
    classvar <view;

    *new {
        ^super.new.init
    }

    init {
        var folderNames, stack;
        var folderDict = ();
        var path = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "/NS_Modules/";
        PathName(path).folders.do({ |entry| folderDict.put(entry.folderName.asSymbol, []) }); 
        PathName(path).filesDo({ |file| 
            if(file.extension == "sc",{
                folderDict.keysDo({ |key|
                    if(file.allFolders.collect(_.asSymbol).includes(key),{
                        folderDict[key] = folderDict[key].add(file.fileNameWithoutExtension.split($_)[1])
                    })
                })
            })
        });

        folderNames = folderDict.keys.asArray.sort;

        stack = StackLayout(
            *folderNames.collect({ |folderKey|
                var modArray = folderDict[folderKey];
                ListView()
                .font_( Font("Helvetica", 14) )
                .items_(modArray)
                .background_(Color.clear)
                .stringColor_(Color.white)
                .beginDragAction_({ |v| modArray[v.value] })
            })
        );

        view = View().layout_(
            HLayout(
                ListView()
                .font_( Font("Helvetica",14) )
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
