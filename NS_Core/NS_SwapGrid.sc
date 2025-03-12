NS_SwapGrid : NS_ControlModule {
    classvar numPages = 6;
    var <view;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|

        this.initControlArrays(4);

        4.do({ |stripIndex|
            controls[stripIndex] = NS_Control(nil, ControlSpec(0,numPages-1,'lin',1),0)
            .addAction(\switch,{ |c|
                var pageIndex = c.value;
                // update controllers
                NSFW.controllers.do({ |ctrl| ctrl.switchStripPage(pageIndex, stripIndex) });

                // check what happens when selected a strip/page that is already active;
                // it will turn off and on very quickly (right?), is that a problem?
                defer {
                    numPages.do({ |page| 
                        nsServer.strips[page][stripIndex].do({ |strp| 
                            strp.pause.highlight( false )
                        }) 
                    });
                    nsServer.strips[pageIndex][stripIndex].unpause.highlight( true );
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

        view.layout.spacing_(NS_Style.stripSpacing).margins_(NS_Style.stripMargins);
    }

    asView { ^view }
}
