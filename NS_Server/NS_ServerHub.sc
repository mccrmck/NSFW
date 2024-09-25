NS_ServerHub {
    classvar <servers;
    classvar <inModules;
    classvar <inChannelView, scroller;
    classvar win;
    classvar buttons, inModuleViews, inView;

    *initClass {
        servers = Dictionary();
    }

    *boot { |blockSizeArray = #[64]|
        var cond = CondVar();
        fork{
            blockSizeArray.do({ |blockSize, index|
                var name = ("nsfw_%".format(index) ).asSymbol;
                servers.put(name, NS_Server(name,blockSize,{cond.signalOne}));
                cond.wait({ servers[name].options.blockSize == blockSize });
            });
            cond.wait{ servers['nsfw_0'].inGroup.notNil };
            { this.makeWindow }.defer;
        };
    }

    *makeWindow {
        var numIns = NSFW.numInChans;
        var visModuleIndex = 0;
        var bounds = Window.availableBounds;
        var gradient = Color.rand;

        inChannelView = View().layout_(
            HLayout(
                *numIns.collect({ |i| 
                    // does not work with multiple servers yet
                    NS_InChannelStrip(servers['nsfw_0'].inGroup, i)     
                })
            ).spacing_(4).margins_(0)
        );

        buttons = View()
        .layout_(
            VLayout(
                VLayout(
                    Button()
                    .states_([["show module list"],["show module list"]])
                    .action_({ |but| 
                        if(but.value == 0,{ NS_ModuleList.close },{ NS_ModuleList.open })
                    }),
                    Button()
                    .states_([["save session"]])
                    .action_({ |but|
                        Dialog.savePanel(
                            { |path| this.save( path ) },
                            nil, 
                            PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "saved/sessions/"
                        )
                    }),
                    Button()
                    .states_([["load session"]])
                    .action_({ |but|
                        FileDialog(
                            { |path| this.load( path ) },
                            fileMode: 2, acceptMode: 0, 
                            path: PathName(NSFW.filenameSymbol.asString).pathOnly +/+ "saved/sessions" 
                        )
                    }),
                    Button().states_([["recording maybe?"]]),
                ),
                VLayout(
                    NS_Switch(["nsfw_0","nsfw_1","active","servers"],{ |switch| }), 
                    NS_AssignButton()
                ),
                VLayout( 
                    NS_Switch(numIns.collect({ |i| i.asString }),{ |switch| 
                        inChannelView.children.do({ |v| v.visible_(false) });
                        inChannelView.children[switch.value].visible_(true)  
                    }).valueAction_(0),
                    NS_AssignButton()
                ),

            ).margins_(0)
        );

        win = Window("NSFW Server Hub",Rect(bounds.width-480,bounds.height-315,480,315));
        win.layout_(
            HLayout(
                buttons,
                inChannelView
            )
        );

        win.layout.spacing_(4).margins_(4);

        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        win.view.fixedWidth_(330).fixedHeight_(360);

        //win.alwaysOnTop_(true);
        win.front;

        win.onClose_({
            // free all modules
            // free all servers 
            // close all windows
            // thisProcess.recompile
        })
    }

    *save { |path| }

    *load { |path| }

}
