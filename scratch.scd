
NSFW.numOutChans

s.options.numOutputBusChannels

g = Group()
g.server.options.numOutputBusChannels

NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][0].inSynth.get(\inBus,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[0][1].inSynthGate
NS_ServerHub.servers['nsfw_0'].inputBusses
NS_ServerHub.servers['nsfw_0'].inGroup.server
NS_ServerHub.servers['nsfw_0'].server.plotTree;
NS_ServerHub.servers['nsfw_0'].server.scope;
NS_ServerHub.servers['nsfw_0'].server.freqscope;
NS_ServerHub.servers['nsfw_0'].server.meter;
NS_ServerHub.servers['nsfw_0'].server.peakCPU;
NS_ServerHub.servers['nsfw_0'].server.avgCPU;
NS_ServerHub.servers['nsfw_0'].server.numSynths;

n = NSFW(OpenStageControl,[64])
NSFW.controllers[0]

v[0]


g = Group()
n = NS_OutChannelStrip(g)
n.view.front


NS_ServerHub.makeWindow




NS_ModuleList.open

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
// refusal -> can I split the two synths and process the track? Maybe do it inline

g = Group()
n = NS_Squish()
s.plotTree
s.meter
n.free


(
SynthDef(\bass, {
	var snd, auto, freq, trig;
	freq = \freq.kr(40);
	trig = ToggleFF.ar(Dust.ar(8));
	auto = trig.lagud(0.1, 0.3);
	snd = Saw.ar(freq);
	snd = Splay.ar(snd);
	snd = (snd * 3).fold2;
	snd = RLPF.ar(snd, freq * 10, 0.1);
	snd = (snd * 5).fold2;
	snd = RLPF.ar(snd, freq * 8 * TRand.ar(1, 4, trig), 0.5);
	snd = (snd * 5).fold2;
	snd = MoogFF.ar(snd, auto.linexp(0, 1, 100, 8000), 0);
	snd = snd + DelayC.ar(snd, 0.01, SinOsc.ar(0.5, [0, pi]).range(0, 0.001));
	snd = (snd * 3.dbamp).tanh;
	Out.ar(\out.kr(0), snd);
}).play;
)


SynthDef(\moistureBass, {
	var sig = VarSaw.ar(\freq.kr(440),width: \width.kr(0.1));
	sig = BHiShelf.ar(sig, 3000, 1.0, 10.0);
	sig = BPF.ar(sig, (0..31).linexp(0, 31, 100, 8000), \rq.kr(0.02)).sum;
	sig = (sig * 3).fold2;
	sig = RLPF.ar(sig,8000,0.1);
	sig = Squish.ar(sig,sig,-18,0.001,0.1,4,0.2,4);
	sig = (sig * \gain.kr(1)).tanh;
	sig = sig * Env.perc().ar(2);
	sig = sig ! 2 ;
	sig = sig * \amp.kr(0.5);
	Out.ar(\outBus.kr(0), sig);
}).add;

x = Synth(\moistureBass,[\freq,40])
x.free
