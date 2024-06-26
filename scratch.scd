
(
var bounds = Window.availableBounds;
var win = Window("NSFW",);
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
NSFW.servers['nsfw_0'].window.swapGrid.controls

NSFW.servers['nsfw_0'].options.blockSize
NSFW.servers['nsfw_0'].server
NSFW.servers['nsfw_0'].outMixer[0].inSynthGate_(1)
NSFW.servers['nsfw_0'].strips[0][0].inSynthGate
NSFW.servers['nsfw_0'].strips[0][0].stripBus
NSFW.servers['nsfw_0'].server.plotTree
NSFW.servers['nsfw_0'].server.freqscope
NSFW.servers['nsfw_0'].server.meter
NSFW.servers['nsfw_0'].strips[0][0].paused
NSFW.servers['nsfw_0'].server.avgCPU
NSFW.servers['nsfw_0'].window.header.stereoInArray[1].inputBus

Window
NSFW.numInChans

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

n = NSFW(OpenStageControl,[64])
NS_ServerHub.makeWindow
NS_ModuleList().view.front

NSFW.numInChans
n = NS_Test()
n.free
EZScroller(nil,nil, 8,4)
ScrollView

