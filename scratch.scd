
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

Env([0,1,0],[0.01,0.49],\wel).plot

Env.shapeNumber(\wel)

NS_ServerHub.servers['nsfw_0'].server
NS_ServerHub.servers['nsfw_0'].strips[0][0].stripBus
NS_ServerHub.servers['nsfw_0'].server.plotTree
NS_ServerHub.servers['nsfw_0'].server.scope
NS_ServerHub.servers['nsfw_0'].server.freqscope
NS_ServerHub.servers['nsfw_0'].server.meter
NS_ServerHub.servers['nsfw_0'].server.peakCPU


// on/off buttons for amp mod, ring mod, combfilter
// repper switch should become three (bigger) buttons, get rid of trigger button
// dust/impulse trigger for repper? also -> fix repper amp/mix...
// check spatialization on repper, maybe it should move?


OpenStageControl.netAddr.sendMsg("/EDIT","panel_12","{\"widgets\": %}".format([OSC_XY().oscString]))

NSFW()
NSFW.numOutChans


NS_ServerHub.makeWindow;
NS_ModuleList.open;

0.8.rand2.fold(0,1).linlin(0,1,-1.1,1.1).clip2
g = Group()
n = NS_PolandFB(g)
n.free
s.meter

// explore rolling on my fingertips on the glass to get interesting micro-variations! Is a nice technique that I think would work well with MLP synths
