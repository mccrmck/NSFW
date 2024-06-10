
(
var bounds = Window.availableBounds;
var win = Window("NSFW",Rect(0,0,200,200));

win.layout_(
    HLayout(
        NS_Knob(win,"tost",\db,{|k| k.value.postln },true, -18),
        //NS_Switch(win,(0..4),{|b| b.value.postln},\horz),
        ~test = NS_Fader(win,"test",\amp,{|b,i| [b,i].postln}),
    )
);
win.front;
)


n = NSFW(NS_OpenStageControl(), 1)
n.controllers
NSFW.servers['nsfw_0'].outMixer
NSFW.servers['nsfw_0'].strips[0][0].inModule
NSFW.servers['nsfw_0'].outMixer[0].moduleArray
NSFW.servers['nsfw_0'].strips[0][0].sends
NSFW.servers['nsfw_0'].server.plotTree
NSFW.servers['nsfw_0'].server.meter
NSFW.servers['nsfw_0'].server.scope
NSFW.servers['nsfw_0'].server.avgCPU

Server.killAll

r = NS_RingMod()
r.oscFuncs[1]
r.controls[1].spec.map(0.9)
r.controls[1].value_(255.15)
r.assignButtons
r.free
f = NS_Freeze()
f.free
a = NS_Test()
a.controls[1].spec.asArray[0]
a.free

Server.all['localhost']

g = NSFW.servers['nsfw_0'].inGroup.server.name
NSFW.servers[g]
OSCFunc.trace(true,true)

NS_Transceiver.listenForOSC(true,RingMod(),)

n = NS_ChannelStrip()


NSFW.servers['nsfw_0'].window.swapGrid.currentActiveStrips
4.wrap(0,4)

g = Group()
n = View().layout_(
    HLayout(
        *1.collect({
            View().layout_(
                HLayout(
                    NS_ChannelStrip(g,0),
                    NS_ChannelStrip(g,0),
                    NS_ChannelStrip(g,0),
                    NS_OutChannelStrip(g,0),
                ).spacing_(0).margins_([4,0])
            )
        })
    )
)
n.maxWidth_(900)
g.server
s.serverRunning
n.front
s.plotTree

a = Array.newClear(4)
a[0] = 4
a.put
