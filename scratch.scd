
NSFW.numOutChans

s.options.numOutputBusChannels

g = Group()
g.server.options.numOutputBusChannels

NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[2][1].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips[2][1].setInSynthGate(0)
NS_ServerHub.servers['nsfw_0'].strips.deepDo(2,{ |i| i.inSynthGate.postln; })
NS_ServerHub.servers['nsfw_0'].window.swapGrid.controls[2].value;
NS_ServerHub.servers['nsfw_0'].inGroup.server;
NS_ServerHub.servers['nsfw_0'].server.plotTree;
NS_ServerHub.servers['nsfw_0'].server.scope;
NS_ServerHub.servers['nsfw_0'].server.freqscope;
NS_ServerHub.servers['nsfw_0'].server.meter;
NS_ServerHub.servers['nsfw_0'].server.peakCPU;
NS_ServerHub.servers['nsfw_0'].server.avgCPU;
NS_ServerHub.servers['nsfw_0'].server.numSynths;

NS_ServerHub.servers['nsfw_0'].server.record("/Users/mikemccormick/Desktop/nightmare.wav",0,2,1018)
NS_ServerHub.servers['nsfw_0'].server.stopRecording

n = NSFW(OpenStageControl,[64])


g = Group()
n = NS_OutChannelStrip(g)
n.view.front

NS_ServerHub.makeWindow

NS_ModuleList.open

n = NS_Gate()
n.free



x = 2.reciprocal - 1

thresh + (signal - thresh) * ratio
(-10 + (-20 - -10).min(0)) * 2


(6 * -1).dbamp

0.dbamp

(
w = Window("test");
v = VLayout();

w.layout_(v);
w.setInnerExtent(200,200);
w.front
)
v.add(Button())



// dust/impulse trigger for repper? also -> fix repper amp/mix...
// if I can stop the pause script from clicking, make muting also pause ChannelStrips? To save CPU?
// ServerHub -> add a TextField to the input busses so I can write notes: Saxophone, Hakon, etc.
// BufferPB: can I add 6 (or whatever) DragSinks for audio files with a switch to go between them?
// when assigning "play/bypass" buttons, inSynthGate gets an extra 0 (first press doesn't register)...how to fix?



10 **4


//
(
SynthDef(\ns_chorus,{
    var numChans = NSFW.numOutChans;
    //var sig = In.ar(\bus.kr, numChans);
    var in = PlayBuf.ar(2,\bufnum.kr,BufRateScale.kr(\bufnum.kr), loop: 1);

    var sig = in + LocalIn.ar(numChans);
    8.do{ sig = DelayC.ar(sig,0.2,{ SinOsc.kr(\rate.kr(0.1),{pi.rand}).range(0.01,0.025) }) };

    LocalOut.ar(sig * \feedB.kr(0.8));
    sig = sig + in;
    sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
    NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
}).add
)
b = Buffer.read(s, "/Users/mikemccormick/Desktop/GHOST samples/stereo/ciaccona.aiff")
x = Synth(\ns_chorus,[\bus,0,\thru,1, \bufnum, b])
x.set(\rate,0.1,\feedB,0.2)
s.scope
s.meter



(
u = Signal.sineFill(512, [1]);
b = Buffer.loadCollection(s, u, 1);
)

b.plot

