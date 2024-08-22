


NS_ServerHub.servers['nsfw_0'].server;
NS_ServerHub.servers['nsfw_0'].strips[0][1].inSink.module.pattern.play
NS_ServerHub.servers['nsfw_0'].server.plotTree;
NS_ServerHub.servers['nsfw_0'].server.scope;
NS_ServerHub.servers['nsfw_0'].server.freqscope;
NS_ServerHub.servers['nsfw_0'].server.meter;
NS_ServerHub.servers['nsfw_0'].server.peakCPU;
NS_ServerHub.servers['nsfw_0'].server.avgCPU;
NS_ServerHub.servers['nsfw_0'].server.numUGens;

\amp.asSpec

n = NSFW(OpenStageControl,[64])
NSFW.controllers[0]


g = Group()
NS_InChannelStrip(0).view.front


// repper switch should become three (bigger) buttons, get rid of trigger button
// dust/impulse trigger for repper? also -> fix repper amp/mix...
// if I can stop the pause script from clicking, make muting also pause ChannelStrips? To save CPU?
// channel strips need to save their inSynthGate value
// repper insynthGate?!?!?!
// ServerHub -> add a TextField to the input busses so I can write notes: Saxophone, Hakon, etc.
// gotta get EnvGen working properly!! Test without ramp args, could it be that .kr/.ar is messing things up?
// BufferPB: can I add 6 (or whatever) DragSinks for audio files with a switch to go between them?
// NS_ShortLoops need a default value for Latch when the module initiates
// make decent reverb - maybe FreeVerb with EQs?

g = Group()
n = NS_NoePaaM(g,0)
n.pattern
s.plotTree
n.free

12.midiratio
{Changed.ar(Impulse.ar(9)).poll}.play


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
a = List.newClear(0)
a[0].isNil


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

(
SynthDef(\ns_noePaaM,{
    var numChans = NSFW.numOutChans;
    var numVoices = 8;
    var sig = SinOsc.ar(\freq.kr() * LFNoise1.kr(0.1!numVoices,\detune.kr(0.01).clip(0.0001,1)).midiratio,Rand(0,2pi));
    sig = sig + HPF.ar(PinkNoise.ar(\noise.kr(0)).wrap2,\freq.kr());
    sig = RLPF.ar(sig,\filtFreq.kr(),\rq.kr(0.8).clip(0.01,1));
    sig = sig * Env.perc(\atk.kr(0.01).clip(0.001,1),\rls.kr(0.5),1,\curve.kr(0.25).linlin(0,1,-10,10)).kr(2);
    sig = (sig * \gain.kr(0.25).linlin(0,1,1,40)).tanh;
    sig = LeakDC.ar(sig);
    sig = sig.tanh;
    sig = Splay.ar(sig, 1, 1, \pan.kr(0));

    sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
    NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
}).add
        )

(
var arpArgs = ['detune','width','noise','rq','gain','atk','rls','curve','lpgLag','lpgRq', 'mute'];
var subD = 0.25;
~arpDict = Dictionary[];

arpArgs.do({ |key|
	var value = ~arpDict[key] ? Bus.control(s,1).set(0.5);
	~arpDict.put(key, value)
});

~ampBus = Bus.control(s,4).setn(1!4);

TempoClock.default.tempo = 92.5/60;

Pdef(\arpSynth,
	
		Ppar([
			Pbind(
				\dur,Pwrand([0.25,Pseq([0.25/2],2)],[0.5,0.1].normalizeSum,inf),
				\instrument, \ns_noePaaM,
				\legato,Pfunc{ {0.2.rrand(0.8)}!4 },
				\freq,Pstep(Pseq(~aFlat,inf),).midicps * Prand(([1,2]!4).allTuples,inf) * Prand([[1,1,1,1],[1,1,1,0.5]],inf),
				// \freq,Pstep(Pseq(~mm193),Pseq(~mm193Durs),inf).midicps * Prand(([1,2]!4).allTuples,inf) * Prand([[1,1,1,1],[1,1,1,0.5]],inf),
				\filtFreq,Pkey(\freq) * 2,
				// \filtFreq,200, // 200 is nice!
				'detune',Pfunc{ ~arpDict['detune'].getSynchronous },
				'width',Pfunc{ ~arpDict['width'].getSynchronous },
				'noise',Pfunc{ ~arpDict['noise'].getSynchronous },
				'rq',  Pkey(\dur) * Pfunc{ ~arpDict['rq'].getSynchronous },
				'gain',Pfunc{ ~arpDict['gain'].getSynchronous },
				'atk',Pfunc{ ~arpDict['atk'].getSynchronous },
				'rls',Pkey(\dur) * Pfunc{ ~arpDict['rls'].getSynchronous } * 4,
				'curve',Pfunc{ ~arpDict['curve'].getSynchronous },
				\pan,[0,-0.5,0.5,0],
				\accent,Pfunc{ [1,0.5,0.5,0.5].scramble },
				\mute,Pfunc { { (0.8.rand <= ~arpDict['mute'].getSynchronous).asInteger }!2 },
				// \mute,Pfunc { [1,1,1,1].scramble },
				\amp,Pfunc{ ~ampBus.getnSynchronous(4) } * Pkey(\accent) * Pkey(\mute) * -30.dbamp,
				\outBus,0,
			)
		]),
).quant_(4).play
)

s.meter
s.scope
~ampBus.setn(1!4)
~arpDict['atk'].set(0.01)
~arpDict['noise'].set(0.01)
~arpDict['detune'].set(0.1)


Pbindef;

(0.1-0.1).linlin(-1,1,-4,4)

g = Group()
n = NS_NoePaaM(g,0)
n.data
n.free
n.pattern.play
n.pattern.stop

Pbindef(\goy,\dummyF,Pstep(Pseq(n.data['mm53'],1)).midicps)

s.meter
s.plotTree
s.scope


TempoClock.default.tempo = 60/60
d = Dictionary[
\g -> [4,5,6],
\a -> [1,2,3]
]

// hovedskjema in the rubato section
// start synth -> with arppregio
// refusal -> can I split the two synths and process the track? Maybe do it inline
