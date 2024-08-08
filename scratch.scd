
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

"for" ++ "_test"

    
n = NSFW(OpenStageControl,[64])
NSFW.controllers

OpenStageControl.stripWidgets[0][0][0]

"%".format([2,3])
NS_ServerHub.servers['nsfw_0'].server
NS_ServerHub.servers['nsfw_0'].strips[0][0]
NS_ServerHub.servers['nsfw_0'].server.plotTree
NS_ServerHub.servers['nsfw_0'].server.scope
NS_ServerHub.servers['nsfw_0'].server.freqscope
NS_ServerHub.servers['nsfw_0'].server.meter
NS_ServerHub.servers['nsfw_0'].server.peakCPU
NS_ServerHub.servers['nsfw_0'].strips[0][0].moduleArray[3].assignButtons[4].value_(1)


//on/off buttons for amp mod, ring mod, combfilter

g = Group()
r = NS_Repper(g,0)
r.makeOSCFragment("lpg")
r.free

"%,%".format(*["test","tost"]).postln;
a ="%".ccatList("%"!2).format(*["test","tost"]).postln

"%".ccatArgs(*["test","tost","stress"]).postln

// load is now printing ["string","string"]


OpenStageControl.netAddr.sendMsg("/EDIT","panel_12","{\"widgets\": %}".format([OSC_XY().oscString]))

String

c = ControlSpec(-9,9,\db)
c.unmap(9)

OSCFunc.trace(false)

s.meter
OSC_ModuleFragment(true,[
    OSC_MultiFader(nil,nil,false,false,128),
    OSC_MultiFader(nil,nil,true,false,128),
    OSC_MultiFader(nil,nil,false,true,128),
    OSC_MultiFader(nil,nil,true,true,128)
]
).write("mfTest")




NS_ServerHub.makeWindow;
NS_ModuleList.open;

OpenStageControl.boot;
OpenStageControl.addModuleFragment(0,0,2,NS_SpecDelay)
OpenStageControl.removeModuleFragment(0,0,1)
OpenStageControl.switchStripPage(0,0)

n = NS_LPG()

.removeAll
OSCFunc.trace(false)

s.avgCPU
s.meter
s.plotTree
s.freqscope;

564/7

n = NetAddr("localhost",8080)
NetAddr.localAddr
n.sendMsg('/EDIT/GET',"127.0.0.1: 57120", "panel_1" )
n.sendMsg('/SET', "panel_1", 0 )
n.sendMsg('/SESSION/SAVE', "~/Desktop/bread.json" )


Dialog.openPanel(path:"~/Desktop/" )

c = OSC_Fader()

OSCFunc.trace()

n = NS_ModuleSink()
n.modSink.string_("in").align_(\center)
n.modSink.object
n.view.front


g = Group()
n = NS_PolandFB(g)
n.free
s.meter



// explore rolling on my fingertips on the glass to get interesting micro-variations! Is a nice technique that I think would work well with MLP synths

