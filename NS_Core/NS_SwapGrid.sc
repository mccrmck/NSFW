NS_SwapGrid : NS_ControlModule {
    var <view;
    var <currentActiveStrips;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|

        this.initControlArrays(4);

        currentActiveStrips = [0,0,0,0];

        4.do({ |stripIndex|
            controls.add(
                NS_Switch(""!6,{ |switch|
                    var pageIndex = switch.value;
                    var oldStrip = currentActiveStrips[stripIndex];

                    nsServer.strips[oldStrip][stripIndex].pause;
                    nsServer.strips[pageIndex][stripIndex].unpause;
                    currentActiveStrips[stripIndex] = pageIndex;

                    // update controllers
                    NSFW.controllers.do({ |ctrl| ctrl.switchStripPage(pageIndex, stripIndex) });

                    // change colour...make modules visible?
                    defer {
                        nsServer.strips[oldStrip][stripIndex].asView.background_ ( Color.clear );
                        nsServer.strips[pageIndex][stripIndex].asView.background_( /*Color.fromHexString("#fcb314")*/ Color.white.alpha_(0.5) );
                    }

                }).maxWidth_(90)
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
