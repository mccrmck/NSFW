NS_SwapGrid : NS_ControlModule {

    *new { |nsServer|
        ^super.new.buildGrid(nsServer)
    }

    buildGrid { |nsServer|
        var numPages  = NS_Server.numPages;
        var numStrips = NS_Server.numStrips;

        numStrips.do({ |stripIndex|
            controls.add( 
                NS_Control(stripIndex, ControlSpec(0, numPages - 1, 'lin', 1), 0)
                .addAction(\switch,{ |c|
                    var pageIndex = c.value;
                    // update controllers
                    NS_Controller.allActive.do({ |ctrl|
                        ctrl.switchStripPage(pageIndex, stripIndex)
                    });

                    // when selecting a strip/page that is already active,
                    // will it pause/unpause? is that an audible problem?
                    defer {
                        numPages.do({ |page| 
                            nsServer.strips[page][stripIndex].do({ |strp| 
                                strp.pause;
                            }) 
                        });
                        nsServer.strips[pageIndex][stripIndex].unpause;
                        // change this now that the strips are ordered differently
                        try { 
                            nsServer.window.stripViews.deepDo(2,{ |strip| strip.refresh })
                        }
                    }
                })
            )
        })
    }
}
