
(
var bounds = Window.availableBounds;
var win = Window("NSFW",);

win.layout_(
    HLayout(
        NS_Knob(win,"tost",\db,{|k| k.value.postln },true, -18),
        //NS_Switch(win,(0..4),{|b| b.value.postln},\horz),
        ~test = NS_Fader(win,"test",\amp,{|b,i| [b,i].postln}),
    )
);
win.front;
)

n = NSFW(NS_OpenStageControl,[64])
NSFW.servers['nsfw_0'].window.swapGrid.controls

NSFW.servers['nsfw_0'].options.blockSize
NSFW.servers['nsfw_0'].server
NSFW.servers['nsfw_0'].outMixer[0].inSynthGate_(1)
NSFW.servers['nsfw_0'].strips[0][0].inSynthGate
NSFW.servers['nsfw_0'].strips[0][0].stripBus
NSFW.servers['nsfw_0'].server.plotTree
NSFW.servers['nsfw_0'].server.scope
NSFW.servers['nsfw_0'].server.meter
NSFW.servers['nsfw_0'].strips[0][0].paused
NSFW.servers['nsfw_1'].server.peakCPU
NSFW.servers['nsfw_0'].window.header.stereoInArray[1].inputBus

Window
NSFW.numInChans

r = NS_Test()
r.oscFuncs[1]
r.controls[1].spec.map(0.9)
r.controls[1].value_(255.15)
r.assignButtons
r.free

OSCFunc.trace(false,false)

i = NS_InputStereo().setInBus(0,Bus.audio( ))[ 0 - 1, Bus(audio, 5, 1, localhost) ]
i.free
0.interpret.isInteger
x = (0..4)
x.drop(-1).collect({ |i| [i,i+1]})

Class
PathName(NS_InputMono.filenameSymbol.asString).fileNameWithoutExtension
NS_Server.inputArrayBu

x = Synth(\default)
x.free

("NS_" ++ "Test").asSymbol.asClass
'SinOsc'.asClass.superclass
b = Bus.audio(s) 
b.index

