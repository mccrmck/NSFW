NS_SwapGrid : NS_ControlModule {  // consider NS_MatrixSwapGrid
    var <view;

    *new { |window|
        ^super.new.init(window)
    }

    init { |window|
        var numPages = NS_MatrixServer.numPages;
        var nsServer = window.nsServer;
        
        this.initControlArrays(4);

        NS_MatrixServer.numStrips.do({ |stripIndex|
            controls[stripIndex] = NS_Control(stripIndex, ControlSpec(0,numPages - 1,'lin',1), 0)
            .addAction(\switch,{ |c|
                var pageIndex = c.value;
                // update controllers
                NS_Controller.allActive.do({ |ctrl|
                    ctrl.switchStripPage(pageIndex, stripIndex)
                });

                // check what happens when selected a strip/page that is already active;
                // it will turn off and on very quickly (right?), is that a problem?
                defer {
                    numPages.do({ |page| 
                        nsServer.strips[page][stripIndex].do({ |strp| 
                            strp.pause;
                            window.stripViews[page][stripIndex].highlight(false)
                        }) 
                    });
                    nsServer.strips[pageIndex][stripIndex].unpause;
                    window.stripViews[pageIndex][stripIndex].highlight(true)
                }
            });
            assignButtons[stripIndex] = NS_AssignButton(this, stripIndex, \switch)
        });

        view = View().layout_(
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
