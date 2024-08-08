NSFW {
    classvar <controllers;
    classvar <>numInChans = 4;
    var win;

    *new { |controllers, blockSizeArray|
        ^super.new.init(controllers.asArray, blockSizeArray.asArray)
    }

    init { |controllersArray, blockSizes|

        var gradient = Color.rand; /*Color.fromHexString("#7b14ba")*/
        var options  = Server.local.options;

        controllers = controllersArray;

        win = Window("NSFW",).layout_(
            VLayout(
                GridLayout.rows(
                    [
                        StaticText().string_( "inDevice:" ).stringColor_( Color.white ),
                        PopUpMenu().items_( ServerOptions.inDevices ).action_({ |menu|
                            var device = menu.item.asString;
                            options.inDevice = device;
                            "inDevice: %".format(device).postln
                        })
                    ],
                    [
                        StaticText().string_( "outDevice:" ).stringColor_( Color.white ),
                        PopUpMenu().items_( ServerOptions.outDevices ).action_({ |menu|
                            var device = menu.item.asString;
                            options.outDevice = device;
                            "outDevice: %".format(device).postln
                        })
                    ],
                    [
                        StaticText().string_( "numInputChannels:" ).stringColor_( Color.white ),
                        PopUpMenu().items_([2,4,8]).value_(1).action_({ |menu|
                            var chans = menu.item.asInteger;
                            options.numInputBusChannels = chans;
                            numInChans = chans;
                        })
                    ],
                    [
                        StaticText().string_( "numOutputChannels:" ).stringColor_( Color.white ),
                        PopUpMenu().items_([2,4,8,16,24]).value_(0).action_({ |menu|
                            options.numOutputBusChannels = menu.item.asInteger
                        })
                    ],
                    [
                        StaticText().string_("sampleRate:").stringColor_(Color.white),
                        PopUpMenu().items_(["44100","48000","88200", "96000"]).value_(1).action_({ |menu|
                            options.sampleRate = menu.item.asInteger
                        })
                    ],
                    [[
                        Button()
                        .states_([["boot"]])
                        .action_({
                            NS_ServerHub.boot(blockSizes);

                            controllersArray.collect({ |ctrl| ctrl.boot });

                            win.close
                        }),
                        columns: 2
                    ]]
                )
            )
        );

        win.drawFunc = {
            Pen.addRect(win.view.bounds);
            Pen.fillAxialGradient(win.view.bounds.leftTop, win.view.bounds.rightBottom, Color.black, gradient);
        };

        win.layout.spacing_(4).margins_(4);
        win.setInnerExtent(240,90);
        win.front
    }
}
