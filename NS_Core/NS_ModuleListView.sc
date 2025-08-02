NS_ModuleListView : NS_Widget {

    *new { |nsControl|
        ^super.new.init(nsControl)
    }

    init { |nsControl|
        var folderNames, moduleFolders, moduleStack;
        var folderDict = ();
        var path = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "/NS_Modules/";
        PathName(path).folders.do({ |entry| 
            folderDict.put(entry.folderName.asSymbol, [])
        }); 
        PathName(path).filesDo({ |file| 
            if(file.extension == "sc",{
                folderDict.keysDo({ |key|
                    if(file.allFolders.collect(_.asSymbol).includes(key),{
                        folderDict[key] = folderDict[key].add(
                            file.fileNameWithoutExtension.split($_)[1]
                        )
                    })
                })
            })
        });

        folderNames = folderDict.keys.asArray.sort;

        moduleStack = StackLayout(
            *folderNames.collect({ |folderKey|
                var modArray = folderDict[folderKey];
                ListView()
                .font_( Font(*NS_Style('bigFont')) )
                .stringColor_(NS_Style('textLight'))
                .selectedStringColor_(NS_Style('textDark'))
                .hiliteColor_(NS_Style('highlight'))
                .background_(NS_Style('transparent'))
                .items_(modArray)
                .action_({ |v| nsControl.value_(modArray[v.value]) })
            })
        );

        moduleFolders =  ListView()
        .fixedWidth_(90)
        .font_( Font(*NS_Style('bigFont')) )
        .stringColor_(NS_Style('textLight'))
        .selectedStringColor_(NS_Style('textDark'))
        .hiliteColor_(NS_Style('highlight'))
        .background_(NS_Style('transparent'))
        .items_(folderNames)
        .action_({ |v| moduleStack.index_(v.value) })
        .valueAction_( folderNames.collect(_.asSymbol).indexOf('main') );

        view = UserView()
        .fixedHeight_(120)
        .fixedWidth_(240)
        .background_(NS_Style('bGroundDark'))
        .layout_(
            HLayout( moduleFolders, moduleStack )
        );

        view.layout.spacing_(NS_Style('viewSpacing')).margins_(NS_Style('viewMargins'));
    }
}
