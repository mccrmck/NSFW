NS_SwapGrid : NS_ControlModule {  // consider NS_MatrixSwapGrid
    var <view;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var numPages = nsServer.numPages;
        
        this.initControlArrays(4);

        4.do({ |stripIndex|
            controls[stripIndex] = NS_Control(
                stripIndex, 
                ControlSpec(0,numPages - 1,'lin',1),
                0
            )
            .addAction(\switch,{ |c|
                var pageIndex = c.value;
                // update controllers
               // NSFW.controllers.do({ |ctrl| ctrl.switchStripPage(pageIndex, stripIndex) });

                // check what happens when selected a strip/page that is already active;
                // it will turn off and on very quickly (right?), is that a problem?
                defer {
                    numPages.do({ |page| 
                        nsServer.strips[page][stripIndex].do({ |strp| 
                            strp.pause;
                            nsServer.strips[page][stripIndex].view.highlight(false)
                        }) 
                    });
                    nsServer.strips[pageIndex][stripIndex].unpause;
                    nsServer.strips[pageIndex][stripIndex].view.highlight(true)
                }
            });
            assignButtons[stripIndex] = NS_AssignButton(this, stripIndex, \switch)
        });

        view = View().maxWidth_(240).layout_(
            HLayout(
                *4.collect({ |stripIndex|
                    VLayout(
                        NS_ControlSwitch(controls[stripIndex],""!numPages),
                        assignButtons[stripIndex]
                    )
                })
            )
        );

        view.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins);
    }

    asView { ^view }
}
