NSFW {
    classvar <controllers;
    classvar <>numInChans = 2, <>numOutChans = 2;
    var win;

    *new { |controllers, blockSizeArray|
        ^super.new.init(controllers.asArray, blockSizeArray.asArray)
    }

    init { |controllersArray, blockSizes|

        var gradient = Color.rand; /*Color.fromHexString("#7b14ba")*/
        var options  = Server.local.options;

        controllers = controllersArray;

        win = Window("NSFW").layout_(
            VLayout(
                GridLayout.rows(
                    [
                        StaticText().string_( "inDevice:" ).stringColor_( Color.white ),
                        PopUpMenu().items_( ServerOptions.inDevices ).action_({ |menu|
                            var device = menu.item.asString;
                            options.inDevice = device;
                            "inDevice: %\n".format(device).postln
                        })
                    ],
                    [
                        StaticText().string_( "outDevice:" ).stringColor_( Color.white ),
                        PopUpMenu().items_( ServerOptions.outDevices ).action_({ |menu|
                            var device = menu.item.asString;
                            options.outDevice = device;
                            "outDevice: %\n".format(device).postln
                        })
                    ],
                    [
                        StaticText().string_( "numInputChannels:" ).stringColor_( Color.white ),
                        PopUpMenu().items_([2,4,6,8]).action_({ |menu|
                            var chans = menu.item.asInteger;
                            options.numInputBusChannels = chans;
                            numInChans = chans;
                            "numInBusses: %\n".format(chans).postln
                        })
                        .value_(0)
                    ],
                    [
                        StaticText().string_( "numOutputChannels:" ).stringColor_( Color.white ),
                        PopUpMenu().items_([2,4,8,12,16,24]).action_({ |menu|
                            var chans = menu.item.asInteger;
                            options.numOutputBusChannels = chans.max(8);
                            numOutChans = chans;
                            "numOutBusses: %\n".format(chans).postln
                        })
                        .value_(0)
                    ],
                    [
                        StaticText().string_("sampleRate:").stringColor_(Color.white),
                        PopUpMenu().items_(["44100","48000","88200", "96000"]).value_(1).action_({ |menu|
                            var sRate = menu.item.asInteger;
                            options.sampleRate = sRate;
                            "sampleRate: %\n".format(sRate).postln;
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
