NS_ModuleList {
    classvar <win;

    *open {
        var gradient = Color.rand;
        var path = PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "NS_Modules/";
        var folderNames = PathName(path).folders.collect({ |entry| entry.folderName });
        var modDrags = folderNames.collect({ |folder|
            PathName(path +/+ folder).entries.collect({ |entry| 
                if(entry.isFile,{
                    var module = entry.fileNameWithoutExtension.split($_)[1];

                    DragSource()
                    .background_(Color.white)
                    .object_(module)
                    .dragLabel_(module)
                    .string_(module)
                    .align_(\left)
                })
            })
        });

        var moduleLists = modDrags.collect({ |dragArray|
            View().layout_( 
                GridLayout.rows( 
                    *dragArray.clump(folderNames.size) 
                ).spacing_(2).margins_(0)
            ).visible_(false)
        });

        var switch = NS_Switch(folderNames,{ |switch| 
            var val = switch.value;
            var height = (modDrags[val].size / folderNames.size) * 28;
            moduleLists.do(_.visible_(false));
            moduleLists[val].visible_(true);
            win.setInnerExtent(folderNames.size * 120, height )
        },'horz').buttonsMinWidth_(120).buttonsMaxHeight_(30);

        win = Window("Module List").layout_(
            VLayout(
                switch,
                 HLayout( *moduleLists )
            )
        );

        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        switch.valueAction_( folderNames.collect({ |name| name.asSymbol }).indexOf('main') );
        win.view.layout.spacing_(4).margins_(8);
        win.alwaysOnTop_(true);
        win.front
    }

    asView { ^win.view }

    *close { win.close }
}
