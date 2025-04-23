NS_MatrixSwapGrid : NS_ControlModule {

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|
        var numPages  = NS_MatrixServer.numPages;
        var numStrips = NS_MatrixServer.numStrips;

        this.initControlArray(numStrips);

        numStrips.do({ |stripIndex|
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
                            //   window.stripViews[page][stripIndex].highlight(false)
                        }) 
                    });
                    nsServer.strips[pageIndex][stripIndex].unpause;
                    //   window.stripViews[pageIndex][stripIndex].highlight(true)
                }
            });
        });
    }
}
