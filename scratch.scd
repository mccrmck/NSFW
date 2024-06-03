






s.meter
ControlSpec

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

c = ControlSpec(0,8,\lin).asSpec
c.guessNumberStep

0.01
Float
~test.slider

Spec
NS_RingMod()

i= NS_Input(nil,0,0)
i.free
Window.closeAll

i.synths[0].get(\dbMakeUp,{|g| g.postln})o
0.dbamp

t = NS_Test(bus: 0)
t.free

s.plotTree
s.meter
s.scope
s.freqscope
b = NS_MainWindow() 
b.server


b.win.visible
b.win.front

false.not



a = a ? Server.default

n = NS_Server('test') 
n.strips[0][0].outBus
n.outMixer[0].inSynth
n.inGroup
n.strips[5][0].stripGroup.run(false)
n.strips[5][1].group.run(true)
n.stripBusses.size

n = NSFW(NS_OpenStageControl(), 1)
n.controllers
NSFW.servers['nsfw_0'].server.plotTree
NSFW.servers['nsfw_0'].server.meter
NSFW.servers['nsfw_0'].server.scope
NSFW.servers['nsfw_0'].strips[5][3].stripBus
NSFW.servers['nsfw_0'].outMixerBusses<D-§>



t = NS_Transceiver.listenForOSC(true,\button)
"/button1".contains("button")

Server.killAll
Server.quitAll
Server.all
Server.allRunningServers


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

OSCFunc.trace(true,true)
NSFW.servers['nsfw_0'].server.sendMsg("/n_set",1317,\amp,0.1)

OSCFunc.trace(true)

NS_Transceiver.listenForOSC(true,RingMod(),)

v = [0.2,0.4]
v = v.collect({|i| \db.asSpec.map(i) })
v

OSCdef(\test,{|msg| msg[1].postln},'/touch_fader_1')


Archive


