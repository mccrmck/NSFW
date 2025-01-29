NSFW {
    classvar <controllers;
    classvar <>numInBusses = 2, <>numChans = 2, <>numOutBusses = 8;
    var win;

    *new { |controllers, blockSizeArray|
        ^super.new.init(controllers.asArray, blockSizeArray.asArray)
    }

    init { |controllersArray, blockSizes|

        var gradient = Color.rand;
        var options  = Server.local.options;
        options.numInputBusChannels = 2;
        options.numOutputBusChannels = 8;

        controllers = controllersArray;

        win = Window("NSFW").layout_(
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
                    StaticText().string_( "numInputBusses:" ).stringColor_( Color.white ),
                    PopUpMenu().items_([2,4,6,8]).action_({ |menu|
                        var chans = menu.item.asInteger;
                        options.numInputBusChannels = chans;
                        numInBusses = chans;
                        "numInBusses: %\n".format(chans).postln
                    })
                    .value_(0)
                ],
                [
                    StaticText().string_( "numInternalChannels:" ).stringColor_( Color.white ),
                    PopUpMenu().items_([2,4,8,12,16,24]).action_({ |menu|
                        var chans = menu.item.asInteger;
                        options.numOutputBusChannels = chans.max(8);
                        numChans = chans;
                        "numChannels: %\n".format(chans).postln
                    })
                    .value_(0)
                ],
                [
                    StaticText().string_( "numOutputBusses:" ).stringColor_( Color.white ),
                    TextField().string_("press ENTER").action_({ |tField|
                        var chans = tField.value.asInteger;
                        options.numOutputBusChannels = chans;
                        numOutBusses = chans;
                        "numOutBusses: %\n".format(chans).postln
                    })
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
        );

        win.drawFunc = {
            var view = win.view;
            Pen.addRect(view.bounds);
            Pen.fillAxialGradient(view.bounds.leftTop, view.bounds.rightBottom, Color.black, gradient);
        };

        win.layout.spacing_(4).margins_(4);
        win.setInnerExtent(240,90);
        win.front
    }
}
