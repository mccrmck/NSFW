
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

NS_ServerHub.servers['nsfw_0'].options.blockSize
NS_ServerHub.servers['nsfw_0'].server
NS_ServerHub.servers['nsfw_0'].outMixer[0].inSynthGate_(1t
NS_ServerHub.servers['nsfw_0'].strips[0][0]
NS_ServerHub.servers['nsfw_0'].strips[0][0].stripBus
NS_ServerHub.servers['nsfw_0'].server.plotTree
NS_ServerHub.servers['nsfw_1'].server.plotTree
NS_ServerHub.servers['nsfw_0'].server.freqscope
NS_ServerHub.servers['nsfw_0'].server.meter
NS_ServerHub.servers['nsfw_0'].strips[0][0].paused
NS_ServerHub.servers['nsfw_0'].server.avgCPU
NS_ServerHub.servers['nsfw_0'].window.header.stereoInArray[1].inputBus

Window
NS_ServerHub.numInChans

r = NS_RingMod()
r.makeOSCFragment("lpg")
r.free

OSCFunc.trace(false,false)

Class

MultiSliderView

OSC_ModuleFragment(true,[
    OSC_MultiFader(nil,nil,false,false,128),
    OSC_MultiFader(nil,nil,true,false,128),
    OSC_MultiFader(nil,nil,false,true,128),
    OSC_MultiFader(nil,nil,true,true,128)
]
).write("mfTest")

OSCFunc.trace(false)

NS_ServerHub.makeWindow
NS_InputModule().view.front
NS_ModuleList.open

NS_ServerHub.numInChans
n = NS_StripRecPB()
n.free
EZScroller(nil,nil, 8,4)
ScrollView
a = DragSink().front
a.object
