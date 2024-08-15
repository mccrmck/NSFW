
(
var bounds = Window.availableBounds;
var win = Window("NS_ServerHub",);
var numFaders = 22;
var nb;

win.layout_(
    VLayout(
         nb = NumberBox().align_(\center),
        ~test = MultiSliderView().size_(numFaders).elasticMode_(1).reference_(0.5!14).thumbSize_(20).action_({|ms| nb.value = ms.currentvalue })
    ).spacing_(0).margins_(0)
);
win.front;
)
~test.valueAction_({0.4.rand}!20)


n = NSFW(OpenStageControl,[64])


NS_ServerHub.servers['nsfw_0'].server
NS_ServerHub.servers['nsfw_0'].strips[0][0].stripBus
NS_ServerHub.servers['nsfw_0'].server.plotTree
NS_ServerHub.servers['nsfw_0'].server.scope
NS_ServerHub.servers['nsfw_0'].server.freqscope
NS_ServerHub.servers['nsfw_0'].server.meter
NS_ServerHub.servers['nsfw_0'].server.peakCPU
NS_ServerHub.servers['nsfw_0'].server.avgCPU
a = NS_ServerHub.servers['nsfw_0'].server.numUGens

g = Group()
NS_InChannelStrip(0).view.front


// repper switch should become three (bigger) buttons, get rid of trigger button
// dust/impulse trigger for repper? also -> fix repper amp/mix...
// must fix the outChannelStrip ModuleSinks!


