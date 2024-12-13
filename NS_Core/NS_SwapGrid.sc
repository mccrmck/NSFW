NS_SwapGrid : NS_ControlModule {
    classvar numPages = 6;
    var <view;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|

        this.initControlArrays(4);

        4.do({ |stripIndex|
            controls.add(
                NS_Switch(""!numPages,{ |switch| // six pages
                    var pageIndex = switch.value;

                    // update controllers
                    NSFW.controllers.do({ |ctrl| ctrl.switchStripPage(pageIndex, stripIndex) });

                    // check what happens when selected a strip/page that is already active;
                    // it will turn off and on very quickly (right?), is that a problem?
                    defer {
                        numPages.do({ |page| 
                            nsServer.strips[page][stripIndex].do({ |strp| 
                                strp.pause.asView.background_( Color.clear )
                            }) 
                        });
                        nsServer.strips[pageIndex][stripIndex].unpause.asView.background_( Color.white.alpha_(0.5) );
                    }
                })
            );

            assignButtons[stripIndex] = NS_AssignButton(this, stripIndex, \switch).maxWidth_(45)
        });

        view = View().layout_(
            HLayout(
                *4.collect({ |stripIndex|
                    VLayout(
                        controls[stripIndex],
                        assignButtons[stripIndex]
                    )
                })
            )
        );

        4.do({ |i| controls[i].valueAction_(0) });
        controls.do({ |switch| switch.layout.spacing_(2).margins_(2) });
        view.layout.spacing_(2).margins_([2,0]);
    }

    asView { ^view }
}
