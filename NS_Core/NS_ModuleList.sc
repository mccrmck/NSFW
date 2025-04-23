NS_ModuleList {
    classvar instance;
    classvar win;

    *new {
        ^instance ?? { ^super.new.init }
    }

    init {
        ^instance = this.class
    }

    *drawWindow {
        var folderNames, moduleFolders, moduleStack;
        var gradient = Color.rand;
        var folderDict = ();
        var path = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "/NS_Modules/";
        PathName(path).folders.do({ |entry| folderDict.put(entry.folderName.asSymbol, []) }); 
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
                .font_( Font(*NS_Style.bigFont) )
                .stringColor_(NS_Style.textLight)
                .selectedStringColor_(NS_Style.textDark)
                .hiliteColor_(NS_Style.highlight)
                .background_(NS_Style.transparent)
                .items_(modArray)
                .beginDragAction_({ |v| modArray[v.value] })
                .mouseMoveAction_({ |v| v.beginDrag })
            })
        );

        moduleFolders =  ListView()
        .font_( Font(*NS_Style.bigFont) )
        .stringColor_(NS_Style.textLight)
        .selectedStringColor_(NS_Style.textDark)
        .hiliteColor_(NS_Style.highlight)
        .background_(NS_Style.transparent)
        .items_(folderNames)
        .action_({ |v| moduleStack.index_(v.value) })
        .valueAction_( folderNames.collect(_.asSymbol).indexOf('main') );

        win = Window(
            "NSFW: Modules",
            (200@200).asRect.center_(Window.availableBounds.center)
        ).drawFunc_({
            var vBounds = win.view.bounds;

            Pen.addRect(vBounds);
            Pen.fillAxialGradient(
                vBounds.leftTop, vBounds.rightBottom, 
                NS_Style.bGroundDark, gradient
            );
        }).layout_(
            HLayout( moduleFolders, moduleStack )
        );

        win.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins);
        win.front
    }

    *toggleVisible {
        win ?? { ^this.drawWindow };
        win.visible ?? { ^this.drawWindow };
        win.visible_( win.visible.not );
    }
}
